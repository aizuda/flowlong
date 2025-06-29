/*
 * Copyright 2023-2025 Licensed under the Dual Licensing
 * website: https://aizuda.com
 */
package com.aizuda.bpm.engine.impl;

import com.aizuda.bpm.engine.*;
import com.aizuda.bpm.engine.assist.Assert;
import com.aizuda.bpm.engine.assist.DateUtils;
import com.aizuda.bpm.engine.assist.ObjectUtils;
import com.aizuda.bpm.engine.core.Execution;
import com.aizuda.bpm.engine.core.FlowCreator;
import com.aizuda.bpm.engine.core.enums.*;
import com.aizuda.bpm.engine.dao.*;
import com.aizuda.bpm.engine.entity.*;
import com.aizuda.bpm.engine.listener.TaskListener;
import com.aizuda.bpm.engine.model.ModelHelper;
import com.aizuda.bpm.engine.model.NodeAssignee;
import com.aizuda.bpm.engine.model.NodeModel;
import com.aizuda.bpm.engine.model.ProcessModel;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * 任务执行业务类
 *
 * <p>
 * <a href="https://aizuda.com">官网</a>尊重知识产权，不允许非法使用，后果自负
 * </p>
 *
 * @author hubin
 * @since 1.0
 */
public class TaskServiceImpl implements TaskService {
    protected final TaskAccessStrategy taskAccessStrategy;
    protected final TaskTrigger taskTrigger;
    protected final TaskListener taskListener;
    protected final FlowLongIdGenerator flowLongIdGenerator;
    protected final FlwInstanceDao instanceDao;
    protected final FlwExtInstanceDao extInstanceDao;
    protected final FlwHisInstanceDao hisInstanceDao;
    protected final FlwTaskDao taskDao;
    protected final FlwTaskActorDao taskActorDao;
    protected final FlwHisTaskDao hisTaskDao;
    protected final FlwHisTaskActorDao hisTaskActorDao;

    public TaskServiceImpl(TaskAccessStrategy taskAccessStrategy, TaskListener taskListener, TaskTrigger taskTrigger, FlowLongIdGenerator flowLongIdGenerator,
                           FlwInstanceDao instanceDao, FlwExtInstanceDao extInstanceDao, FlwHisInstanceDao hisInstanceDao,
                           FlwTaskDao taskDao, FlwTaskActorDao taskActorDao, FlwHisTaskDao hisTaskDao,
                           FlwHisTaskActorDao hisTaskActorDao) {
        this.taskAccessStrategy = taskAccessStrategy;
        this.taskTrigger = taskTrigger;
        this.taskListener = taskListener;
        this.flowLongIdGenerator = flowLongIdGenerator;
        this.instanceDao = instanceDao;
        this.extInstanceDao = extInstanceDao;
        this.hisInstanceDao = hisInstanceDao;
        this.taskDao = taskDao;
        this.taskActorDao = taskActorDao;
        this.hisTaskDao = hisTaskDao;
        this.hisTaskActorDao = hisTaskActorDao;
    }

    /**
     * 更新当前执行节点信息
     *
     * @param flwTask 当前所在执行任务
     */
    protected void updateCurrentNode(FlwTask flwTask) {
        FlwInstance flwInstance = new FlwInstance();
        flwInstance.setId(flwTask.getInstanceId());
        flwInstance.setCurrentNodeName(flwTask.getTaskName());
        flwInstance.setCurrentNodeKey(flwTask.getTaskKey());
        flwInstance.setLastUpdateBy(flwTask.getCreateBy());
        flwInstance.setLastUpdateTime(DateUtils.getCurrentDate());
        instanceDao.updateById(flwInstance);
        FlwHisInstance flwHisInstance = new FlwHisInstance();
        flwHisInstance.setId(flwInstance.getId());
        flwHisInstance.setCurrentNodeName(flwInstance.getCurrentNodeName());
        flwHisInstance.setCurrentNodeKey(flwInstance.getCurrentNodeKey());
        flwHisInstance.setLastUpdateBy(flwInstance.getLastUpdateBy());
        flwHisInstance.setLastUpdateTime(flwInstance.getLastUpdateTime());
        hisInstanceDao.updateById(flwHisInstance);
    }

    /**
     * 执行任务
     *
     * @param taskId      任务ID
     * @param flowCreator 任务创建者
     * @param args        执行参数
     * @param taskState   任务状态
     * @param eventType   执行事件
     * @return {@link FlwTask}
     */
    @Override
    public FlwTask executeTask(Long taskId, FlowCreator flowCreator, Map<String, Object> args, TaskState taskState, TaskEventType eventType) {
        FlwTask flwTask = this.getAllowedFlwTask(taskId, flowCreator, args, taskState);

        // 完成暂存待审任务，设置流程实例状态为审批中
        if (TaskType.saveAsDraft.eq(flwTask.getTaskType())) {
            eventType = TaskEventType.start;
            FlwHisInstance fhi = new FlwHisInstance();
            fhi.setId(flwTask.getInstanceId());
            hisInstanceDao.updateById(fhi.instanceState(InstanceState.active));
        }
        // 重新发起审批
        else if (PerformType.start.eq(flwTask.getPerformType()) && null != flwTask.getParentTaskId()) {
            eventType = TaskEventType.restart;
        }
        // 触发器情况直接移除任务
        else if (PerformType.trigger.eq(flwTask.getPerformType())) {
            taskDao.deleteById(flwTask.getId());
            return flwTask;
        }

        // 迁移任务至历史表
        this.moveToHisTask(flwTask, taskState, flowCreator);

        // 任务监听器通知
        this.taskNotify(eventType, () -> flwTask, null, null, flowCreator);
        return flwTask;
    }

    /**
     * 强制完成所有任务
     */
    @Override
    public boolean forceCompleteAllTask(Long instanceId, FlwTask currentFlwTask, FlowCreator flowCreator, InstanceState instanceState, TaskEventType eventType) {
        List<FlwTask> flwTasks = taskDao.selectListByInstanceId(instanceId);
        if (null != flwTasks) {
            TaskState taskState = TaskState.of(instanceState);
            flwTasks.forEach(t -> {
                if (null != currentFlwTask && Objects.equals(t.getId(), currentFlwTask.getId())) {
                    // 设置新增参数变量
                    t.putAllVariable(currentFlwTask.variableMap());
                }
                this.forceCompleteTask(t, flowCreator, taskState, eventType);
            });
        }
        // 当前任务监听器通知
        if (null != currentFlwTask) {
            this.taskNotify(eventType, () -> currentFlwTask, null, null, flowCreator);
        }
        return true;
    }

    @Override
    public boolean forceCompleteTask(FlwTask flwTask, FlowCreator flowCreator, TaskState taskState, TaskEventType eventType) {
        // 强制完成任务，不通知监听器
        return this.moveToHisTask(flwTask, taskState, flowCreator);
    }

