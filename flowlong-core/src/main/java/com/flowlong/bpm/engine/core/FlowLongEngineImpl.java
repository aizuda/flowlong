/*
 * Copyright 2023-2025 Licensed under the AGPL License
 */
package com.flowlong.bpm.engine.core;

import com.flowlong.bpm.engine.FlowLongEngine;
import com.flowlong.bpm.engine.assist.Assert;
import com.flowlong.bpm.engine.assist.DateUtils;
import com.flowlong.bpm.engine.assist.ObjectUtils;
import com.flowlong.bpm.engine.core.enums.PerformType;
import com.flowlong.bpm.engine.entity.FlwInstance;
import com.flowlong.bpm.engine.entity.FlwProcess;
import com.flowlong.bpm.engine.entity.FlwTask;
import com.flowlong.bpm.engine.entity.FlwTaskActor;
import com.flowlong.bpm.engine.handler.impl.CreateTaskHandler;
import com.flowlong.bpm.engine.model.NodeAssignee;
import com.flowlong.bpm.engine.model.NodeModel;
import com.flowlong.bpm.engine.model.ProcessModel;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.function.Consumer;

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
    public Optional<FlwInstance> startInstanceById(Long id, FlowCreator flowCreator, Map<String, Object> args) {
        FlwProcess process = processService().getProcessById(id);
        if (null == process) {
            return Optional.empty();
        }
        return this.startProcess(process.checkState(), flowCreator, args);
    }

    /**
     * 根据流程名称、版本号、创建人、参数列表启动流程实例
     */
    @Override
    public Optional<FlwInstance> startInstanceByName(String name, Integer version, FlowCreator flowCreator, Map<String, Object> args) {
        FlwProcess process = processService().getProcessByVersion(name, version);
        return this.startProcess(process, flowCreator, args);
    }

    /**
     * 启动流程实例
     */
    protected Optional<FlwInstance> startProcess(FlwProcess process, FlowCreator flowCreator, Map<String, Object> args) {
        Execution execution = this.execute(process, flowCreator, args);
        // 执行启动模型
        process.executeStartModel(flowLongContext, execution);
        return Optional.ofNullable(execution.getFlwInstance());
    }


    /**
     * 创建流程实例，并返回执行对象
     *
     * @param process     流程定义
     * @param flowCreator 流程实例任务创建者
     * @param args        参数列表
     * @return Execution
     */
    protected Execution execute(FlwProcess process, FlowCreator flowCreator, Map<String, Object> args) {
        FlwInstance flwInstance = runtimeService().createInstance(process, flowCreator, args);
        if (log.isDebugEnabled()) {
            log.debug("创建流程实例对象:" + flwInstance);
        }
        Execution current = new Execution(this, process, flwInstance, args);
        current.setCreateId(flowCreator.getCreateId());
        current.setCreateBy(flowCreator.getCreateBy());
        return current;
    }

    /**
     * 根据任务ID，创建人，参数列表执行任务
     */
    @Override
    public void executeTask(Long taskId, FlowCreator flowCreator, Map<String, Object> args) {
        // 完成任务，并且构造执行对象
        this.execute(taskId, flowCreator, args, execution -> {
            // 执行节点模型
            execution.getProcess().executeNodeModel(flowLongContext, execution, execution.getFlwTask().getTaskName());
        });
    }

    /**
     * 执行任务并跳转到指定节点
     */
    @Override
    public void executeAndJumpTask(Long taskId, String nodeName, FlowCreator flowCreator, Map<String, Object> args) {
        // 执行当前任务
        this.execute(taskId, flowCreator, args, execution -> {
            ProcessModel processModel = execution.getProcess().getProcessModel();
            Assert.isNull(processModel, "当前任务未找到流程定义模型");

            // 查找模型节点
            NodeModel nodeModel = processModel.getNode(nodeName);
            Assert.isNull(nodeModel, "根据节点名称[" + nodeName + "]无法找到节点模型");

            // 创建当前节点任务
            nodeModel.createTask(flowLongContext, execution);
        });
    }

    /**
     * 根据任务ID，创建人，参数列表完成任务，并且构造执行对象
     */
    protected void execute(Long taskId, FlowCreator flowCreator, Map<String, Object> args, Consumer<Execution> executeNextStep) {
        if (args == null) {
            args = new HashMap<>();
        }
        FlwTask flwTask = taskService().complete(taskId, flowCreator, args);
        if (log.isDebugEnabled()) {
            log.debug("任务[taskId=" + taskId + "]已完成");
        }
        FlwInstance flwInstance = queryService().getInstance(flwTask.getInstanceId());
        Assert.isNull(flwInstance, "指定的流程实例[id=" + flwTask.getInstanceId() + "]已完成或不存在");
        flwInstance.setLastUpdateBy(flowCreator.getCreateId());
        flwInstance.setLastUpdateTime(DateUtils.getCurrentDate());
        runtimeService().updateInstance(flwInstance);

        PerformType performType = PerformType.get(flwTask.getPerformType());
        if (performType == PerformType.countersign) {
            /**
             * 会签未全部完成，不继续执行节点模型
             */
            List<FlwTask> flwTaskList = queryService().getTasksByInstanceIdAndTaskName(flwInstance.getId(), flwTask.getTaskName());
            if (ObjectUtils.isNotEmpty(flwTaskList)) {
                return;
            }
        }

        /**
         * 流程模型
         */
        FlwProcess process = processService().getProcessById(flwInstance.getProcessId());

        /**
         * 票签（ 总权重大于 50% 表示通过 ）
         */
        if (performType == PerformType.voteSign) {
            Optional<List<FlwTaskActor>> flwTaskActorsOptional = queryService().getActiveTaskActorsByInstanceId(flwInstance.getId());
            if (flwTaskActorsOptional.isPresent()) {
                NodeModel nodeModel = process.getProcessModel().getNode(flwTask.getTaskName());
                int passWeight = nodeModel.getPassWeight() == null ? 50 : nodeModel.getPassWeight();
                int votedWeight = 100 - flwTaskActorsOptional.get().stream().mapToInt(t -> t.getWeight() == null ? 0 : t.getWeight()).sum();
                if (votedWeight < passWeight) {
                    // 投票权重小于节点权重继续投票
                    return;
                } else {
                    // 投票完成关闭投票状态，进入下一个节点
                    Assert.isFalse(taskService().completeActiveTasksByInstanceId(flwInstance.getId(), flowCreator),
                            "Failed to close voting status");
                }
            }
        }

        /**
         * 追加实例参数
         */
        Map<String, Object> instanceMaps = flwInstance.getVariableMap();
        if (instanceMaps != null) {
            for (Map.Entry<String, Object> entry : instanceMaps.entrySet()) {
                if (args.containsKey(entry.getKey())) {
                    continue;
                }
                args.put(entry.getKey(), entry.getValue());
            }
        }
        Execution execution = new Execution(this, process, flwInstance, args);
        execution.setCreateId(flowCreator.getCreateId());
        execution.setCreateBy(flowCreator.getCreateBy());
        execution.setFlwTask(flwTask);

        /**
         * 按顺序依次审批，一个任务按顺序多个参与者依次添加
         */
        if (performType == PerformType.sort) {
            NodeModel nodeModel = process.getProcessModel().getNode(flwTask.getTaskName());
            boolean findTaskActor = false;
            NodeAssignee nextNodeAssignee = null;
            List<NodeAssignee> nodeUserList = nodeModel.getNodeUserList();
            if (ObjectUtils.isEmpty(nodeUserList)) {
                /**
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
                        if (Objects.equals(taskActor.getActorId(), flowCreator.getCreateId())) {
                            findTaskActor = true;
                        }
                    }
                }
            } else {
                /**
                 * 模型中去找下一个执行者
                 */
                for (NodeAssignee nodeAssignee : nodeUserList) {
                    if (findTaskActor) {
                        // 找到下一个执行人
                        nextNodeAssignee = nodeAssignee;
                        break;
                    }
                    if (Objects.equals(nodeAssignee.getId(), flowCreator.getCreateId())) {
                        findTaskActor = true;
                    }
                }
            }

            // 如果下一个顺序执行人存在，创建顺序审批任务
            if (null != nextNodeAssignee) {
                execution.setNextFlwTaskActor(FlwTaskActor.ofUser(nextNodeAssignee.getId(), nextNodeAssignee.getName()));
                new CreateTaskHandler(nodeModel).handle(flowLongContext, execution);
                return;
            }
        }

        // 执行回调逻辑
        executeNextStep.accept(execution);
    }
}
