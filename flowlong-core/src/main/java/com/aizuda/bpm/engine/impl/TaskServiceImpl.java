/*
 * Copyright 2023-2025 Licensed under the AGPL License
 */
package com.aizuda.bpm.engine.impl;

import com.aizuda.bpm.engine.*;
import com.aizuda.bpm.engine.assist.Assert;
import com.aizuda.bpm.engine.assist.DateUtils;
import com.aizuda.bpm.engine.assist.ObjectUtils;
import com.aizuda.bpm.engine.core.Execution;
import com.aizuda.bpm.engine.core.FlowCreator;
import com.aizuda.bpm.engine.core.enums.EventType;
import com.aizuda.bpm.engine.core.enums.PerformType;
import com.aizuda.bpm.engine.core.enums.TaskState;
import com.aizuda.bpm.engine.core.enums.TaskType;
import com.aizuda.bpm.engine.dao.*;
import com.aizuda.bpm.engine.entity.*;
import com.aizuda.bpm.engine.listener.TaskListener;
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
 * 尊重知识产权，不允许非法使用，后果自负
 * </p>
 *
 * @author hubin
 * @since 1.0
 */
public class TaskServiceImpl implements TaskService {
    private final TaskAccessStrategy taskAccessStrategy;
    private final TaskTrigger taskTrigger;
    private final TaskListener taskListener;
    private final FlwInstanceDao instanceDao;
    private final FlwExtInstanceDao extInstanceDao;
    private final FlwHisInstanceDao hisInstanceDao;
    private final FlwTaskDao taskDao;
    private final FlwTaskActorDao taskActorDao;
    private final FlwHisTaskDao hisTaskDao;
    private final FlwHisTaskActorDao hisTaskActorDao;