    /**
     * 执行节点跳转任务
     */
    @Override
    public Optional<List<FlwTask>> executeJumpTask(Long taskId, String nodeKey, FlowCreator flowCreator, Map<String, Object> args,
                                             Function<FlwTask, Execution> executionFunction, TaskType taskTye) {
        FlwTask flwTask = null;
        TaskEventType taskEventType = null;
        TaskState taskState = null;
        if (taskTye == TaskType.jump) {
            taskEventType = TaskEventType.jump;
            taskState = TaskState.jump;
        } else if (taskTye == TaskType.rejectJump) {
            taskEventType = TaskEventType.rejectJump;
            taskState = TaskState.rejectJump;
        } else if (taskTye == TaskType.reApproveJump) {
            taskEventType = TaskEventType.reApproveJump;
            taskState = TaskState.reApproveJump;
        } else if (taskTye == TaskType.routeJump) {
            taskEventType = TaskEventType.routeJump;
            taskState = TaskState.routeJump;
        }

        // 驳回重新审批跳转或者路由跳转，当前任务已被执行需查历史
        if (taskTye == TaskType.reApproveJump || taskTye == TaskType.routeJump) {
            // 获取历史任务
            flwTask = hisTaskDao.selectCheckById(taskId);
        }

        Assert.illegal(null == taskEventType, "taskTye only allow jump and rejectJump");
        if (null == flwTask) {
            // 获取当前任务
            flwTask = this.getAllowedFlwTask(taskId, flowCreator, null, null);
        }

        // 执行跳转到目标节点
        Execution execution = executionFunction.apply(flwTask);
        ProcessModel processModel = execution.getProcessModel();
        Assert.isNull(processModel, "当前任务未找到流程定义模型");
        execution.setArgs(args);

        // 查找模型节点
        NodeModel nodeModel;
        if (null == nodeKey) {
            // 1，找到当前节点的父审批节点
            nodeModel = processModel.getNode(flwTask.getTaskKey()).parentApprovalNode();
        } else {
            // 2，找到指定 nodeName 节点
            nodeModel = processModel.getNode(nodeKey);
        }
        Assert.isNull(nodeModel, "根据节点key[" + nodeKey + "]无法找到节点模型");

        // 非发起节点和审批节点不允许跳转
        TaskType taskType = TaskType.get(nodeModel.getType());
        Assert.illegal(TaskType.major != taskType && TaskType.approval != taskType, "not allow jumping nodes");

        // 获取当前执行实例的所有正在执行的任务，强制终止跳到指定节点的所有子节点任务
        List<FlwTask> fts = taskDao.selectListByInstanceId(flwTask.getInstanceId());
        if (ObjectUtils.isNotEmpty(fts)) {
            List<NodeModel> allChildNodes = ModelHelper.getRootNodeAllChildNodes(processModel.getNodeConfig());
            for (FlwTask ft : fts) {
                if (allChildNodes.stream().anyMatch(n -> Objects.equals(n.getNodeKey(), ft.getTaskKey()))) {
                    // 设置执行参数
                    ft.putAllVariable(args);
                    // 归档历史
                    this.moveToHisTask(ft, taskState, flowCreator);

                    // 任务监听器通知
                    this.taskNotify(taskEventType, () -> ft, null, nodeModel, flowCreator);
                }
            }
        }

        List<FlwTask> flwTasks = new ArrayList<>();
        List<FlwTaskActor> taskActors = new ArrayList<>();

        // 设置任务类型为跳转
        FlwTask createTask = this.createTaskBase(nodeModel, execution);
        createTask.taskType(taskTye);
        if (TaskType.major == taskType) {
            // 发起节点，创建发起任务，分配发起人
            createTask.performType(PerformType.start);
            createTask.setId(flowLongIdGenerator.getId(createTask.getId()));
            Assert.isFalse(taskDao.insert(createTask), "failed to create initiation task");
            flwTasks.add(createTask);
            FlwTaskActor fta = FlwTaskActor.ofFlwInstance(execution.getFlwInstance(), createTask.getId());
            fta.setId(flowLongIdGenerator.getId(fta.getId()));
            taskActors.add(fta);
            taskActorDao.insert(fta);
        } else {
            // 模型中获取参与者信息
            taskActors = execution.getTaskActorProvider().getTaskActors(nodeModel, execution);
            // 创建审批人
            PerformType performType = PerformType.get(nodeModel.getExamineMode());
            flwTasks.addAll(this.saveTask(createTask, performType, taskActors, execution, nodeModel));
        }

        // 更新当前节点
        this.updateCurrentNode(createTask);

        // 任务监听器通知
        this.taskNotify(taskEventType, execution::getFlwTask, taskActors, nodeModel, flowCreator);
        return Optional.of(flwTasks);
    }

    /**
     * 获取执行任务并验证合法性
     *
     * @param taskId      任务ID
     * @param flowCreator 任务创建者
     * @param args        执行参数
     * @param taskState   {@link TaskState}
     * @return 流程任务
     */
    protected FlwTask getAllowedFlwTask(Long taskId, FlowCreator flowCreator, Map<String, Object> args, TaskState taskState) {
        FlwTask flwTask = taskDao.selectCheckById(taskId);
        if (null != args) {
            flwTask.putAllVariable(args);
        }
        if (null == taskState || TaskState.allowedCheck(taskState)) {
            Assert.isNull(isAllowed(flwTask, flowCreator.getCreateId()),
                    "当前参与者 [" + flowCreator.getCreateBy() + "]不允许执行任务[taskId=" + taskId + "]");
        }
        return flwTask;
    }

    /**
     * 迁移任务至历史表
     *
     * @param flwTask     执行任务
     * @param taskState   任务状态
     * @param flowCreator 任务创建者
     * @return true 成功 false 失败
     */
    protected boolean moveToHisTask(FlwTask flwTask, TaskState taskState, FlowCreator flowCreator) {
        // 获取当前所有处理人员
        List<FlwTaskActor> taskActors = taskActorDao.selectListByTaskId(flwTask.getId());
        if (taskState != TaskState.autoComplete && taskState != TaskState.autoReject
                && taskState != TaskState.autoJump && ObjectUtils.isEmpty(taskActors)) {
            // 非自动处理，不存在处理人，不再继续执行
            return true;
        }

        // 迁移 task 信息到 flw_his_task
        FlwHisTask hisTask = FlwHisTask.of(flwTask);
        hisTask.setTaskState(taskState);
        hisTask.setFlowCreator(flowCreator);
        hisTask.calculateDuration();

        // 代理人审批
        if (TaskType.agent.eq(flwTask.getTaskType())) {

            // 当前处理人为代理人员
            FlwTaskActor agentFlwTaskActor = taskActors.stream().filter(t -> t.agentActor() && t.eqActorId(flowCreator.getCreateId()))
                    .findFirst().orElse(null);
            if (null != agentFlwTaskActor) {

                // 设置历史代理任务状态为【代理人协办完成的任务】设置被代理人信息
                hisTask.taskType(TaskType.agentAssist);
                taskActors.stream().filter(t -> Objects.equals(agentFlwTaskActor.getAgentId(), t.getActorId()))
                        .findFirst().ifPresent(t -> {
                            hisTask.setAssignorId(t.getActorId());
                            hisTask.setAssignor(t.getActorName());

                            // 更新被代理人信息
                            FlwTaskActor flwTaskActor = FlwTaskActor.ofAgentIt(flowCreator);
                            flwTaskActor.setId(t.getId());
                            taskActorDao.updateById(flwTaskActor);
                        });
                hisTaskDao.insert(hisTask);

                // 迁移任务当前代理人员
                FlwHisTaskActor fht = FlwHisTaskActor.of(agentFlwTaskActor);
                fht.setId(flowLongIdGenerator.getId(fht.getId()));
                hisTaskActorDao.insert(fht);

                // 清理其它代理人
                taskActorDao.deleteByTaskIdAndAgentType(flwTask.getId(), 0);

                // 代理人完成任务，当前任务设置为代理人归还任务，代理人信息变更
                FlwTask newFlwTask = new FlwTask();
                newFlwTask.setId(flwTask.getId());
                newFlwTask.taskType(TaskType.agentReturn);
                newFlwTask.setAssignorId(flowCreator.getCreateId());
                newFlwTask.setAssignor(flowCreator.getCreateBy());
                return taskDao.updateById(newFlwTask);
            } else {

                // 当前处理人员为被代理人，删除代理人员
                List<FlwTaskActor> newFlwTaskActor = new ArrayList<>();
                for (FlwTaskActor taskActor : taskActors) {
                    if (taskActor.agentActor()) {
                        taskActorDao.deleteById(taskActor.getId());
                    } else {
                        newFlwTaskActor.add(taskActor);
                    }
                }
                taskActors = newFlwTaskActor;
                // 设置被代理人自己完成任务
                flwTask.taskType(TaskType.agentOwn);
            }
        }

        // 领导审批，代理人归还任务，回收代理人查看权限
        else if (TaskType.agentReturn.eq(flwTask.getTaskType())) {
            hisTaskActorDao.deleteByTaskId(flwTask.getId());
            hisTaskDao.deleteById(flwTask.getId());

            // 代理人协办完成的任务
            hisTask.taskType(TaskType.agentAssist);
        }

        // 会签情况处理其它任务 排除完成及自动跳过情况，自动跳过是当前任务归档非所有任务
        if (PerformType.countersign.eq(flwTask.getPerformType()) && TaskState.complete.ne(taskState.getValue())
                && TaskState.autoJump.ne(taskState.getValue())) {
            List<FlwTask> flwTaskList = taskDao.selectListByParentTaskId(flwTask.getParentTaskId());
            flwTaskList.forEach(t -> {
                FlwHisTask ht = FlwHisTask.of(t);
                ht.setTaskState(taskState);
                ht.setFlowCreator(flowCreator);
                ht.calculateDuration();
                ht.setTaskType(hisTask.getTaskType());
                hisTaskDao.insert(ht);
            });
            List<Long> taskIds = flwTaskList.stream().map(FlwTask::getId).collect(Collectors.toList());

            // 迁移任务参与者
            this.moveToHisTaskActor(taskActorDao.selectListByTaskIds(taskIds));

            // 删除会签任务
            return taskDao.deleteByIds(taskIds);
        } else if (PerformType.orSign.eq(flwTask.getPerformType())) {
            // 或签情况处理
            for (FlwTaskActor fta : taskActors) {
                if (Objects.equals(flowCreator.getCreateId(), fta.getActorId())) {
                    // 找到审批任务参与者归档
                    taskActors = Collections.singletonList(fta);
                } else {
                    // 移除 flw_task_actor 中 task 参与者信息
                    taskActorDao.deleteById(fta.getId());
                }
            }
        }

        // 迁移任务至历史表
        Assert.isFalse(hisTaskDao.insert(hisTask), "Migration to FlwHisTask table failed");

        // 迁移任务参与者
        this.moveToHisTaskActor(taskActors);

        // 删除 flw_task 中指定 task 信息
        return taskDao.deleteById(flwTask.getId());
    }

