/*
 * Copyright 2023-2025 Licensed under the AGPL License
 */
package com.flowlong.bpm.engine.core;

import com.flowlong.bpm.engine.FlowLongEngine;
import com.flowlong.bpm.engine.assist.Assert;
import com.flowlong.bpm.engine.assist.DateUtils;
import com.flowlong.bpm.engine.assist.ObjectUtils;
import com.flowlong.bpm.engine.core.enums.PerformType;
import com.flowlong.bpm.engine.core.enums.TaskType;
import com.flowlong.bpm.engine.entity.FlwInstance;
import com.flowlong.bpm.engine.entity.FlwProcess;
import com.flowlong.bpm.engine.entity.FlwTask;
import com.flowlong.bpm.engine.entity.FlwTaskActor;
import com.flowlong.bpm.engine.model.NodeAssignee;
import com.flowlong.bpm.engine.model.NodeModel;
import com.flowlong.bpm.engine.model.ProcessModel;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * 基本的流程引擎实现类
 *
 * <p>
 * 尊重知识产权，不允许非法使用，后果自负
 * </p>
 *
 * @author hubin
 * @since 1.0
 */
@Slf4j
public class FlowLongEngineImpl implements FlowLongEngine {
    /**
     * 配置对象
     */
    protected FlowLongContext flowLongContext;

    @Override
    public FlowLongEngine configure(FlowLongContext flowLongContext) {
        this.flowLongContext = flowLongContext;
        return this;
    }

    @Override
    public FlowLongContext getContext() {
        return this.flowLongContext;
    }

    /**
     * 根据流程定义ID，创建人，参数列表启动流程实例
     */
    @Override
    public Optional<FlwInstance> startInstanceById(Long id, FlowCreator flowCreator, Map<String, Object> args, Supplier<FlwInstance> supplier) {
        FlwProcess process = processService().getProcessById(id);
        return this.startProcessInstance(process.checkState(), flowCreator, args, supplier);
    }

    /**
     * 根据流程定义key、版本号、创建人、参数列表启动流程实例
     */
    @Override
    public Optional<FlwInstance> startInstanceByProcessKey(String processKey, Integer version, FlowCreator flowCreator, Map<String, Object> args, Supplier<FlwInstance> supplier) {
        FlwProcess process = processService().getProcessByVersion(processKey, version);
        return this.startProcessInstance(process, flowCreator, args, supplier);
    }

    /**
     * 启动流程实例
     *
     * @param process     流程定义对象
     * @param flowCreator 流程创建者
     * @param args        执行参数
     * @param supplier    初始化流程实例提供者
     * @return 流程实例
     */
    protected Optional<FlwInstance> startProcessInstance(FlwProcess process, FlowCreator flowCreator, Map<String, Object> args, Supplier<FlwInstance> supplier) {
        // 执行启动模型
        return process.executeStartModel(flowLongContext, flowCreator, nodeModel -> {
            FlwInstance flwInstance = runtimeService().createInstance(process, flowCreator, args, nodeModel.getNodeName(), supplier);
            if (log.isDebugEnabled()) {
                log.debug("创建流程实例对象:" + flwInstance);
            }
            return new Execution(this, process.model(), flowCreator, flwInstance, args);
        });
    }

    /**
     * 重启流程实例（从当前所在节点currentNode位置开始）
     */
    @Override
    public void restartProcessInstance(Long id, String currentNode, Execution execution) {
        FlwProcess process = processService().getProcessById(id);
        NodeModel nodeModel = process.model().getNode(currentNode);
        if (null != nodeModel) {
            nodeModel.nextNode().ifPresent(childNode -> childNode.execute(flowLongContext, execution));
        }
    }

    /**
     * 根据任务ID，创建人，参数列表执行任务
     */
    @Override
    public boolean executeTask(Long taskId, FlowCreator flowCreator, Map<String, Object> args) {
        // 完成任务，并且构造执行对象
        return this.execute(taskId, flowCreator, args, execution -> {
            // 执行节点模型
            return execution.getProcessModel().executeNodeModel(flowLongContext, execution, execution.getFlwTask().getTaskName());
        });
    }

    /**
     * 执行任务并跳转到指定节点
     */
    @Override
    public boolean executeJumpTask(Long taskId, String nodeName, FlowCreator flowCreator) {
        // 执行任务跳转归档
        return taskService().executeJumpTask(taskId, nodeName, flowCreator, flwTask -> {
            FlwInstance flwInstance = this.getFlwInstance(flwTask.getInstanceId(), flowCreator.getCreateBy());
            ProcessModel processModel = runtimeService().getProcessModelByInstanceId(flwInstance.getId());
            return new Execution(this, processModel, flowCreator, flwInstance, flwInstance.variableToMap());
        });
    }

    @Override
    public boolean executeAppendNodeModel(Long taskId, NodeModel nodeModel, FlowCreator flowCreator, boolean beforeAfter) {
        // 追加指定节点模型
        runtimeService().appendNodeModel(taskId, nodeModel, beforeAfter);

        // 前置加签、执行任务并跳转到指定节点（后置加签无需处理任务流转，当前正常任务审批后进入后置加签节点模型）
        return beforeAfter && executeJumpTask(taskId, nodeModel.getNodeName(), flowCreator);
    }

