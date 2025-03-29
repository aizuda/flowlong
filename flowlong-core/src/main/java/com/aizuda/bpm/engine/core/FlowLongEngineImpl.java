/*
 * Copyright 2023-2025 Licensed under the Dual Licensing
 * website: https://aizuda.com
 */
package com.aizuda.bpm.engine.core;

import com.aizuda.bpm.engine.FlowLongEngine;
import com.aizuda.bpm.engine.assist.Assert;
import com.aizuda.bpm.engine.assist.DateUtils;
import com.aizuda.bpm.engine.assist.ObjectUtils;
import com.aizuda.bpm.engine.core.enums.*;
import com.aizuda.bpm.engine.entity.*;
import com.aizuda.bpm.engine.model.ModelHelper;
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
 * <a href="https://aizuda.com">官网</a>尊重知识产权，不允许非法使用，后果自负
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

        // 会签情况存在多个任务，遇到某个处理人自动跳过
        if (TaskEventType.autoJump.eq(eventType) && PerformType.countersign.eq(flwTask.getPerformType())) {
            // 直接返回，不再执行后续逻辑
            return true;
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
    public boolean autoCompleteTask(Long taskId, Map<String, Object> args, FlowCreator flowCreator) {
        return executeTask(taskId, flowCreator, null, TaskState.autoComplete, TaskEventType.autoComplete);
    }

    /**
     * 自动拒绝任务
     */
    @Override
    public boolean autoRejectTask(FlwTask flwTask, Map<String, Object> args, FlowCreator flowCreator) {
        Optional<FlwTask> flwTaskOptional = taskService().rejectTask(flwTask, flowCreator, args);
        if (log.isDebugEnabled()) {
            log.debug("Auto reject taskId={}", flwTask.getId());
        }
        return flwTaskOptional.isPresent();
    }

    @Override
    public Optional<FlwTask> executeJumpTask(Long taskId, String nodeKey, FlowCreator flowCreator, Map<String, Object> args, TaskType taskTye) {
        // 执行任务跳转归档
        return taskService().executeJumpTask(taskId, nodeKey, flowCreator, args, flwTask -> {
            FlwInstance flwInstance = this.getFlwInstance(flwTask.getInstanceId(), flowCreator.getCreateBy());
            ProcessModel processModel = runtimeService().getProcessModelByInstanceId(flwInstance.getId());

            // 重新加载流程模型内容
            ModelHelper.reloadProcessModel(flowLongContext, flwInstance.getId(), processModel);

            // 构建节点模型
            Execution execution = new Execution(this, processModel, flowCreator, flwInstance, flwInstance.variableToMap());

            // 传递父节点信息
            execution.setFlwTask(flwTask);
            return execution;
        }, taskTye);
    }

    @Override
    public Optional<FlwTask> executeRejectTask(FlwTask currentFlwTask, String nodeKey, FlowCreator flowCreator, Map<String, Object> args) {

        if (null != nodeKey) {
            // 3，驳回到指定节点
            return this.executeJumpTask(currentFlwTask.getId(), nodeKey, flowCreator, args, TaskType.rejectJump);
        }

        FlwExtInstance extInstance = queryService().getExtInstance(currentFlwTask.getInstanceId());
        ProcessModel processModel = extInstance.model();
        NodeModel nodeModel = processModel.getNode(currentFlwTask.getTaskKey());

        if (Objects.equals(1, nodeModel.getRejectStrategy())) {
            // 驳回策略 1，驳回到发起人
            return this.executeJumpTask(currentFlwTask.getId(), processModel.getNodeConfig().getNodeKey(), flowCreator, args, TaskType.rejectJump);
        }

        // 2，驳回到上一节点
        return taskService().rejectTask(currentFlwTask, flowCreator, args);
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

    /**
     * 创建抄送任务
     * <p>默认不校验是否重复抄送</p>
     *
     * @param taskModel   任务模型
     * @param ccUserList  抄送任务分配到任务的人或角色列表
     * @param flwTask     当前任务
     * @param flowCreator 任务创建者
     */
    @Override
    public boolean createCcTask(NodeModel taskModel, FlwTask flwTask, List<NodeAssignee> ccUserList, FlowCreator flowCreator) {
        return taskService().createCcTask(taskModel, flwTask, ccUserList, flowCreator);
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

        final Long instanceId = flwTask.getInstanceId();
        PerformType performType = PerformType.get(flwTask.getPerformType());
        if (performType == PerformType.countersign) {
            /*
             * 会签未全部完成，不继续执行节点模型
             */
            List<FlwTask> flwTaskList = queryService().getTasksByInstanceIdAndTaskKey(instanceId, flwTask.getTaskKey());
            if (ObjectUtils.isNotEmpty(flwTaskList)) {
                return true;
            }
        }

        /*
         * 流程模型
         */
        final ProcessModel processModel = runtimeService().getProcessModelByInstanceId(instanceId);

        // 当前任务事件
        TaskEventType taskEventType = null;

        /*
         * 驳回跳转，处理重新审批策略
         */
        if (TaskType.rejectJump.eq(flwTask.getTaskType())) {
            // 找到父节点模型处理策略
            FlwHisTask parentTask = queryService().getHistTask(flwTask.getParentTaskId());
            NodeModel parentNodeModel = processModel.getNode(parentTask.getTaskKey());
            if (Objects.equals(2, parentNodeModel.getRejectStart())) {
                // 驳回重新审批策略 2，回到上一个节点
                return this.executeJumpTask(flwTask.getId(), parentTask.getTaskKey(), flowCreator, args, TaskType.reApproveJump).isPresent();
            } else {
                // 驳回重新审批策略 1，继续往下执行
                taskEventType = TaskEventType.reApproveCreate;
            }
        }

        // 当前节点模型
        NodeModel nodeModel = processModel.getNode(flwTask.getTaskKey());

        /*
         * 票签（ 总权重大于 50% 表示通过 ）
         */
        if (performType == PerformType.voteSign) {
            Optional<List<FlwTaskActor>> flwTaskActorsOptional = queryService().getActiveTaskActorsByInstanceId(instanceId);
            if (flwTaskActorsOptional.isPresent()) {
                int passWeight = nodeModel.getPassWeight() == null ? 50 : nodeModel.getPassWeight();
                int votedWeight = 100 - flwTaskActorsOptional.get().stream().mapToInt(t -> t.getWeight() == null ? 0 : t.getWeight()).sum();
                if (votedWeight < passWeight) {
                    // 投票权重小于节点权重继续投票
                    return true;
                } else {
                    // 投票完成关闭投票状态，进入下一个节点
                    Assert.isFalse(taskService().completeActiveTasksByInstanceId(instanceId, flowCreator),
                            "Failed to close voting status");
                }
            }
        }

        // 构建执行对象
        FlwInstance flwInstance = this.getFlwInstance(flwTask.getInstanceId(), flowCreator.getCreateBy());
        final Execution execution = this.createExecution(processModel, flwInstance, flwTask, flowCreator, ObjectUtils.getArgs(args));

        // 设置当前任务事件
        execution.setTaskEventType(taskEventType);

        /*
         * 按顺序依次审批，一个任务按顺序多个参与者依次添加
         */
        if (performType == PerformType.sort) {
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

            // 如果下一个顺序执行人存在，创建顺序审批任务
            NodeAssignee nextNodeAssignee = nodeModel.nextNodeAssignee(execution, assigneeId);
            if (null != nextNodeAssignee) {
                // 参与者类型
                int actorType = execution.getTaskActorProvider().getActorType(nodeModel);
                execution.setNextFlwTaskActor(FlwTaskActor.of(nextNodeAssignee, actorType));
                return flowLongContext.createTask(execution, nodeModel);
            }
        }

        /*
         * 执行触发器任务
         */
        if (performType == PerformType.trigger) {
            return taskService().executeTaskTrigger(execution, flwTask);
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