    /**
     * 迁移任务参与者至历史表
     *
     * @param taskActors 任务参与者列表
     */
    protected void moveToHisTaskActor(List<FlwTaskActor> taskActors) {
        if (null != taskActors) {
            taskActors.forEach(t -> {
                // 将 task 参与者信息迁移到 flw_his_task_actor
                FlwHisTaskActor fht = FlwHisTaskActor.of(t);
                fht.setId(flowLongIdGenerator.getId(fht.getId()));
                hisTaskActorDao.insert(fht);
                // 移除 flw_task_actor 中 task 参与者信息
                taskActorDao.deleteById(t.getId());
            });
        }
    }

    protected void taskNotify(TaskEventType eventType, Supplier<FlwTask> supplier, List<FlwTaskActor> taskActors,
                              NodeModel nodeModel, FlowCreator flowCreator) {
        if (null != taskListener) {
            taskListener.notify(eventType, supplier, taskActors, nodeModel, flowCreator);
        }
    }

    @Override
    public boolean executeTaskTrigger(Execution execution, FlwTask flwTask) {
        execution.setFlwTask(flwTask);
        NodeModel nodeModel = execution.getProcessModel().getNode(flwTask.getTaskKey());
        Function<Execution, Boolean> finishFunction = (e) -> {

            // 任务监听器通知
            this.taskNotify(TaskEventType.trigger, () -> flwTask, null, nodeModel, execution.getFlowCreator());

            /*
             * 可能存在子节点，存在继续执行
             */
            return nodeModel.nextNode().map(model -> model.execute(execution.getEngine().getContext(), execution))
                    // 不存在子节点，结束流程
                    .orElseGet(() -> execution.endInstance(nodeModel));
        };
        return nodeModel.executeTrigger(execution, () -> {
            // 使用默认触发器
            TaskTrigger taskTrigger = execution.getEngine().getContext().getTaskTrigger();
            if (null == taskTrigger) {
                return false;
            }
            return taskTrigger.execute(nodeModel, execution, finishFunction);
        }, finishFunction);
    }

