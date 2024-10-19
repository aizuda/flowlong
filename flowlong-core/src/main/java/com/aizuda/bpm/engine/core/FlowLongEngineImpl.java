/*
 * Copyright 2023-2025 Licensed under the AGPL License
 */
package com.aizuda.bpm.engine.core;

import com.aizuda.bpm.engine.FlowLongEngine;
import com.aizuda.bpm.engine.assist.Assert;
import com.aizuda.bpm.engine.assist.DateUtils;
import com.aizuda.bpm.engine.assist.ObjectUtils;
import com.aizuda.bpm.engine.core.enums.*;
import com.aizuda.bpm.engine.entity.*;
import com.aizuda.bpm.engine.model.NodeAssignee;
import com.aizuda.bpm.engine.model.NodeModel;
import com.aizuda.bpm.engine.model.ProcessModel;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
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
        FlwProcess process = processService().getProcessByVersion(flowCreator.getTenantId(), processKey, version);
        return this.startProcessInstance(process, flowCreator, args, supplier);
    }

    /**
     * 根据流程对象启动流程实例
     *
     * @param process     流程定义对象
     * @param flowCreator 流程创建者
     * @param args        执行参数
     * @param supplier    初始化流程实例提供者
     * @return {@link FlwInstance} 流程实例
     */
    @Override
    public Optional<FlwInstance> startProcessInstance(FlwProcess process, FlowCreator flowCreator, Map<String, Object> args, Supplier<FlwInstance> supplier) {
        // 执行启动模型
        return process.executeStartModel(flowLongContext, flowCreator, nodeModel -> {
            FlwInstance flwInstance = runtimeService().createInstance(process, flowCreator, args, nodeModel, supplier);
            if (log.isDebugEnabled()) {
                log.debug("start process instanceId={}", flwInstance.getId());
            }
            return new Execution(this, process.model(true), flowCreator, flwInstance, args);
        });
    }

    /**
     * 重启流程实例（从当前所在节点currentNode位置开始）
     */
    @Override
    public void restartProcessInstance(Long id, String currentNodeKey, Execution execution) {
        FlwProcess process = processService().getProcessById(id);
        NodeModel nodeModel = process.model().getNode(currentNodeKey);
        if (null != nodeModel) {
            Optional<NodeModel> nodeModelOptional = nodeModel.nextNode();
            if (nodeModelOptional.isPresent()) {
                // 执行子节点
                nodeModelOptional.get().execute(flowLongContext, execution);
            } else {
                // 不存在任何子节点结束流程
                execution.endInstance(nodeModel);
            }
        }
    }

    /**
     * 根据任务ID，创建人，参数列表执行任务
     */
    @Override
    public boolean executeTask(Long taskId, FlowCreator flowCreator, Map<String, Object> args) {
        // 完成任务，并且构造执行对象
        FlwTask flwTask = taskService().complete(taskId, flowCreator, ObjectUtils.getArgs(args));
        if (log.isDebugEnabled()) {
            log.debug("Execute complete taskId={}", taskId);
        }
        return afterDoneTask(flowCreator, flwTask, args, execution ->
                // 执行节点模型
                execution.executeNodeModel(flowLongContext, execution.getFlwTask().getTaskKey()));
    }

    /**
     * 自动跳转任务
     */
    @Override
    public boolean autoJumpTask(Long taskId, Map<String, Object> args, FlowCreator flowCreator) {
        return executeTask(taskId, flowCreator, args, TaskState.autoJump, TaskEventType.autoJump);
    }

    protected boolean executeTask(Long taskId, FlowCreator flowCreator, Map<String, Object> args, TaskState taskState, TaskEventType eventType) {
        FlwTask flwTask = taskService().executeTask(taskId, flowCreator, ObjectUtils.getArgs(args), taskState, eventType);
        if (log.isDebugEnabled()) {
            log.debug("Auto execute taskId={}", taskId);
        }
        // 完成任务后续逻辑
        return afterDoneTask(flowCreator, flwTask, args, execution -> {
            // 执行节点模型
            return execution.executeNodeModel(flowLongContext, execution.getFlwTask().getTaskKey());
        });
    }

    /**
     * 自动完成任务
     */
    @Override
    public boolean autoCompleteTask(Long taskId, Map<String, Object> args) {
        return executeTask(taskId, FlowCreator.ADMIN, null, TaskState.autoComplete, TaskEventType.autoComplete);
    }

    /**
     * 自动拒绝任务
     */
    @Override
    public boolean autoRejectTask(Long taskId, Map<String, Object> args) {
        FlwTask flwTask = taskService().executeTask(taskId, FlowCreator.ADMIN, ObjectUtils.getArgs(args), TaskState.autoComplete, TaskEventType.autoComplete);
        if (log.isDebugEnabled()) {
            log.debug("Auto reject taskId={}", taskId);
        }
        return null != flwTask;
    }

    /**
     * 执行任务并跳转到指定节点
     */
    @Override
    public boolean executeJumpTask(Long taskId, String nodeKey, FlowCreator flowCreator, Map<String, Object> args) {
        // 执行任务跳转归档
        return taskService().executeJumpTask(taskId, nodeKey, flowCreator, args, flwTask -> {
            FlwInstance flwInstance = this.getFlwInstance(flwTask.getInstanceId(), flowCreator.getCreateBy());
            ProcessModel processModel = runtimeService().getProcessModelByInstanceId(flwInstance.getId());
            Execution execution = new Execution(this, processModel, flowCreator, flwInstance, flwInstance.variableToMap());
            // 传递父节点信息
            execution.setFlwTask(flwTask);
            return execution;
        });
    }

    @Override
    public List<FlwTask> createNewTask(Long taskId, TaskType taskType, PerformType performType, List<FlwTaskActor> taskActors,
                                       FlowCreator flowCreator, Map<String, Object> args) {
        return taskService().createNewTask(taskId, taskType, performType, taskActors, flowCreator, flwTask -> {

            /*
             * 流程模型
             */
            final ProcessModel processModel = runtimeService().getProcessModelByInstanceId(flwTask.getInstanceId());

            // 当前流程实例
            final FlwInstance flwInstance = this.getFlwInstance(flwTask.getInstanceId(), flowCreator.getCreateBy());

            // 构建执行对象
            return this.createExecution(processModel, flwInstance, flwTask, flowCreator, args);
        });
    }

    @Override
    public boolean executeAppendNodeModel(Long taskId, NodeModel nodeModel, FlowCreator flowCreator, Map<String, Object> args, boolean beforeAfter) {
        // 追加指定节点模型
        runtimeService().appendNodeModel(taskId, nodeModel, beforeAfter);

        // 前置加签、执行任务并跳转到指定节点
        if (beforeAfter) {
            return executeJumpTask(taskId, nodeModel.getNodeKey(), flowCreator, args);
        }

        // 后置加签无需处理任务流转，当前正常任务审批后进入后置加签节点模型
        return true;
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
        Assert.isNull(flwInstance, "process instance [ id=" + instanceId + " ] completed or not present");
        flwInstance.setLastUpdateBy(updateBy);
        flwInstance.setLastUpdateTime(DateUtils.getCurrentDate());
        runtimeService().updateInstance(flwInstance);
        return flwInstance;
    }

    /**
     * 任务完成以后后续任务节点生成，逻辑判断
     */
    private boolean afterDoneTask(FlowCreator flowCreator, FlwTask flwTask, Map<String, Object> args,
                                  Function<Execution, Boolean> executeNextStep) {
        if (TaskType.agent.eq(flwTask.getTaskType())) {
            // 代理人完成任务，结束后续执行
            return true;
        }

        FlwInstance flwInstance = this.getFlwInstance(flwTask.getInstanceId(), flowCreator.getCreateBy());
        PerformType performType = PerformType.get(flwTask.getPerformType());
        if (performType == PerformType.countersign) {
            /*
             * 会签未全部完成，不继续执行节点模型
             */
            List<FlwTask> flwTaskList = queryService().getTasksByInstanceIdAndTaskKey(flwInstance.getId(), flwTask.getTaskKey());
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
                NodeModel nodeModel = processModel.getNode(flwTask.getTaskKey());
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

        // 构建执行对象
        Map<String, Object> objectMap = ObjectUtils.getArgs(args);
        final Execution execution = this.createExecution(processModel, flwInstance, flwTask, flowCreator, objectMap);

        /*
         * 按顺序依次审批，一个任务按顺序多个参与者依次添加
         */
        if (performType == PerformType.sort) {
            NodeModel nodeModel = processModel.getNode(flwTask.getTaskKey());
            boolean findTaskActor = false;
            NodeAssignee nextNodeAssignee = null;
            List<NodeAssignee> nodeAssigneeList = nodeModel.getNodeAssigneeList();
            // 当前任务实际办理人
            String assigneeId = flowCreator.getCreateId();
            if (NodeSetType.role.eq(nodeModel.getSetType()) || NodeSetType.department.eq(nodeModel.getSetType())) {
                // 角色、部门 任务参与者
                List<FlwHisTaskActor> htaList = flowLongContext.getQueryService().getHisTaskActorsByTaskIdAndActorId(flwTask.getId(), flowCreator.getCreateId());
                if (ObjectUtils.isNotEmpty(htaList)) {
                    assigneeId = htaList.get(0).getAgentId();
                }
            } else if (TaskType.transfer.getValue() == flwTask.getTaskType()) {
                assigneeId = flwTask.getAssignorId();
            }
            if (ObjectUtils.isEmpty(nodeAssigneeList)) {
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
                for (NodeAssignee nodeAssignee : nodeAssigneeList) {
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

        /*
         * 执行触发器任务
         */
        if (performType == PerformType.trigger) {
            taskService().executeTaskTrigger(execution, flwTask);
            return true;
        }

        // 执行回调逻辑
        return executeNextStep.apply(execution);
    }

    protected Execution createExecution(ProcessModel processModel, FlwInstance flwInstance, FlwTask flwTask,
                                        FlowCreator flowCreator, Map<String, Object> args) {
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
        return execution;
    }

}