    public TaskServiceImpl(TaskAccessStrategy taskAccessStrategy, TaskListener taskListener, TaskTrigger taskTrigger,
                           FlwInstanceDao instanceDao, FlwExtInstanceDao extInstanceDao, FlwHisInstanceDao hisInstanceDao,
                           FlwTaskDao taskDao, FlwTaskActorDao taskActorDao, FlwHisTaskDao hisTaskDao,
                           FlwHisTaskActorDao hisTaskActorDao) {
        this.taskAccessStrategy = taskAccessStrategy;
        this.taskTrigger = taskTrigger;
        this.taskListener = taskListener;
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
    public FlwTask executeTask(Long taskId, FlowCreator flowCreator, Map<String, Object> args, TaskState taskState, EventType eventType) {
        FlwTask flwTask = this.getAllowedFlwTask(taskId, flowCreator, args, taskState);

        // 迁移任务至历史表
        this.moveToHisTask(flwTask, taskState, flowCreator);

        // 任务监听器通知
        this.taskNotify(eventType, () -> flwTask, null, flowCreator);
        return flwTask;
    }

    /**
     * 执行节点跳转任务
     */
    @Override
    public boolean executeJumpTask(Long taskId, String nodeKey, FlowCreator flowCreator, Map<String, Object> args, Function<FlwTask, Execution> executionFunction) {
        FlwTask flwTask = this.getAllowedFlwTask(taskId, flowCreator, null, null);

        // 执行跳转到目标节点
        Execution execution = executionFunction.apply(flwTask);
        ProcessModel processModel = execution.getProcessModel();
        Assert.isNull(processModel, "当前任务未找到流程定义模型");
        execution.setArgs(args);

        // 查找模型节点
        NodeModel nodeModel;
        if (null == nodeKey) {
            // 1，找到当前节点的父节点
            nodeModel = processModel.getNode(flwTask.getTaskKey()).getParentNode();
        } else {
            // 2，找到指定 nodeName 节点
            nodeModel = processModel.getNode(nodeKey);
        }
        Assert.isNull(nodeModel, "根据节点key[" + nodeKey + "]无法找到节点模型");

        // 获取当前执行实例的所有正在执行的任务，强制终止执行并跳到指定节点
        taskDao.selectListByInstanceId(flwTask.getInstanceId()).forEach(t -> this.moveToHisTask(t, TaskState.jump, flowCreator));

        if (0 == nodeModel.getType()) {
            // 发起节点，创建发起任务，分配发起人
            FlwTask initiationTask = this.createTaskBase(nodeModel, execution);
            initiationTask.setPerformType(PerformType.start);
            Assert.isFalse(taskDao.insert(initiationTask), "Failed to create initiation task");
            taskActorDao.insert(FlwTaskActor.ofFlwInstance(execution.getFlwInstance(), initiationTask.getId()));
        } else {
            // 其它节点创建
            this.createTask(nodeModel, execution);
        }

        // 任务监听器通知
        this.taskNotify(EventType.jump, () -> flwTask, nodeModel, flowCreator);
        return true;
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
            flwTask.setVariable(args);
        }
        if (null == taskState || TaskState.allowedCheck(taskState)) {
            Assert.isFalse(isAllowed(flwTask, flowCreator.getCreateId()), () -> "当前参与者 [" + flowCreator.getCreateBy() + "]不允许执行任务[taskId=" + taskId + "]");
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
        // 迁移 task 信息到 flw_his_task
        FlwHisTask hisTask = FlwHisTask.of(flwTask);
        hisTask.setTaskState(taskState);
        hisTask.setFlowCreator(flowCreator);
        hisTask.calculateDuration();

        // 获取当前所有处理人员
        List<FlwTaskActor> taskActors = taskActorDao.selectListByTaskId(flwTask.getId());

        // 代理人审批
        if (TaskType.agent.eq(flwTask.getTaskType())) {

            // 当前处理人为代理人员
            if (taskActors.stream().anyMatch(t -> t.agentActor() && t.eqActorId(flowCreator.getCreateId()))) {

                // 设置历史代理任务状态为【代理人协办完成的任务】设置被代理人信息
                hisTask.setTaskType(TaskType.agentAssist);
                taskActors.stream().filter(t -> !t.agentActor()).findFirst().ifPresent(t -> {
                    hisTask.setAssignorId(t.getActorId());
                    hisTask.setAssignor(t.getActorName());
                });
                hisTaskDao.insert(hisTask);

                // 迁移任务当前代理人员，清理其它代理人
                this.moveToHisTaskActor(Collections.singletonList(FlwTaskActor.of(flowCreator, flwTask, 1)));
                taskActorDao.deleteByTaskIdAndWeight(flwTask.getId(), 1);

                // 代理人完成任务，当前任务设置为代理人归还任务，代理人信息变更
                FlwTask newFlwTask = new FlwTask();
                newFlwTask.setId(flwTask.getId());
                newFlwTask.setTaskType(TaskType.agentReturn);
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
                flwTask.setTaskType(TaskType.agentOwn);
            }
        }

        // 领导审批，代理人归还任务，回收代理人查看权限
        if (TaskType.agentReturn.eq(flwTask.getTaskType())) {
            hisTaskActorDao.deleteByTaskId(flwTask.getId());
            hisTaskDao.deleteById(flwTask.getId());

            // 代理人协办完成的任务
            hisTask.setTaskType(TaskType.agentAssist);
        }

        // 会签情况处理其它任务 排除完成情况
        if (PerformType.countersign.eq(flwTask.getPerformType()) && TaskState.complete.ne(taskState.getValue())) {
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
            return taskDao.deleteBatchIds(taskIds);
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
                hisTaskActorDao.insert(FlwHisTaskActor.of(t));
                // 移除 flw_task_actor 中 task 参与者信息
                taskActorDao.deleteById(t.getId());
            });
        }
    }

    protected void taskNotify(EventType eventType, Supplier<FlwTask> supplier, NodeModel nodeModel, FlowCreator flowCreator) {
        if (null != taskListener) {
            taskListener.notify(eventType, supplier, nodeModel, flowCreator);
        }
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
        this.taskNotify(EventType.update, () -> flwTask, null, flowCreator);
    }

    /**
     * 查看任务设置为已阅状态
     *
     * @param taskId    任务ID
     * @param taskActor 任务参与者
     */
    @Override
    public boolean viewTask(Long taskId, FlwTaskActor taskActor) {
        if (taskActorDao.selectCountByTaskIdAndActorId(taskId, taskActor.getActorId()) > 0) {
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

    /**
     * 根据 任务ID 认领任务，删除其它任务参与者
     */
    @Override
    public FlwTask claim(Long taskId, FlowCreator flowCreator) {
        FlwTask flwTask = taskDao.selectCheckById(taskId);
        if (!isAllowed(flwTask, flowCreator.getCreateId())) {
            Assert.illegal("当前执行用户ID [" + flowCreator.getCreateBy() + "] 不允许提取任务 [taskId=" + taskId + "]");
        }
        // 删除任务参与者
        taskActorDao.deleteByTaskId(taskId);
        // 插入当前用户ID作为唯一参与者
        taskActorDao.insert(FlwTaskActor.of(flowCreator, flwTask));

        // 任务监听器通知
        this.taskNotify(EventType.claim, () -> flwTask, null, flowCreator);
        return flwTask;
    }

    /**
     * 根据 任务ID 分配任务给指定办理人、重置任务类型
     *
     * @param taskId               任务ID
     * @param taskType             任务类型
     * @param flowCreator          任务参与者
     * @param assigneeFlowCreators 指定办理人列表
     * @return true 成功 false 失败
     */
    @Override
    public boolean assigneeTask(Long taskId, TaskType taskType, FlowCreator flowCreator, List<FlowCreator> assigneeFlowCreators) {
        // 受理任务权限验证
        FlwTaskActor flwTaskActor = this.getAllowedFlwTaskActor(taskId, flowCreator);

        // 不允许重复分配
        FlwTask dbFlwTask = taskDao.selectById(taskId);
        if (ObjectUtils.isNotEmpty(dbFlwTask.getAssignorId())) {
            Assert.illegal("Do not allow duplicate assign , taskId = " + taskId);
        }

        // 设置任务为委派任务或者为转办任务
        FlwTask flwTask = new FlwTask();
        flwTask.setId(taskId);
        flwTask.setTaskType(taskType);

        if (taskType == TaskType.agent) {
            // 设置代理人员信息，第一个人为主办 assignorId 其他人为协办 assignor 多个英文逗号分隔
            FlowCreator afc = assigneeFlowCreators.get(0);
            flwTask.setAssignorId(afc.getCreateId());
            flwTask.setAssignor(assigneeFlowCreators.stream().map(FlowCreator::getCreateBy).collect(Collectors.joining(", ")));
            // 分配代理人可见代理任务
            assigneeFlowCreators.forEach(t -> taskActorDao.insert(FlwTaskActor.of(t, dbFlwTask, 1)));
        } else {
            // 设置委托人信息
            flwTask.setAssignorId(flowCreator.getCreateId());
            flwTask.setAssignor(flowCreator.getCreateBy());

            // 删除任务历史参与者
            taskActorDao.deleteById(flwTaskActor.getId());

            // 分配任务给办理人
            FlowCreator afc = assigneeFlowCreators.get(0);
            this.assignTask(flwTaskActor.getInstanceId(), taskId, FlwTaskActor.ofFlowCreator(afc));
        }

        // 更新任务
        taskDao.updateById(flwTask);

        // 任务监听器通知
        this.taskNotify(EventType.assignment, () -> {
            dbFlwTask.setTaskType(taskType);
            dbFlwTask.setAssignorId(flwTask.getAssignorId());
            dbFlwTask.setAssignor(flwTask.getAssignor());
            return dbFlwTask;
        }, null, flowCreator);
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
        FlwTaskActor flwTaskActor = this.getAllowedFlwTaskActor(taskId, flowCreator);

        // 当前委托任务
        FlwTask flwTask = taskDao.selectCheckById(taskId);

        // 任务归还至委托人
        FlwTaskActor taskActor = new FlwHisTaskActor();
        taskActor.setId(flwTaskActor.getId());
        taskActor.setActorId(flwTask.getAssignorId());
        taskActor.setActorName(flwTask.getAssignor());
        if (taskActorDao.updateById(taskActor)) {
            // 设置任务状态为委托归还，委托人设置为归还人
            FlwTask temp = new FlwTask();
            temp.setId(taskId);
            temp.setTaskType(TaskType.delegateReturn);
            temp.setAssignorId(flowCreator.getCreateId());
            temp.setAssignor(flowCreator.getCreateBy());
            Assert.isFalse(taskDao.updateById(temp), "resolveTask failed");

            // 任务监听器通知
            this.taskNotify(EventType.delegateResolve, () -> {
                flwTask.setTaskType(temp.getTaskType());
                flwTask.setAssignorId(temp.getCreateId());
                flwTask.setAssignor(temp.getCreateBy());
                return flwTask;
            }, null, flowCreator);
        }
        return true;
    }

    /**
     * 拿回任务、根据历史任务ID撤回下一个节点的任务、恢复历史任务
     */
    @Override
    public Optional<FlwTask> reclaimTask(Long taskId, FlowCreator flowCreator) {
        // 当前任务子任务已经执行完成不允许撤回
        Assert.isTrue(taskDao.selectCountByParentTaskId(taskId) == 0, "Do not allow reclaim task");

        // 下面执行撤回逻辑
        Optional<FlwTask> flwTaskOptional = this.undoHisTask(taskId, flowCreator, hisTask -> {
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
        flwTaskOptional.ifPresent(flwTask -> this.taskNotify(EventType.reclaim, () -> flwTask, null, flowCreator));
        return flwTaskOptional;
    }

    /**
     * 唤醒指定的历史任务
     */
    @Override
    public FlwTask resume(Long taskId, FlowCreator flowCreator) {
        FlwHisTask histTask = hisTaskDao.selectCheckById(taskId);
        Assert.isTrue(ObjectUtils.isEmpty(histTask.getCreateBy()) || !Objects.equals(histTask.getCreateBy(), flowCreator.getCreateBy()),
                "当前参与者[" + flowCreator.getCreateBy() + "]不允许唤醒历史任务[taskId=" + taskId + "]");

        // 流程实例结束情况恢复流程实例
        FlwInstance flwInstance = instanceDao.selectById(histTask.getInstanceId());
        Assert.isNull(flwInstance, "已结束流程任务不支持唤醒");

        // 历史任务恢复
        FlwTask flwTask = histTask.cloneTask(null);
        taskDao.insert(flwTask);

        // 分配任务
        assignTask(flwTask.getInstanceId(), taskId, FlwTaskActor.of(flowCreator, flwTask));

        // 更新当前执行节点信息
        this.updateCurrentNode(flwTask);

        // 任务监听器通知
        this.taskNotify(EventType.resume, () -> flwTask, null, flowCreator);
        return flwTask;
    }

    /**
     * 撤回指定的任务
     */
    @Override
    public Optional<FlwTask> withdrawTask(Long taskId, FlowCreator flowCreator) {
        return this.undoHisTask(taskId, flowCreator, hisTask -> {
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
            Assert.isEmpty(flwTasks, "后续活动任务已完成或不存在，无法撤回.");
            List<Long> taskIds = flwTasks.stream().map(FlowEntity::getId).collect(Collectors.toList());
            // 查询任务参与者
            List<Long> taskActorIds = taskActorDao.selectListByTaskIds(taskIds)
                    .stream().map(FlwTaskActor::getId).collect(Collectors.toList());
            if (ObjectUtils.isNotEmpty(taskActorIds)) {
                taskActorDao.deleteBatchIds(taskActorIds);
            }
            taskDao.deleteBatchIds(flwTasks.stream().map(FlowEntity::getId).collect(Collectors.toList()));

            // 任务监听器通知
            this.taskNotify(EventType.withdraw, () -> hisTask, null, flowCreator);
        });
    }

    @Override
    public Optional<FlwTask> rejectTask(FlwTask currentFlwTask, FlowCreator flowCreator, Map<String, Object> args) {
        Assert.isTrue(currentFlwTask.startNode(), "上一步任务ID为空，无法驳回至上一步处理");

        // 执行任务驳回
        this.executeTask(currentFlwTask.getId(), flowCreator, args, TaskState.reject, EventType.reject);

        // 撤回至上一级任务
        Long parentTaskId = currentFlwTask.getParentTaskId();
        Optional<FlwTask> flwTaskOptional = this.undoHisTask(parentTaskId, flowCreator, null);

        // 任务监听器通知
        flwTaskOptional.ifPresent(flwTask -> this.taskNotify(EventType.recreate, () -> flwTask, null, flowCreator));
        return flwTaskOptional;
    }

    /**
     * 撤回历史任务
     *
     * @param hisTaskId       历史任务ID
     * @param flowCreator     任务创建者
     * @param hisTaskConsumer 历史任务业务处理
     * @return 任务参与者
     */
    protected Optional<FlwTask> undoHisTask(Long hisTaskId, FlowCreator flowCreator, Consumer<FlwHisTask> hisTaskConsumer) {
        FlwHisTask hisTask = hisTaskDao.selectCheckById(hisTaskId);
        if (null != hisTaskConsumer) {
            hisTaskConsumer.accept(hisTask);
        }

        // 撤回历史任务
        if (hisTask.startNode()) {
            // 如果直接撤回到发起人，构建发起人关联信息
            FlwTask flwTask = hisTask.undoTask();
            taskDao.insert(flwTask);
            taskActorDao.insert(FlwTaskActor.ofFlwTask(flwTask));
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
                    taskActorMap.forEach((k, v) -> hisTasks.stream().filter(t -> Objects.equals(t.getId(), v.getTaskId()))
                            .findFirst().ifPresent(t -> {
                                FlwTask flwTask = t.undoTask();
                                taskDao.insert(flwTask);
                                taskActorDao.insert(FlwTaskActor.of(flwTask.getId(), v));
                            }));
                }
            } else {
                // 恢复历史任务
                FlwTask flwTask = hisTask.undoTask();
                taskDao.insert(flwTask);

                // 撤回任务参与者
                List<FlwHisTaskActor> hisTaskActors = hisTaskActorDao.selectListByTaskId(hisTask.getId());
                if (null != hisTaskActors) {
                    hisTaskActors.forEach(t -> taskActorDao.insert(FlwTaskActor.of(flwTask.getId(), t)));
                }
            }
        }

        // 更新当前执行节点信息
        this.updateCurrentNode(hisTask);
        return Optional.of(hisTask);
    }

    /**
     * 对指定的任务分配参与者。参与者可以为用户、部门、角色
     *
     * @param instanceId 实例ID
     * @param taskId     任务ID
     * @param taskActor  任务参与者
     */
    protected void assignTask(Long instanceId, Long taskId, FlwTaskActor taskActor) {
        taskActor.setId(null);
        taskActor.setInstanceId(instanceId);
        taskActor.setTaskId(taskId);
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
        newFlwTask.setTaskType(taskType);
        newFlwTask.setPerformType(performType);
        newFlwTask.setParentTaskId(taskId);
        Execution execution = executionFunction.apply(newFlwTask);
        execution.setFlowCreator(flowCreator);
        return this.saveTask(newFlwTask, performType, taskActors, execution, null);
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
    public List<FlwTask> createTask(NodeModel nodeModel, Execution execution) {
        // 构建任务
        FlwTask flwTask = this.createTaskBase(nodeModel, execution);

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
             * 0，发起人 （ 直接保存历史任务、执行进入下一个节点逻辑 ）
             */
            flwTasks.addAll(this.saveTask(flwTask, PerformType.start, taskActors, execution, nodeModel));

            /*
             * 执行进入下一个节点
             */
            nodeModel.nextNode().ifPresent(nextNode -> nextNode.execute(execution.getEngine().getContext(), execution));
        } else if (TaskType.approval.eq(nodeType)) {
            /*
             * 1，审批人
             */
            PerformType performType = PerformType.get(nodeModel.getExamineMode());
            flwTasks.addAll(this.saveTask(flwTask, performType, taskActors, execution, nodeModel));
            // 审批提醒
            TaskReminder taskReminder = execution.getEngine().getContext().getTaskReminder();
            if (null != taskReminder) {
                flwTasks.forEach(s -> taskReminder.remind(execution.getEngine().getContext(), s.getInstanceId(), s));
            }
        } else if (TaskType.cc.eq(nodeType)) {
            /*
             * 2，抄送任务
             */
            this.saveTaskCc(nodeModel, flwTask, execution.getFlowCreator());

            /*
             * 可能存在子节点
             */
            nodeModel.nextNode().ifPresent(nextNode -> nextNode.execute(execution.getEngine().getContext(), execution));
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
            execution.getEngine().startProcessInstance(flwProcess, flowCreator, null, () -> {
                FlwInstance flwInstance = new FlwInstance();
                flwInstance.setParentInstanceId(flwTask.getInstanceId());
                return flwInstance;
            }).ifPresent(instance -> {
                // 归档历史
                FlwHisTask flwHisTask = FlwHisTask.ofCallInstance(nodeModel, instance);
                if (hisTaskDao.insert(flwHisTask)) {
                    // 追加子流程实例ID
                    nodeModel.setCallProcess(nodeModel.getCallProcess() + ":" + instance.getId());
                    // 主流程监听
                    this.taskNotify(EventType.callProcess, () -> flwHisTask, nodeModel, flowCreator);
                }
            });
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
                Assert.isFalse(taskTrigger.execute(nodeModel, execution), "task trigger execute failed");
                // 执行成功，任务归档
                FlwHisTask hisTask = FlwHisTask.of(flwTask);
                hisTask.setTaskState(TaskState.complete);
                hisTask.setFlowCreator(execution.getFlowCreator());
                hisTask.calculateDuration();
                hisTaskDao.insert(hisTask);
            } else {
                // 定时触发器，等待执行
                flwTasks.addAll(this.saveTask(flwTask, PerformType.trigger, taskActors, execution, nodeModel));
            }
        }

        return flwTasks;
    }

    /**
     * 保存抄送任务
     *
     * @param nodeModel   节点模型
     * @param flwTask     流程任务对象
     * @param flowCreator 处理人
     */
    public void saveTaskCc(NodeModel nodeModel, FlwTask flwTask, FlowCreator flowCreator) {
        List<NodeAssignee> nodeUserList = nodeModel.getNodeAssigneeList();
        if (ObjectUtils.isNotEmpty(nodeUserList)) {
            // 抄送任务
            FlwHisTask flwHisTask = FlwHisTask.of(flwTask, TaskState.complete);
            flwHisTask.setTaskType(TaskType.cc);
            flwHisTask.setPerformType(PerformType.copy);
            flwHisTask.calculateDuration();
            hisTaskDao.insert(flwHisTask);

            // 任务监听器通知
            this.taskNotify(EventType.cc, () -> flwHisTask, nodeModel, flowCreator);

            for (NodeAssignee nodeUser : nodeUserList) {
                hisTaskActorDao.insert(FlwHisTaskActor.ofNodeAssignee(nodeUser, flwHisTask.getInstanceId(), flwHisTask.getId()));
            }
        }
    }

    /**
     * 根据模型、执行对象、任务类型构建基本的task对象
     *
     * @param nodeModel 节点模型
     * @param execution 执行对象
     * @return Task任务对象
     */
    private FlwTask createTaskBase(NodeModel nodeModel, Execution execution) {
        FlwTask flwTask = new FlwTask();
        flwTask.setFlowCreator(execution.getFlowCreator());
        flwTask.setCreateTime(DateUtils.getCurrentDate());
        flwTask.setInstanceId(execution.getFlwInstance().getId());
        flwTask.setTaskName(nodeModel.getNodeName());
        flwTask.setTaskKey(nodeModel.getNodeKey());
        flwTask.setTaskType(nodeModel.getType());
        flwTask.setParentTaskId(execution.getFlwTask() == null ? 0L : execution.getFlwTask().getId());
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
        flwTask.setVariable(args);
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
        flwTask.setPerformType(performType);
        final FlowCreator flowCreator = execution.getFlowCreator();

        if (performType == PerformType.timer || performType == PerformType.trigger) {
            // 定时器任务，触发器任务
            taskDao.insert(flwTask);
            flwTasks.add(flwTask);
            return flwTasks;
        }

        if (performType == PerformType.start) {
            // 发起任务
            FlwHisTask flwHisTask = FlwHisTask.of(flwTask, TaskState.complete);
            flwHisTask.calculateDuration();
            if (hisTaskDao.insert(flwHisTask)) {
                // 设置为执行任务
                execution.setFlwTask(flwHisTask);
                // 记录发起人
                hisTaskActorDao.insert(FlwHisTaskActor.ofFlwHisTask(flwHisTask));
                flwTasks.add(flwTask);

                // 创建任务监听
                this.taskNotify(EventType.start, () -> flwTask, nodeModel, flowCreator);
            }
            return flwTasks;
        }

        if (ObjectUtils.isEmpty(taskActors)) {
            Assert.illegal("taskActors cannot be empty. taskName = " + flwTask.getTaskName() + ", taskKey = " +
                    flwTask.getTaskKey() + ", performType = " + performType.getValue());
        }

        if (performType == PerformType.orSign) {
            /*
             * 或签一条任务多个参与者
             */
            taskDao.insert(flwTask);
            taskActors.forEach(t -> this.assignTask(flwTask.getInstanceId(), flwTask.getId(), t));
            flwTasks.add(flwTask);

            // 创建任务监听
            this.taskNotify(EventType.create, () -> flwTask, nodeModel, flowCreator);
            return flwTasks;
        }

        if (performType == PerformType.sort) {
            /*
             * 按顺序依次审批，一个任务按顺序多个参与者依次添加
             */
            taskDao.insert(flwTask);
            flwTasks.add(flwTask);

            // 分配一个参与者
            FlwTaskActor nextFlwTaskActor = execution.getNextFlwTaskActor();
            this.assignTask(flwTask.getInstanceId(), flwTask.getId(), null == nextFlwTaskActor ? taskActors.get(0) : nextFlwTaskActor);

            // 创建任务监听
            this.taskNotify(EventType.create, () -> flwTask, nodeModel, flowCreator);
            return flwTasks;
        }

        /*
         * 会签（票签）每个参与者生成一条任务
         */
        taskActors.forEach(t -> {
            FlwTask newFlwTask = flwTask.cloneTask(null);
            taskDao.insert(newFlwTask);
            flwTasks.add(newFlwTask);

            // 分配参与者
            this.assignTask(newFlwTask.getInstanceId(), newFlwTask.getId(), t);

            // 创建任务监听
            this.taskNotify(EventType.create, () -> newFlwTask, nodeModel, flowCreator);
        });
        return flwTasks;
    }

    /**
     * 根据 taskId、createId 判断创建人是否允许执行任务
     *
     * @param flwTask 流程任务
     * @param userId  用户ID
     * @return true 允许 false 不允许
     */
    @Override
    public boolean isAllowed(FlwTask flwTask, String userId) {
        // 未指定创建人情况，默认为不验证执行权限
        if (null == flwTask.getCreateId()) {
            return true;
        }

        // 任务执行创建人不存在
        if (ObjectUtils.isEmpty(userId)) {
            return false;
        }

        // 任务参与者列表
        List<FlwTaskActor> actors = taskActorDao.selectListByTaskId(flwTask.getId());
        if (ObjectUtils.isEmpty(actors)) {
            // 未设置参与者，默认返回 true
            return true;
        }
        return taskAccessStrategy.isAllowed(userId, actors);
    }

    /**
     * 向指定的任务ID添加参与者
     *
     * @param taskId        任务ID
     * @param flwTaskActors 参与者列表
     */
    @Override
    public boolean addTaskActor(Long taskId, PerformType performType, List<FlwTaskActor> flwTaskActors, FlowCreator flowCreator) {
        FlwTask flwTask = taskDao.selectCheckById(taskId);
        Assert.isTrue(ObjectUtils.isEmpty(flwTaskActors), "actorIds cannot be empty");

        List<FlwTaskActor> taskActorList = this.getTaskActorsByTaskId(taskId);
        Map<String, FlwTaskActor> taskActorMap = taskActorList.stream().collect(Collectors.toMap(FlwTaskActor::getActorId, t -> t));
        for (FlwTaskActor flwTaskActor : flwTaskActors) {
            // 不存在的参与者
            if (null != taskActorMap.get(flwTaskActor.getActorId())) {
                continue;
            }
            if (PerformType.countersign.eq(flwTask.getPerformType())) {
                /*
                 * 会签多任务情况
                 */
                FlwTask newFlwTask = flwTask.cloneTask(flowCreator.getCreateId(), flowCreator.getCreateBy());
                taskDao.insert(newFlwTask);
                this.assignTask(flwTask.getInstanceId(), newFlwTask.getId(), flwTaskActor);
            } else {
                /*
                 * 单一任务多处理人员情况
                 */
                this.assignTask(flwTask.getInstanceId(), taskId, flwTaskActor);
            }
        }

        // 更新任务参与类型
        FlwTask temp = new FlwTask();
        temp.setId(taskId);
        temp.setPerformType(performType);
        if (taskDao.updateById(temp)) {
            // 创建任务监听
            this.taskNotify(EventType.addTaskActor, () -> flwTask, null, flowCreator);
            return true;
        }
        return false;
    }

    protected List<FlwTaskActor> getTaskActorsByTaskId(Long taskId) {
        List<FlwTaskActor> taskActorList = taskActorDao.selectListByTaskId(taskId);
        Assert.isTrue(ObjectUtils.isEmpty(taskActorList), "not found task actor");
        return taskActorList;
    }

    @Override
    public boolean removeTaskActor(Long taskId, List<String> actorIds, FlowCreator flowCreator) {
        FlwTask flwTask = taskDao.selectCheckById(taskId);
        Assert.isTrue(ObjectUtils.isEmpty(actorIds), "actorIds cannot be empty");

        if (PerformType.countersign.eq(flwTask.getPerformType())) {
            /*
             * 会签多任务情况
             */
            List<FlwTaskActor> taskActorList = taskActorDao.selectListByInstanceId(flwTask.getInstanceId());
            Assert.isTrue(taskActorList.size() >= actorIds.size(), "cannot all be deleted");
            taskActorList.forEach(t -> {
                if (actorIds.contains(t.getActorId())) {
                    // 删除参与者表
                    taskActorDao.deleteById(t.getId());
                    // 删除任务表
                    taskDao.deleteById(t.getTaskId());
                }
            });
        } else {
            /*
             * 单一任务多处理人员情况
             */
            List<FlwTaskActor> taskActorList = this.getTaskActorsByTaskId(taskId);
            Assert.isTrue(taskActorList.size() >= actorIds.size(), "cannot all be deleted");

            // 删除参与者表，任务关联关系
            taskActorDao.deleteByTaskIdAndActorIds(taskId, actorIds);
        }

        // 创建任务监听
        this.taskNotify(EventType.removeTaskActor, () -> flwTask, null, flowCreator);
        return true;
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
    public void cascadeRemoveByInstanceIds(List<Long> instanceIds) {
        // 删除历史任务及参与者
        hisTaskActorDao.deleteByInstanceIds(instanceIds);
        hisTaskDao.deleteByInstanceIds(instanceIds);

        // 删除任务及参与者
        taskActorDao.deleteByInstanceIds(instanceIds);
        taskDao.deleteByInstanceIds(instanceIds);
    }

}