    /**
     * 获取流程实例
     *
     * @param instanceId 流程实例ID
     * @param updateBy   更新人
     * @return {@link FlwInstance}
     */
    protected FlwInstance getFlwInstance(Long instanceId, String updateBy) {
        FlwInstance flwInstance = queryService().getInstance(instanceId);
        Assert.isNull(flwInstance, "指定的流程实例[id=" + instanceId + "]已完成或不存在");
        flwInstance.setLastUpdateBy(updateBy);
        flwInstance.setLastUpdateTime(DateUtils.getCurrentDate());
        runtimeService().updateInstance(flwInstance);
        return flwInstance;
    }

    /**
     * 根据任务ID，创建人，参数列表完成任务，并且构造执行对象
     */
    protected boolean execute(Long taskId, FlowCreator flowCreator, Map<String, Object> args, Function<Execution, Boolean> executeNextStep) {
        if (args == null) {
            args = new HashMap<>();
        }
        FlwTask flwTask = taskService().complete(taskId, flowCreator, args);
        if (log.isDebugEnabled()) {
            log.debug("任务[taskId=" + taskId + "]已完成");
        }
        FlwInstance flwInstance = this.getFlwInstance(flwTask.getInstanceId(), flowCreator.getCreateBy());
        PerformType performType = PerformType.get(flwTask.getPerformType());
        if (performType == PerformType.countersign) {
            /*
             * 会签未全部完成，不继续执行节点模型
             */
            List<FlwTask> flwTaskList = queryService().getTasksByInstanceIdAndTaskName(flwInstance.getId(), flwTask.getTaskName());
            if (ObjectUtils.isNotEmpty(flwTaskList)) {
                return true;
            }
        }

        /*
         * 流程模型
         */
        final ProcessModel processModel = runtimeService().getProcessModelByInstanceId(flwInstance.getId());

        /*
         * 票签（ 总权重大于 50% 表示通过 ）
         */
        if (performType == PerformType.voteSign) {
            Optional<List<FlwTaskActor>> flwTaskActorsOptional = queryService().getActiveTaskActorsByInstanceId(flwInstance.getId());
            if (flwTaskActorsOptional.isPresent()) {
                NodeModel nodeModel = processModel.getNode(flwTask.getTaskName());
                int passWeight = nodeModel.getPassWeight() == null ? 50 : nodeModel.getPassWeight();
                int votedWeight = 100 - flwTaskActorsOptional.get().stream().mapToInt(t -> t.getWeight() == null ? 0 : t.getWeight()).sum();
                if (votedWeight < passWeight) {
                    // 投票权重小于节点权重继续投票
                    return true;
                } else {
                    // 投票完成关闭投票状态，进入下一个节点
                    Assert.isFalse(taskService().completeActiveTasksByInstanceId(flwInstance.getId(), flowCreator),
                            "Failed to close voting status");
                }
            }
        }

        /*
         * 追加实例参数
         */
        Map<String, Object> instanceMaps = flwInstance.variableToMap();
        if (instanceMaps != null) {
            for (Map.Entry<String, Object> entry : instanceMaps.entrySet()) {
                if (args.containsKey(entry.getKey())) {
                    continue;
                }
                args.put(entry.getKey(), entry.getValue());
            }
        }
        Execution execution = new Execution(this, processModel, flowCreator, flwInstance, args);
        execution.setFlwTask(flwTask);

        /*
         * 按顺序依次审批，一个任务按顺序多个参与者依次添加
         */
        if (performType == PerformType.sort) {
            NodeModel nodeModel = processModel.getNode(flwTask.getTaskName());
            boolean findTaskActor = false;
            NodeAssignee nextNodeAssignee = null;
            List<NodeAssignee> nodeUserList = nodeModel.getNodeUserList();
            // 当前任务实际办理人
            String assigneeId = flowCreator.getCreateId();
            if (TaskType.transfer.getValue() == flwTask.getTaskType()) {
                assigneeId = flwTask.getAssignorId();
            }
            if (ObjectUtils.isEmpty(nodeUserList)) {
                /*
                 * 模型未设置处理人，那么需要获取自定义参与者
                 */
                List<FlwTaskActor> taskActors = execution.getTaskActorProvider().getTaskActors(nodeModel, execution);
                if (ObjectUtils.isNotEmpty(taskActors)) {
                    for (FlwTaskActor taskActor : taskActors) {
                        if (findTaskActor) {
                            // 找到下一个执行人
                            nextNodeAssignee = NodeAssignee.of(taskActor);
                            break;
                        }

                        // 判断找到当前任务实际办理人
                        if (Objects.equals(taskActor.getActorId(), assigneeId)) {
                            findTaskActor = true;
                        }
                    }
                }
            } else {
                /*
                 * 模型中去找下一个执行者
                 */
                for (NodeAssignee nodeAssignee : nodeUserList) {
                    if (findTaskActor) {
                        // 找到下一个执行人
                        nextNodeAssignee = nodeAssignee;
                        break;
                    }
                    if (Objects.equals(nodeAssignee.getId(), assigneeId)) {
                        findTaskActor = true;
                    }
                }
            }

            // 如果下一个顺序执行人存在，创建顺序审批任务
            if (null != nextNodeAssignee) {
                execution.setNextFlwTaskActor(FlwTaskActor.ofNodeAssignee(nextNodeAssignee));
                return flowLongContext.createTask(execution, nodeModel);
            }
        }

        // 执行回调逻辑
        return executeNextStep.apply(execution);
    }
}