    /**
     * 完成指定实例ID活动任务
     *
     * @param instanceId 实例ID
     * @return true 成功 false 失败
     */
    @Override
    public boolean completeActiveTasksByInstanceId(Long instanceId, FlowCreator flowCreator) {
        List<FlwTask> flwTasks = taskDao.selectListByInstanceId(instanceId);
        if (ObjectUtils.isNotEmpty(flwTasks)) {
            for (FlwTask flwTask : flwTasks) {
                // 迁移任务至历史表，设置任务状态为终止
                if (!this.moveToHisTask(flwTask, TaskState.terminate, flowCreator)) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * 更新任务对象的 finishTime、createBy、expireTime、version、variable
     *
     * @param flwTask 任务对象
     */
    @Override
    public void updateTaskById(FlwTask flwTask, FlowCreator flowCreator) {
        taskDao.updateById(flwTask);
        // 任务监听器通知
        this.taskNotify(TaskEventType.update, () -> flwTask, null, null, flowCreator);
    }

    /**
     * 查看任务设置为已阅状态
     */
    @Override
    public boolean viewTask(Long taskId, FlowCreator flowCreator) {
        if (taskActorDao.selectCountByTaskIdAndActorId(taskId, flowCreator.getCreateId()) > 0) {
            /*
             * 设置任务为已阅状态
             */
            FlwTask flwTask = new FlwTask();
            flwTask.setId(taskId);
            flwTask.setViewed(1);
            return taskDao.updateById(flwTask);
        }
        return false;
    }

    @Override
    public FlwTask claimRole(Long taskId, FlowCreator flowCreator) {
        return claim(taskId, AgentType.claimRole, TaskEventType.claimRole, flowCreator);
    }

    @Override
    public FlwTask claimDepartment(Long taskId, FlowCreator flowCreator) {
        return claim(taskId, AgentType.claimDepartment, TaskEventType.claimDepartment, flowCreator);
    }

    /**
     * 根据 任务ID 认领任务，删除其它任务参与者
     *
     * @param taskId      任务ID
     * @param agentType   代理人类型
     * @param eventType   流程引擎监听类型
     * @param flowCreator 任务认领者
     * @return Task 任务对象
     */
    protected FlwTask claim(Long taskId, AgentType agentType, TaskEventType eventType, FlowCreator flowCreator) {
        FlwTask flwTask = taskDao.selectCheckById(taskId);
        FlwTaskActor taskActor = this.isAllowed(flwTask, flowCreator.getCreateId());
        if (null == taskActor) {
            Assert.illegal("当前执行用户ID [" + flowCreator.getCreateBy() + "] 不允许认领任务 [taskId=" + taskId + "]");
        }

        // 删除任务参与者
        taskActorDao.deleteById(taskActor.getId());

        FlwTaskActor fta = FlwTaskActor.ofAgent(agentType, flowCreator, flwTask, taskActor);

        // 插入当前用户ID作为唯一参与者
        fta.setId(flowLongIdGenerator.getId(fta.getId()));
        if (taskActorDao.insert(fta)) {
            // 任务监听器通知
            this.taskNotify(eventType, () -> flwTask, Collections.singletonList(fta), null, flowCreator);
        }
        return flwTask;
    }

    @Override
    public boolean transferTask(FlowCreator flowCreator, FlowCreator assigneeFlowCreator) {
        List<FlwTaskActor> taskActors = taskActorDao.selectListByActorId(flowCreator.getCreateId());
        if (ObjectUtils.isEmpty(taskActors)) {
            return false;
        }
        // 遍历处理所有任务
        for (FlwTaskActor taskActor : taskActors) {
            // 设置委托人信息
            FlwTask ft = new FlwTask();
            ft.setId(taskActor.getTaskId());
            ft.taskType(TaskType.transfer);
            ft.setAssignorId(flowCreator.getCreateId());
            ft.setAssignor(flowCreator.getCreateBy());
            if (taskDao.updateById(ft)) {
                // 更新任务参与者为指定用户
                FlwTaskActor fta = new FlwHisTaskActor();
                fta.setId(taskActor.getId());
                fta.setActorId(assigneeFlowCreator.getCreateId());
                fta.setActorName(assigneeFlowCreator.getCreateBy());
                taskActorDao.updateById(fta);
            }
        }
        return true;
    }

    /**
     * 根据 任务ID 分配任务给指定办理人、重置任务类型
     *
     * @param taskId               任务ID
     * @param taskType             任务类型
     * @param flowCreator          任务参与者
     * @param assigneeFlowCreators 指定办理人列表
     * @param args                 任务参数
     * @param check                校验函数，可以根据 dbFlwTask.getAssignorId() 是否存在判断为重发分配
     * @return true 成功 false 失败
     */
    @Override
    public boolean assigneeTask(Long taskId, TaskType taskType, FlowCreator flowCreator, List<FlowCreator> assigneeFlowCreators,
                                Map<String, Object> args, Function<FlwTask, Boolean> check) {
        // 受理任务权限验证
        FlwTaskActor flwTaskActor = this.getAllowedFlwTaskActor(taskId, flowCreator);

        // 不允许重复分配
        FlwTask dbFlwTask = taskDao.selectById(taskId);
        if (null != check && !check.apply(dbFlwTask)) {
            return false;
        }

        List<FlwTaskActor> taskActors = new ArrayList<>();

        // 设置任务为委派任务或者为转办任务
        FlwTask flwTask = new FlwTask();
        flwTask.setId(dbFlwTask.getId());
        flwTask.taskType(taskType);

        // 设置任务参数，如果有参数则设置到任务参数中
        if (null != args) {
            flwTask.setVariable(dbFlwTask.getVariable());
            flwTask.putAllVariable(args);
        }

        if (taskType == TaskType.agent) {
            // 设置代理人员信息，第一个人为主办 assignorId 其他人为协办 assignor 多个英文逗号分隔
            FlowCreator afc = assigneeFlowCreators.get(0);
            flwTask.setAssignorId(afc.getCreateId());
            flwTask.setAssignor(assigneeFlowCreators.stream().map(FlowCreator::getCreateBy).collect(Collectors.joining(", ")));
            // 分配代理人可见代理任务
            assigneeFlowCreators.forEach(t -> {
                FlwTaskActor fta = FlwTaskActor.ofAgent(AgentType.agent, t, dbFlwTask, flwTaskActor);
                fta.setId(flowLongIdGenerator.getId(fta.getId()));
                taskActors.add(fta);
                taskActorDao.insert(fta);
            });
        } else {
            if (null == dbFlwTask.getAssignorId()) {
                // 设置委托人信息
                flwTask.setAssignorId(flowCreator.getCreateId());
                flwTask.setAssignor(flowCreator.getCreateBy());
            }

            // 删除任务历史参与者
            taskActorDao.deleteById(flwTaskActor.getId());

            // 分配任务给办理人
            FlwTaskActor fta = FlwTaskActor.ofFlowCreator(assigneeFlowCreators.get(0));
            taskActors.add(fta);
            this.assignTask(flwTaskActor.getInstanceId(), taskId, flwTaskActor.getActorType(), fta);
        }

        // 更新任务
        taskDao.updateById(flwTask);

        // 任务监听器通知
        this.taskNotify(TaskEventType.assignment, () -> {
            dbFlwTask.taskType(taskType);
            dbFlwTask.setAssignorId(flwTask.getAssignorId());
            dbFlwTask.setAssignor(flwTask.getAssignor());
            return dbFlwTask;
        }, taskActors, null, flowCreator);
        return true;
    }

    /**
     * 获取指定 任务ID 合法参与者对象
     *
     * @param taskId      任务ID
     * @param flowCreator 任务参与者
     * @return 任务参与者
     */
    protected FlwTaskActor getAllowedFlwTaskActor(Long taskId, FlowCreator flowCreator) {
        List<FlwTaskActor> taskActors = taskActorDao.selectListByTaskIdAndActorId(taskId, flowCreator.getCreateId());
        return taskAccessStrategy.getAllowedFlwTaskActor(taskId, flowCreator, taskActors);
    }

    /**
     * 根据 任务ID 解决委派任务
     *
     * @param taskId      任务ID
     * @param flowCreator 任务参与者
     * @return true 成功 false 失败
     */
    @Override
    public boolean resolveTask(Long taskId, FlowCreator flowCreator) {
        // 解决任务权限验证
        FlwTaskActor taskActor = this.getAllowedFlwTaskActor(taskId, flowCreator);

        // 当前委托任务
        FlwTask flwTask = taskDao.selectCheckById(taskId);

        // 任务归还至委托人
        FlwTaskActor fta = new FlwHisTaskActor();
        fta.setId(taskActor.getId());
        fta.setActorId(flwTask.getAssignorId());
        fta.setActorName(flwTask.getAssignor());
        if (taskActorDao.updateById(fta)) {
            // 设置任务状态为委托归还，委托人设置为归还人
            FlwTask temp = new FlwTask();
            temp.setId(taskId);
            temp.taskType(TaskType.delegateReturn);
            temp.setAssignorId(flowCreator.getCreateId());
            temp.setAssignor(flowCreator.getCreateBy());
            Assert.isFalse(taskDao.updateById(temp), "resolveTask failed");

            // 任务监听器通知
            this.taskNotify(TaskEventType.delegateResolve, () -> {
                flwTask.setTaskType(temp.getTaskType());
                flwTask.setAssignorId(temp.getCreateId());
                flwTask.setAssignor(temp.getCreateBy());
                return flwTask;
            }, Collections.singletonList(fta), null, flowCreator);
        }
        return true;
    }

    /**
     * 拿回任务、根据历史任务ID撤回下一个节点的任务、恢复历史任务
     */
    @Override
    public Optional<List<FlwTask>> reclaimTask(Long taskId, FlowCreator flowCreator) {

        // 下面执行撤回逻辑
        Optional<List<FlwTask>> flwTasksOptional = this.undoHisTask(taskId, flowCreator, TaskType.reclaim, hisTask -> {
            boolean checkReclaim = true;
            // 顺序签或会签情况，判断存在未执行并行任务不检查允许拿回
            if (PerformType.sort.eq(hisTask.getPerformType()) || PerformType.countersign.eq(hisTask.getPerformType())) {
                checkReclaim = taskDao.selectCountByParentTaskId(hisTask.getParentTaskId()) < 1;
            }
            if (checkReclaim) {
                // 当前任务子任务已经执行完成不允许撤回
                Assert.isTrue(taskDao.selectCountByParentTaskId(taskId) == 0, "Do not allow reclaim task");
            }

            List<FlwTask> flwTaskList = taskDao.selectListByInstanceId(hisTask.getInstanceId());
            Assert.isEmpty(flwTaskList, "No approval tasks found");
            FlwTask existFlwTask = flwTaskList.get(0);
            if (!PerformType.countersign.eq(existFlwTask.getPerformType())) {
                // 非会签情况
                Assert.isFalse(Objects.equals(existFlwTask.getParentTaskId(), taskId), "Do not allow cross level reclaim task");
            }
            flwTaskList.forEach(flwTask -> this.moveToHisTask(flwTask, TaskState.revoke, flowCreator));
        });

        // 任务监听器通知
        flwTasksOptional.ifPresent(fts -> fts.forEach(ft -> this.taskNotify(TaskEventType.reclaim, () -> ft, null, null, flowCreator)));
        return flwTasksOptional;
    }

    /**
     * 唤醒撤回或拒绝终止历史任务
     */
    @Override
    public boolean resume(Long instanceId, String nodeKey, FlowCreator flowCreator) {
        FlwHisInstance fhi = hisInstanceDao.selectById(instanceId);
        if (null == fhi || !Objects.equals(fhi.getCreateBy(), flowCreator.getCreateBy()) ||
                (InstanceState.reject.ne(fhi.getInstanceState()) && InstanceState.revoke.ne(fhi.getInstanceState()))) {
            return false;
        }

        // 恢复当前实例
        if (instanceDao.insert(fhi.toFlwInstance())) {
            // 重置历史实例为激活状态
            FlwHisInstance temp = new FlwHisInstance();
            temp.setId(instanceId);
            hisInstanceDao.updateById(temp.instanceState(InstanceState.active));
        }

        Consumer<FlwHisTask> fhtConsumer = hisTask -> {
            // 历史任务恢复
            FlwTask flwTask = hisTask.cloneTask(null);
            flwTask.setId(flowLongIdGenerator.getId(flwTask.getId()));
            if (taskDao.insert(flwTask)) {
                // 历史任务参与者恢复
                List<FlwTaskActor> taskActors = new ArrayList<>();
                List<FlwHisTaskActor> hisTaskActors = hisTaskActorDao.selectListByTaskId(hisTask.getId());
                hisTaskActors.forEach(t -> {
                    FlwTaskActor fta = FlwTaskActor.ofFlwHisTaskActor(flwTask.getId(), t);
                    fta.setId(flowLongIdGenerator.getId(fta.getId()));
                    if (taskActorDao.insert(fta)) {
                        taskActors.add(fta);
                    }
                });

                // 任务监听器通知
                this.taskNotify(TaskEventType.resume, () -> flwTask, taskActors, null, flowCreator);
            }
        };

        if (null != nodeKey) {
            // 恢复指定节点key历史任务
            hisTaskDao.selectListByInstanceIdAndTaskKey(instanceId, nodeKey).ifPresent(hisTasks -> {
                if (hisTasks.size() > 1) {
                    // 获取最近执行的指定节点历史任务
                    List<FlwHisTask> lastFhtList = hisTaskDao.selectListByParentTaskId(hisTasks.get(0).getParentTaskId());
                    if (ObjectUtils.isNotEmpty(lastFhtList)) {
                        lastFhtList.forEach(fhtConsumer);
                    }
                } else {
                    hisTasks.forEach(fhtConsumer);
                }
            });
        } else {
            // 恢复历史任务
            TaskState taskState = TaskState.rejectEnd;
            if (InstanceState.revoke.eq(fhi.getInstanceState())) {
                taskState = TaskState.revoke;
            }
            hisTaskDao.selectListByInstanceIdAndTaskState(instanceId, taskState.getValue())
                    .ifPresent(hisTasks -> hisTasks.forEach(fhtConsumer));
        }
        return true;
    }

    /**
     * 撤回指定的任务
     */
    @Override
    public Optional<List<FlwTask>> withdrawTask(Long taskId, FlowCreator flowCreator) {
        return this.undoHisTask(taskId, flowCreator, TaskType.withdraw, hisTask -> {
            List<FlwTask> flwTasks = null;
            PerformType performType = PerformType.get(hisTask.getPerformType());
            if (performType == PerformType.countersign) {
                // 根据父任务ID查询所有子任务
                flwTasks = taskDao.selectListByParentTaskId(hisTask.getId());
            } else {
                List<Long> hisTaskIds = hisTaskDao.selectListByInstanceIdAndTaskNameAndParentTaskId(hisTask.getInstanceId(),
                        hisTask.getTaskName(), hisTask.getParentTaskId()).stream().map(FlwHisTask::getId).collect(Collectors.toList());
                if (ObjectUtils.isNotEmpty(hisTaskIds)) {
                    flwTasks = taskDao.selectListByParentTaskIds(hisTaskIds);
                }
            }
            if (ObjectUtils.isEmpty(flwTasks)) {
                flwTasks = taskDao.selectListByInstanceId(hisTask.getInstanceId());
                // 设置为 0 执行撤回到发起人逻辑
                hisTask.setParentTaskId(0L);
            }
            if (ObjectUtils.isNotEmpty(flwTasks)) {
                List<Long> taskIds = flwTasks.stream().map(FlowEntity::getId).collect(Collectors.toList());
                // 查询任务参与者
                List<Long> taskActorIds = taskActorDao.selectListByTaskIds(taskIds)
                        .stream().map(FlwTaskActor::getId).collect(Collectors.toList());
                if (ObjectUtils.isNotEmpty(taskActorIds)) {
                    taskActorDao.deleteByIds(taskActorIds);
                }
                taskDao.deleteByIds(flwTasks.stream().map(FlowEntity::getId).collect(Collectors.toList()));

                // 任务监听器通知
                this.taskNotify(TaskEventType.withdraw, () -> hisTask, null, null, flowCreator);
            }
        });
    }

    @Override
    public Optional<List<FlwTask>> rejectTask(FlwTask currentFlwTask, FlowCreator flowCreator, Map<String, Object> args) {
        Assert.isTrue(currentFlwTask.startNode(), "上一步任务ID为空，无法驳回至上一步处理");
        final Long instanceId = currentFlwTask.getInstanceId();

        // 执行任务驳回
        this.executeTask(currentFlwTask.getId(), flowCreator, args, TaskState.reject, TaskEventType.reject);

        // 或签 结束其它任务
        final Integer performType = currentFlwTask.getPerformType();
        if (PerformType.countersign.eq(performType) || PerformType.orSign.eq(performType)
                || PerformType.voteSign.eq(performType)) {
            List<FlwTask> flwTasks = taskDao.selectListByParentTaskId(currentFlwTask.getParentTaskId());
            if (null != flwTasks && !flwTasks.isEmpty()) {
                // 删除其它任务及参与者信息
                taskActorDao.deleteByInstanceIdAndTaskIds(instanceId, flwTasks.stream().map(FlowEntity::getId).collect(Collectors.toList()));
                taskDao.deleteByInstanceIdAndParentTaskId(instanceId, currentFlwTask.getParentTaskId());
            }
        }

        // 处理并行分支，包容分支情况（当前实例中获取模型）
        FlwExtInstance flwExtInstance = extInstanceDao.selectById(instanceId);
        ProcessModel processModel = flwExtInstance.model();
        NodeModel currentNodeModel = processModel.getNode(currentFlwTask.getTaskKey());
        NodeModel parentNode = currentNodeModel.getParentNode();
        if (parentNode.parallelNode() || parentNode.inclusiveNode()) {
            List<String> allNextNodeKeys = ModelHelper.getAllNextConditionNodeKeys(parentNode.getParentNode());
            List<Long> flwTaskIds = taskDao.selectListByInstanceId(instanceId).stream()
                    .filter(t -> allNextNodeKeys.contains(t.getTaskKey()) && !Objects.equals(t.getId(), currentFlwTask.getId()))
                    .map(FlowEntity::getId).collect(Collectors.toList());
            if (!flwTaskIds.isEmpty()) {
                taskActorDao.deleteByInstanceIdAndTaskIds(instanceId, flwTaskIds);
                taskDao.deleteByIds(flwTaskIds);
            }
        }


        // 撤回至上一级任务
        Long parentTaskId = currentFlwTask.getParentTaskId();
        Optional<List<FlwTask>> flwTasksOptional = this.undoHisTask(parentTaskId, flowCreator, TaskType.reject, null);

        // 任务监听器通知
        flwTasksOptional.ifPresent(fts -> fts.forEach(ft -> this.taskNotify(TaskEventType.recreate, () -> ft, null, null, flowCreator)));
        return flwTasksOptional;
    }

    /**
     * 撤回历史任务
     *
     * @param hisTaskId       历史任务ID
     * @param flowCreator     任务创建者
     * @param taskType        任务类型
     * @param hisTaskConsumer 历史任务业务处理
     * @return 任务参与者
     */
    protected Optional<List<FlwTask>> undoHisTask(Long hisTaskId, FlowCreator flowCreator, TaskType taskType,
                                            Consumer<FlwHisTask> hisTaskConsumer) {
        Optional<List<FlwTask>> flwTasksOptional = Optional.empty();
        FlwHisTask hisTask = hisTaskDao.selectCheckById(hisTaskId);
        if (null == hisTask || hisTask.startNode()) {
            // 任务不存在、发起节点撤回，直接返回
            return flwTasksOptional;
        }

        // 回调处理函数
        if (null != hisTaskConsumer) {
            hisTaskConsumer.accept(hisTask);
        }

        // 撤回历史任务
        if (hisTask.startNode()) {
            // 如果直接撤回到发起人，构建发起人关联信息
            FlwTask flwTask = hisTask.undoTask(taskType);
            flwTask.setId(flowLongIdGenerator.getId(flwTask.getId()));
            if (taskDao.insert(flwTask)) {
                flwTasksOptional = Optional.of(Collections.singletonList(flwTask));
                FlwTaskActor fta = FlwTaskActor.ofFlwTask(flwTask);
                fta.setId(flowLongIdGenerator.getId(fta.getId()));
                taskActorDao.insert(fta);
            }
        } else {
            if (PerformType.countersign.eq(hisTask.getPerformType())) {
                // 会签任务需要撤回所有子任务
                List<FlwHisTask> hisTasks = hisTaskDao.selectListByParentTaskId(hisTask.getParentTaskId());

                // 撤回任务参与者
                List<FlwHisTaskActor> hisTaskActors = hisTaskActorDao.selectListByTaskIds(hisTasks.stream()
                        .map(FlwHisTask::getId).collect(Collectors.toList()));
                if (null != hisTaskActors) {
                    Map<String, FlwHisTaskActor> taskActorMap = new HashMap<>();
                    for (FlwHisTaskActor t : hisTaskActors) {
                        FlwHisTaskActor t1 = taskActorMap.get(t.getActorId());
                        if (null == t1 || t.getTaskId() > t1.getTaskId()) {
                            // 同一个任务参与者，获取最新的任务
                            taskActorMap.put(t.getActorId(), t);
                        }
                    }

                    // 恢复最新历史任务
                    List<FlwTask> flwTasks = new ArrayList<>();
                    taskActorMap.forEach((k, v) -> hisTasks.stream().filter(t -> Objects.equals(t.getId(), v.getTaskId()))
                            .findFirst().ifPresent(t -> {
                                FlwTask flwTask = t.undoTask(taskType);
                                flwTask.setId(flowLongIdGenerator.getId(flwTask.getId()));
                                if (taskDao.insert(flwTask)) {
                                    flwTasks.add(flwTask);
                                    FlwTaskActor fta = FlwTaskActor.of(flwTask.getId(), v);
                                    fta.setId(flowLongIdGenerator.getId(fta.getId()));
                                    taskActorDao.insert(fta);
                                }
                            }));
                    flwTasksOptional = Optional.of(flwTasks);
                }
            } else {
                // 恢复历史任务
                FlwTask flwTask = hisTask.undoTask(taskType);
                flwTask.setId(flowLongIdGenerator.getId(flwTask.getId()));
                if (taskDao.insert(flwTask)) {
                    flwTasksOptional = Optional.of(Collections.singletonList(flwTask));
                    // 撤回任务参与者
                    List<FlwHisTaskActor> hisTaskActors = hisTaskActorDao.selectListByTaskId(hisTask.getId());
                    if (null != hisTaskActors) {
                        hisTaskActors.forEach(t -> {
                            FlwTaskActor fta = FlwTaskActor.of(flwTask.getId(), t);
                            fta.setId(flowLongIdGenerator.getId(fta.getId()));
                            taskActorDao.insert(fta);
                        });
                    }
                }
            }
        }

        // 更新当前执行节点信息
        this.updateCurrentNode(hisTask);
        return flwTasksOptional;
    }

    /**
     * 对指定的任务分配参与者。参与者可以为用户、部门、角色
     *
     * @param instanceId 实例ID
     * @param taskId     任务ID
     * @param actorType  参与者类型 0，用户 1，角色 2，部门
     * @param taskActor  任务参与者
     */
    protected void assignTask(Long instanceId, Long taskId, int actorType, FlwTaskActor taskActor) {
        taskActor.setId(flowLongIdGenerator.getId(taskActor.getId()));
        taskActor.setInstanceId(instanceId);
        taskActor.setTaskId(taskId);
        taskActor.setActorType(actorType);
        taskActorDao.insert(taskActor);
    }

    /**
     * 根据已有任务、参与者创建新的任务
     * <p>
     * 适用于动态转派，动态协办等处理且流程图中不体现节点情况
     * </p>
     */
    @Override
    public List<FlwTask> createNewTask(Long taskId, TaskType taskType, PerformType performType, List<FlwTaskActor> taskActors,
                                       FlowCreator flowCreator, Function<FlwTask, Execution> executionFunction) {
        FlwTask flwTask = taskDao.selectCheckById(taskId);
        FlwTask newFlwTask = flwTask.cloneTask(flowCreator.getCreateId(), flowCreator.getCreateBy());
        newFlwTask.taskType(taskType);
        newFlwTask.performType(performType);
        newFlwTask.setParentTaskId(taskId);
        Execution execution = executionFunction.apply(newFlwTask);
        execution.setFlowCreator(flowCreator);
        return this.saveTask(newFlwTask, performType, taskActors, execution, null);
    }

    /**
     * 创建抄送任务
     * <p>默认不校验是否重复抄送</p>
     *
     * @param taskModel   任务模型
     * @param flwTask     当前任务
     * @param flowCreator 任务创建者
     */
    @Override
    public boolean createCcTask(NodeModel taskModel, FlwTask flwTask, List<NodeAssignee> ccUserList, FlowCreator flowCreator) {
        if (ObjectUtils.isEmpty(ccUserList)) {
            return false;
        }

        TaskEventType eventType = TaskEventType.cc;
        FlwTask newFlwTask;
        if (TaskType.cc.eq(taskModel.getType())) {
            // 抄送任务
            newFlwTask = flwTask;
        } else {
            // 非抄送任务手动创建抄送，需要克隆当前任务
            eventType = TaskEventType.createCc;
            newFlwTask = flwTask.cloneTask(flowCreator.getCreateId(), flowCreator.getCreateBy());
        }
        newFlwTask.setId(flowLongIdGenerator.getId(newFlwTask.getId()));
        taskDao.insert(newFlwTask);

        // 抄送历史任务
        FlwHisTask fht = FlwHisTask.of(newFlwTask, TaskState.complete);
        fht.taskType(TaskType.cc);
        fht.performType(PerformType.copy);
        fht.calculateDuration();
        fht.setId(flowLongIdGenerator.getId(fht.getId()));
        hisTaskDao.insert(fht);

        // 即刻归档，确保自增ID情况一致性
        taskDao.deleteById(newFlwTask.getId());

        // 历史任务参与者数据入库
        List<FlwTaskActor> htaList = new ArrayList<>();
        for (NodeAssignee nodeUser : ccUserList) {
            FlwHisTaskActor hta = FlwHisTaskActor.ofNodeAssignee(nodeUser, fht.getInstanceId(), fht.getId());
            hta.setId(flowLongIdGenerator.getId(hta.getId()));
            hta.setWeight(6);
            if (hisTaskActorDao.insert(hta)) {
                htaList.add(hta);
            }
        }

        // 任务监听器通知
        this.taskNotify(eventType, () -> fht, htaList, taskModel, flowCreator);
        return true;
    }

    /**
     * 获取超时或者提醒的任务
     *
     * @return 任务列表
     */
    @Override
    public List<FlwTask> getTimeoutOrRemindTasks() {
        return taskDao.selectListTimeoutOrRemindTasks(DateUtils.getCurrentDate());
    }

    /**
     * 获取任务模型
     *
     * @param taskId 任务ID
     * @return 节点模型
     */
    @Override
    public NodeModel getTaskModel(Long taskId) {
        FlwTask flwTask = hisTaskDao.selectById(taskId);
        if (null == flwTask) {
            flwTask = taskDao.selectCheckById(taskId);
        }
        FlwExtInstance extInstance = extInstanceDao.selectById(flwTask.getInstanceId());
        ProcessModel model = extInstance.model();
        NodeModel nodeModel = model.getNode(flwTask.getTaskKey());
        if (null == nodeModel) {
            Assert.illegal("Cannot find NodeModel. taskId = " + taskId);
        }
        return nodeModel;
    }

    /**
     * 创建 task 根据 model 决定是否分配参与者
     *
     * @param nodeModel 节点模型
     * @param execution 执行对象
     * @return 任务列表
     */
    @Override
    public List<FlwTask> createTask(NodeModel nodeModel, Execution execution, Function<FlwTask, FlwTask> taskFunction) {
        // 构建任务
        FlwTask flwTask = this.createTaskBase(nodeModel, execution);

        // 任务处理函数
        if (null != taskFunction) {
            flwTask = taskFunction.apply(flwTask);
        }

        // 模型中获取参与者信息
        List<FlwTaskActor> taskActors = execution.getTaskActorProvider().getTaskActors(nodeModel, execution);
        List<FlwTask> flwTasks = new ArrayList<>();

        // 处理流程任务
        Integer nodeType = nodeModel.getType();

        // 更新当前执行节点信息，抄送节点除外
        if (!TaskType.cc.eq(nodeType)) {
            this.updateCurrentNode(flwTask);
        }

        if (TaskType.major.eq(nodeType)) {
            /*
             * 0，发起人（ 直接保存历史任务、执行进入下一个节点逻辑 ）
             */
            flwTasks.addAll(this.saveTask(flwTask, PerformType.start, taskActors, execution, nodeModel));

            /*
             * 非暂存草稿，执行进入下一个节点
             */
            if (!execution.isSaveAsDraft()) {
                nodeModel.nextNode().ifPresent(nextNode -> nextNode.execute(execution.getEngine().getContext(), execution));
            }
        } else if (TaskType.approval.eq(nodeType)) {
            /*
             * 1，审批人
             */
            PerformType performType = PerformType.get(nodeModel.getExamineMode());
            flwTasks.addAll(this.saveTask(flwTask, performType, taskActors, execution, nodeModel));
        } else if (TaskType.cc.eq(nodeType)) {
            /*
             * 2，抄送任务
             */
            this.createCcTask(nodeModel, flwTask, nodeModel.getNodeAssigneeList(), execution.getFlowCreator());

            /*
             * 可能存在子节点
             */
            Optional<NodeModel> nextNodeOptional = nodeModel.nextNode();
            if (nextNodeOptional.isPresent()) {
                // 下一个节点如果在并行分支，判断是否并行分支都执行结束
                boolean _exec = true;
                NodeModel ccNextNode = nextNodeOptional.get();
                NodeModel _cnn = execution.getProcessModel().getNode(ccNextNode.getNodeKey());
                if (_cnn.getParentNode().parallelNode()) {
                    // 抄送节点独立占据一个分支或者存在执行任务
                    if (ccNextNode.getParentNode().parallelNode() || taskDao.selectCountByInstanceId(flwTask.getInstanceId()) > 0) {
                        _exec = false;
                    }
                }
                if (_exec) {
                    // 执行下一个节点
                    ccNextNode.execute(execution.getEngine().getContext(), execution);
                }
            } else {
                // 不存在任何子节点结束流程
                execution.endInstance(nodeModel);
            }
        } else if (TaskType.conditionNode.eq(nodeType)) {
            /*
             * 3，条件审批
             */
            FlwTask singleFlwTask = flwTask.cloneTask(null);
            PerformType performType = PerformType.get(nodeModel.getExamineMode());
            flwTasks.addAll(this.saveTask(singleFlwTask, performType, taskActors, execution, nodeModel));
        } else if (TaskType.callProcess.eq(nodeType)) {
            /*
             * 5，办理子流程
             */
            FlowCreator flowCreator = execution.getFlowCreator();
            String callProcess = nodeModel.getCallProcess();
            Assert.isEmpty(callProcess, "The execution parameter callProcess does not exist");
            String[] callProcessArr = callProcess.split(":");
            ProcessService processService = execution.getEngine().processService();
            FlwProcess flwProcess;
            if (Objects.equals(2, callProcessArr.length)) {
                flwProcess = processService.getProcessById(Long.valueOf(callProcessArr[0]));
            } else {
                flwProcess = processService.getProcessByKey(flowCreator.getTenantId(), callProcessArr[0]);
            }
            if (null == flwProcess) {
                Assert.illegal("No found flwProcess, callProcess=" + callProcess);
            }
            // 启动子流程，任务归档历史
            final long instanceId = flwTask.getInstanceId();
            execution.getEngine().startProcessInstance(flwProcess, flowCreator, null, false, () -> {
                FlwInstance flwInstance = new FlwInstance();
                if (nodeModel.callAsync()) {
                    // 设置优先级为 1 异步子流程
                    flwInstance.priority(InstancePriority.async);
                }
                flwInstance.setCurrentNodeKey(nodeModel.getNodeKey());
                flwInstance.setParentInstanceId(instanceId);
                return flwInstance;
            }).ifPresent(instance -> {
                // 归档历史
                FlwHisTask flwHisTask = FlwHisTask.ofCallInstance(nodeModel, instance);
                flwHisTask.setId(flowLongIdGenerator.getId(flwHisTask.getId()));
                if (hisTaskDao.insert(flwHisTask)) {
                    // 追加子流程实例ID
                    nodeModel.setCallProcess(nodeModel.getCallProcess() + ":" + instance.getId());
                    // 主流程监听
                    this.taskNotify(TaskEventType.callProcess, () -> flwHisTask, null, nodeModel, flowCreator);
                }
            });

            // 如果是异步调用，继续执行后续逻辑
            if (nodeModel.callAsync()) {
                nodeModel.nextNode().ifPresent(t -> t.execute(execution.getEngine().getContext(), execution));
            }
        } else if (TaskType.timer.eq(nodeType)) {
            /*
             * 6，定时器任务
             */
            flwTask.loadExpireTime(nodeModel.getExtendConfig(), true);
            flwTasks.addAll(this.saveTask(flwTask, PerformType.timer, taskActors, execution, nodeModel));
        } else if (TaskType.trigger.eq(nodeType)) {
            /*
             * 7、触发器任务
             */
            flwTask.loadExpireTime(nodeModel.getExtendConfig(), false);
            if (null == flwTask.getExpireTime()) {
                // 立即触发器，直接执行
                execution.setFlwTask(flwTask);
                // 使用默认触发器
                Function<Execution, Boolean> finishFunction = (e) -> this.executeFinishTrigger(nodeModel, execution, execution.getFlowCreator());
                nodeModel.executeTrigger(execution, () -> taskTrigger.execute(nodeModel, execution, finishFunction), finishFunction);
            } else {
                // 定时触发器，等待执行
                flwTasks.addAll(this.saveTask(flwTask, PerformType.trigger, taskActors, execution, nodeModel));
            }
        }

        return flwTasks;
    }

    public boolean executeFinishTrigger(NodeModel nodeModel, Execution execution, FlowCreator flowCreator) {
        if (execution.isSaveAsDraft()) {
            // 触发器暂存草稿
            execution.setFlwTasks(this.saveTask(execution.getFlwTask(), PerformType.trigger, null, execution, nodeModel));
            return true;
        }

        // 构建触发器历史任务
        FlwHisTask hisTask = FlwHisTask.of(execution.getFlwTask());
        hisTask.setTaskState(TaskState.complete);
        hisTask.setFlowCreator(flowCreator);
        hisTask.calculateDuration();
        hisTask.setId(flowLongIdGenerator.getId(hisTask.getId()));
        hisTaskDao.insert(hisTask);

        // 任务监听器通知
        this.taskNotify(TaskEventType.trigger, () -> hisTask, null, nodeModel, flowCreator);

        /*
         * 可能存在子节点
         */
        nodeModel.nextNode().ifPresent(nextNode -> nextNode.execute(execution.getEngine().getContext(), execution));
        return true;
    }

    /**
     * 根据模型、执行对象、任务类型构建基本的task对象
     *
     * @param nodeModel 节点模型
     * @param execution 执行对象
     * @return Task任务对象
     */
    protected FlwTask createTaskBase(NodeModel nodeModel, Execution execution) {
        FlwTask flwTask = new FlwTask();
        flwTask.setFlowCreator(execution.getFlowCreator());
        flwTask.setCreateTime(DateUtils.getCurrentDate());
        flwTask.setInstanceId(execution.getFlwInstance().getId());
        flwTask.setTaskName(nodeModel.getNodeName());
        flwTask.setTaskKey(nodeModel.getNodeKey());
        flwTask.setTaskType(nodeModel.getType());
        flwTask.setPerformType(nodeModel.getExamineMode());
        flwTask.setActionUrl(nodeModel.getActionUrl());
        // 触发器 父任务ID flwTask 不为 null 但 getFlwTask().getId() == null
        FlwTask executionTask = execution.getFlwTask();
        if (null == executionTask || null == executionTask.getId()) {
            flwTask.setParentTaskId(0L);
        } else {
            flwTask.setParentTaskId(executionTask.getId());
        }
        Map<String, Object> args = execution.getArgs();
        // 审批期限非空，设置期望任务完成时间
        Integer term = nodeModel.getTerm();
        if (null != term && term > 0) {
            flwTask.setExpireTime(DateUtils.toDate(DateUtils.now().plusHours(term)));
            if (null == args) {
                args = new HashMap<>();
            }
            args.put("termMode", nodeModel.getTermMode());
        }
        flwTask.putAllVariable(args);
        flwTask.setRemindRepeat(0);
        flwTask.setViewed(0);
        return flwTask;
    }

    /**
     * 保存任务及参与者信息
     *
     * @param flwTask     流程任务对象
     * @param performType 参与类型 {@link PerformType}
     * @param taskActors  参与者ID集合
     * @param execution   流程执行处理类 {@link Execution}
     * @param nodeModel   流程节点模型对象 {@link NodeModel}
     * @return 流程任务列表
     */
    protected List<FlwTask> saveTask(FlwTask flwTask, PerformType performType, List<FlwTaskActor> taskActors, Execution execution, NodeModel nodeModel) {
        List<FlwTask> flwTasks = new ArrayList<>();
        flwTask.setId(flowLongIdGenerator.getId(flwTask.getId()));
        flwTask.performType(performType);
        final FlowCreator flowCreator = execution.getFlowCreator();

        if (performType == PerformType.timer || performType == PerformType.trigger) {
            // 定时器任务，触发器任务
            taskDao.insert(flwTask);
            flwTasks.add(flwTask);
            return flwTasks;
        }

        if (performType == PerformType.start) {
            TaskEventType taskEventType;
            // 暂存草稿
            if (execution.isSaveAsDraft()) {
                taskEventType = TaskEventType.startAsDraft;
                // 暂存待审
                flwTask.taskType(TaskType.saveAsDraft);
                taskDao.insert(flwTask);
                // 设置为执行任务
                execution.setFlwTask(flwTask);
                // 记录发起人
                FlwTaskActor fht = FlwTaskActor.ofFlwTask(flwTask);
                fht.setId(flowLongIdGenerator.getId(fht.getId()));
                taskActorDao.insert(fht);
            } else {
                taskEventType = TaskEventType.start;
                // 发起任务
                taskDao.insert(flwTask);
                // 创建历史任务
                FlwHisTask flwHisTask = FlwHisTask.of(flwTask, TaskState.complete);
                flwHisTask.calculateDuration();
                if (hisTaskDao.insert(flwHisTask)) {
                    // 设置为执行任务
                    execution.setFlwTask(flwHisTask);
                    // 即刻归档，确保自增ID情况一致性
                    taskDao.deleteById(flwTask.getId());
                    // 记录发起人
                    FlwHisTaskActor fht = FlwHisTaskActor.ofFlwHisTask(flwHisTask);
                    fht.setId(flowLongIdGenerator.getId(fht.getId()));
                    hisTaskActorDao.insert(fht);
                    flwTask.setId(flwHisTask.getId());
                }
            }
            // 添加返回任务
            flwTasks.add(flwTask);
            // 创建任务监听
            this.taskNotify(taskEventType, () -> flwTask, taskActors, nodeModel, flowCreator);
            return flwTasks;
        }

        if (ObjectUtils.isEmpty(taskActors)) {
            // 非正常创建任务处理逻辑
            if (execution.getTaskActorProvider().abnormal(flwTask, performType, taskActors, execution, nodeModel)) {
                // 返回 true 继续执行
                return flwTasks;
            }
        }

        // 参与者类型
        int actorType = execution.getTaskActorProvider().getActorType(nodeModel);

        if (performType == PerformType.orSign) {
            /*
             * 或签一条任务多个参与者
             */
            taskDao.insert(flwTask);
            taskActors.forEach(t -> this.assignTask(flwTask.getInstanceId(), flwTask.getId(), assignActorType(actorType, t.getActorType()), t));
            flwTasks.add(flwTask);

            // 创建任务监听
            this.taskNotify(execution.getTaskEventType(), () -> flwTask, taskActors, nodeModel, flowCreator);
            return flwTasks;
        }

        if (performType == PerformType.sort) {
            /*
             * 按顺序依次审批，一个任务执行完，按顺序多个参与者依次执行
             */
            taskDao.insert(flwTask);
            flwTasks.add(flwTask);

            // 分配下一个参与者
            FlwTaskActor nextFlwTaskActor = execution.getNextFlwTaskActor();
            if (null == nextFlwTaskActor) {
                nextFlwTaskActor = taskActors.get(0);
            }
            this.assignTask(flwTask.getInstanceId(), flwTask.getId(), assignActorType(actorType, nextFlwTaskActor.getActorType()), nextFlwTaskActor);

            // 创建任务监听
            this.taskNotify(execution.getTaskEventType(), () -> flwTask, Collections.singletonList(nextFlwTaskActor), nodeModel, flowCreator);
            return flwTasks;
        }

        /*
         * 会签（票签）每个参与者生成一条任务
         */
        taskActors.forEach(t -> {
            FlwTask newFlwTask = flwTask.cloneTask(null);
            newFlwTask.setId(flowLongIdGenerator.getId(newFlwTask.getId()));
            if (taskDao.insert(newFlwTask)) {
                flwTasks.add(newFlwTask);

                // 分配参与者
                this.assignTask(newFlwTask.getInstanceId(), newFlwTask.getId(), assignActorType(actorType, t.getActorType()), t);

                // 创建任务监听
                this.taskNotify(execution.getTaskEventType(), () -> newFlwTask, Collections.singletonList(t), nodeModel, flowCreator);
            }
        });

        // 返回创建的任务列表
        return flwTasks;
    }

    /**
     * 优先使用数据库参与者类型
     */
    protected int assignActorType(int actorType, Integer dbActorType) {
        return null == dbActorType ? actorType : dbActorType;
    }

    /**
     * 根据 taskId、createId 判断创建人是否允许执行任务
     *
     * @param flwTask 流程任务
     * @param userId  用户ID
     * @return true 允许 false 不允许
     */
    @Override
    public FlwTaskActor isAllowed(FlwTask flwTask, String userId) {
        // 未指定创建人情况，默认为不验证执行权限
        if (null == flwTask.getCreateId()) {
            return null;
        }

        // 任务执行创建人不存在
        if (ObjectUtils.isEmpty(userId)) {
            return null;
        }

        // 任务参与者列表
        List<FlwTaskActor> actors = taskActorDao.selectListByTaskId(flwTask.getId());
        return taskAccessStrategy.isAllowed(userId, actors);
    }

    /**
     * 向指定的任务ID添加参与者
     *
     * @param taskId     任务ID
     * @param taskActors 参与者列表
     */
    @Override
    public boolean addTaskActor(Long taskId, PerformType performType, List<FlwTaskActor> taskActors, FlowCreator flowCreator) {
        FlwTask flwTask = taskDao.selectCheckById(taskId);
        Assert.isTrue(ObjectUtils.isEmpty(taskActors), "actorIds cannot be empty");
        boolean countersign = PerformType.countersign.eq(flwTask.getPerformType());

        if (countersign || (PerformType.sort != performType && PerformType.countersign != performType
                && PerformType.orSign != performType && PerformType.voteSign != performType)) {
            // 会签（非合法类型）不允许修改审批方式
            performType = null;
        } else {
            // 按顺序依次审批，被修改为会签
            countersign = Objects.equals(PerformType.countersign, performType);
        }

        List<FlwTaskActor> ftaList = new ArrayList<>();
        List<FlwTaskActor> taskActorList = this.getTaskActorsByTaskId(taskId);
        Map<String, FlwTaskActor> taskActorMap = taskActorList.stream().collect(Collectors.toMap(FlwTaskActor::getActorId, t -> t));
        for (FlwTaskActor taskActor : taskActors) {
            // 不存在的参与者
            if (null != taskActorMap.get(taskActor.getActorId())) {
                continue;
            }
            if (countersign) {
                /*
                 * 会签多任务情况
                 */
                FlwTask newFlwTask = flwTask.cloneTask(flowCreator.getCreateId(), flowCreator.getCreateBy());
                newFlwTask.setId(flowLongIdGenerator.getId(newFlwTask.getId()));
                if (taskDao.insert(newFlwTask)) {
                    // 分配参与者
                    this.assignTask(flwTask.getInstanceId(), newFlwTask.getId(), 0, taskActor);
                    // 创建会签加签任务监听
                    this.taskNotify(TaskEventType.addCountersign, () -> newFlwTask, Collections.singletonList(taskActor), null, flowCreator);
                }
            } else {
                /*
                 * 单一任务多处理人员情况
                 */
                this.assignTask(flwTask.getInstanceId(), taskId, 0, taskActor);
            }
            // 新增参与者
            ftaList.add(taskActor);
        }

        // 更新任务参与类型
        if (null != performType) {
            FlwTask temp = new FlwTask();
            temp.setId(taskId);
            temp.performType(performType);
            taskDao.updateById(temp);
        }

        // 创建任务监听
        this.taskNotify(TaskEventType.addTaskActor, () -> flwTask, ftaList, null, flowCreator);
        return true;
    }

    protected List<FlwTaskActor> getTaskActorsByTaskId(Long taskId) {
        List<FlwTaskActor> taskActorList = taskActorDao.selectListByTaskId(taskId);
        Assert.isTrue(ObjectUtils.isEmpty(taskActorList), "not found task actor");
        return taskActorList;
    }

    @Override
    public boolean removeTaskActor(Long taskId, List<String> actorIds, FlowCreator flowCreator) {
        if (ObjectUtils.isEmpty(actorIds)) {
            return false;
        }
        List<FlwTaskActor> taskActorList = taskActorDao.selectListByTaskId(taskId);
        if (ObjectUtils.isEmpty(taskActorList)) {
            return false;
        }
        List<FlwTaskActor> ftaList = taskActorList.stream().filter(t -> actorIds.contains(t.getActorId())).collect(Collectors.toList());
        if (Objects.equals(ftaList.size(), taskActorList.size())) {
            // 不允许减签全部参与者
            return false;
        }
        // 执行减签
        if (taskActorDao.deleteByTaskIdAndActorIds(taskId, actorIds)) {
            // 创建任务监听
            this.taskNotify(TaskEventType.removeTaskActor, () -> taskDao.selectCheckById(taskId), ftaList, null, flowCreator);
            return true;
        }
        return false;
    }

    @Override
    public boolean changeTaskActor(Long taskId, FlwTaskActor taskActor) {
        // 删除任务参与者信息
        if (taskActorDao.deleteByTaskId(taskId)) {
            // 关联为指定参与者
            return taskActorDao.insert(taskActor);
        }
        return false;
    }

    @Override
    public void endCallProcessTask(Long callProcessId, Long callInstanceId) {
        List<FlwHisTask> flwHisTasks = hisTaskDao.selectListByCallProcessIdAndCallInstanceId(callProcessId, callInstanceId);
        if (ObjectUtils.isNotEmpty(flwHisTasks)) {
            FlwHisTask dbHis = flwHisTasks.get(0);
            FlwHisTask his = new FlwHisTask();
            his.setId(dbHis.getId());
            his.setCreateTime(dbHis.getCreateTime());
            his.setTaskState(TaskState.complete);
            his.calculateDuration();
            his.setCreateTime(null);
            hisTaskDao.updateById(his);
        }
    }

    /**
     * 级联删除表 flw_his_task_actor, flw_his_task, flw_task_actor, flw_task
     *
     * @param instanceIds 流程实例ID列表
     */
    @Override
    public boolean cascadeRemoveByInstanceIds(List<Long> instanceIds) {
        // 删除历史任务及参与者
        hisTaskActorDao.deleteByInstanceIds(instanceIds);
        hisTaskDao.deleteByInstanceIds(instanceIds);

        // 删除任务及参与者
        taskActorDao.deleteByInstanceIds(instanceIds);
        taskDao.deleteByInstanceIds(instanceIds);
        return true;
    }

}
