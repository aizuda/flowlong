/*
 * Copyright 2023-2025 Licensed under the AGPL License
 */
package com.flowlong.bpm.mybatisplus.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.flowlong.bpm.engine.TaskAccessStrategy;
import com.flowlong.bpm.engine.TaskService;
import com.flowlong.bpm.engine.assist.Assert;
import com.flowlong.bpm.engine.assist.DateUtils;
import com.flowlong.bpm.engine.assist.ObjectUtils;
import com.flowlong.bpm.engine.core.Execution;
import com.flowlong.bpm.engine.core.FlowCreator;
import com.flowlong.bpm.engine.core.enums.EventType;
import com.flowlong.bpm.engine.core.enums.PerformType;
import com.flowlong.bpm.engine.core.enums.TaskState;
import com.flowlong.bpm.engine.core.enums.TaskType;
import com.flowlong.bpm.engine.entity.*;
import com.flowlong.bpm.engine.exception.FlowLongException;
import com.flowlong.bpm.engine.listener.TaskListener;
import com.flowlong.bpm.engine.model.NodeAssignee;
import com.flowlong.bpm.engine.model.NodeModel;
import com.flowlong.bpm.engine.model.ProcessModel;
import com.flowlong.bpm.mybatisplus.mapper.*;

import java.util.*;
import java.util.function.Consumer;
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
    private TaskAccessStrategy taskAccessStrategy;
    private FlwProcessMapper processMapper;
    private TaskListener taskListener;
    private FlwInstanceMapper instanceMapper;
    private FlwTaskMapper taskMapper;
    private FlwTaskCcMapper taskCcMapper;
    private FlwTaskActorMapper taskActorMapper;
    private FlwHisTaskMapper hisTaskMapper;
    private FlwHisTaskActorMapper hisTaskActorMapper;

    public TaskServiceImpl(TaskAccessStrategy taskAccessStrategy, TaskListener taskListener,
                           FlwProcessMapper processMapper, FlwInstanceMapper instanceMapper, FlwTaskMapper taskMapper,
                           FlwTaskCcMapper taskCcMapper, FlwTaskActorMapper taskActorMapper, FlwHisTaskMapper hisTaskMapper,
                           FlwHisTaskActorMapper hisTaskActorMapper) {
        this.taskAccessStrategy = taskAccessStrategy;
        this.processMapper = processMapper;
        this.taskListener = taskListener;
        this.instanceMapper = instanceMapper;
        this.taskMapper = taskMapper;
        this.taskCcMapper = taskCcMapper;
        this.taskActorMapper = taskActorMapper;
        this.hisTaskMapper = hisTaskMapper;
        this.hisTaskActorMapper = hisTaskActorMapper;
    }

    /**
     * 完成指定任务
     * 该方法仅仅结束活动任务，并不能驱动流程继续执行
     */
    @Override
    public FlwTask complete(Long taskId, FlowCreator flowCreator, Map<String, Object> args) {
        return this.executeTask(taskId, flowCreator, args, TaskState.complete, EventType.complete);
    }

    /**
     * 执行任务
     *
     * @param taskId      任务ID
     * @param flowCreator 任务创建者
     * @param args        执行参数
     * @param taskState   任务状态
     * @param eventType   执行事件
     * @return
     */
    protected FlwTask executeTask(Long taskId, FlowCreator flowCreator, Map<String, Object> args, TaskState taskState, EventType eventType) {
        FlwTask flwTask = taskMapper.getCheckById(taskId);
        flwTask.setVariable(args);
        Assert.isFalse(isAllowed(flwTask, flowCreator.getCreateId()), "当前参与者 [" + flowCreator.getCreateBy() + "]不允许执行任务[taskId=" + taskId + "]");

        // 迁移任务至历史表
        this.moveToHisTask(flwTask, taskState, flowCreator);

        // 任务监听器通知
        this.taskNotify(eventType, flwTask);
        return flwTask;
    }

    /**
     * 迁移任务至历史表
     *
     * @param flwTask     执行任务
     * @param taskState   任务状态
     * @param flowCreator 任务创建者
     * @return
     */
    protected boolean moveToHisTask(FlwTask flwTask, TaskState taskState, FlowCreator flowCreator) {
        // 迁移 task 信息到 flw_his_task
        FlwHisTask hisTask = FlwHisTask.of(flwTask);
        hisTask.setFinishTime(DateUtils.getCurrentDate());
        hisTask.setTaskState(taskState);
        hisTask.setCreateId(flowCreator.getCreateId());
        hisTask.setCreateBy(flowCreator.getCreateBy());
        Assert.isFalse(hisTaskMapper.insert(hisTask) > 0, "Migration to FlwHisTask table failed");

        // 迁移任务参与者
        List<FlwTaskActor> taskActors = taskActorMapper.selectListByTaskId(flwTask.getId());
        if (ObjectUtils.isNotEmpty(taskActors)) {
            // 将 task 参与者信息迁移到 flw_his_task_actor
            taskActors.forEach(t -> Assert.isFalse(hisTaskActorMapper.insert(FlwHisTaskActor.of(t)) > 0,
                    "Migration to FlwHisTaskActor table failed"));
            // 移除 flw_task_actor 中 task 参与者信息
            Assert.isFalse(taskActorMapper.deleteByTaskId(flwTask.getId()), "Delete FlwTaskActor table failed");
        }

        // 删除 flw_task 中指定 task 信息
        return taskMapper.deleteById(flwTask.getId()) > 0;
    }

    protected void taskNotify(EventType eventType, FlwTask flwTask) {
        if (null != taskListener) {
            taskListener.notify(eventType, flwTask);
        }
    }

    /**
     * 完成指定实例ID活动任务
     *
     * @param instanceId 实例ID
     * @return
     */
    @Override
    public boolean completeActiveTasksByInstanceId(Long instanceId, FlowCreator flowCreator) {
        List<FlwTask> flwTasks = taskMapper.selectList(Wrappers.<FlwTask>lambdaQuery().eq(FlwTask::getInstanceId, instanceId));
        if (ObjectUtils.isNotEmpty(flwTasks)) {
            for (FlwTask flwTask : flwTasks) {
                // 迁移任务至历史表，设置任务状态为终止
                if (!this.moveToHisTask(flwTask, TaskState.termination, flowCreator)) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * 更新任务对象的finish_Time、createBy、expire_Time、version、variable
     *
     * @param flwTask 任务对象
     */
    @Override
    public void updateTaskById(FlwTask flwTask) {
        taskMapper.updateById(flwTask);
        // 任务监听器通知
        this.taskNotify(EventType.update, flwTask);
    }

    /**
     * 查看任务设置为已阅状态
     *
     * @param taskId    任务ID
     * @param taskActor 任务参与者
     * @return
     */
    @Override
    public boolean viewTask(Long taskId, FlwTaskActor taskActor) {
        if (taskActorMapper.selectCount(Wrappers.<FlwTaskActor>lambdaQuery().eq(FlwTaskActor::getTaskId, taskId)
                .eq(FlwTaskActor::getActorId, taskActor.getActorId())) > 0) {
            /**
             * 设置任务为已阅状态
             */
            FlwTask flwTask = new FlwTask();
            flwTask.setId(taskId);
            flwTask.setViewed(1);
            return taskMapper.updateById(flwTask) > 0;
        }
        return false;
    }

    /**
     * 任务设置超时
     *
     * @param taskId 任务ID
     */
    @Override
    public boolean taskTimeout(Long taskId) {
        FlwTask flwTask = taskMapper.selectById(taskId);
        if (null != flwTask) {
            // 1，保存任务状态为超时，设置完成时间
            FlwHisTask hisTask = FlwHisTask.of(flwTask);
            hisTask.setFinishTime(DateUtils.getCurrentDate());
            hisTask.setTaskState(TaskState.timeout);
            hisTaskMapper.insert(hisTask);

            // 2，级联删除任务和对应的任务参与者
            taskActorMapper.deleteByTaskId(taskId);
            taskMapper.deleteById(taskId);

            // 3，任务监听器通知
            this.taskNotify(EventType.timeout, flwTask);
        }
        return true;
    }

    /**
     * 根据 任务ID 认领任务，删除其它任务参与者
     */
    @Override
    public FlwTask claim(Long taskId, FlwHisTaskActor flwHisTaskActor) {
        FlwTask flwTask = taskMapper.getCheckById(taskId);
        if (!isAllowed(flwTask, flwHisTaskActor.getActorId())) {
            throw new FlowLongException("当前执行用户ID [" + flwHisTaskActor.getActorName() + "] 不允许提取任务 [taskId=" + taskId + "]");
        }
        // 删除任务参与者
        taskActorMapper.deleteByTaskId(taskId);
        // 插入当前用户ID作为唯一参与者
        taskActorMapper.insert(flwHisTaskActor);
        return flwTask;
    }

    /**
     * 根据 任务ID 分配任务给指定办理人、重置任务类型
     *
     * @param taskId            任务ID
     * @param taskType          任务类型
     * @param taskActor         任务参与者
     * @param assigneeTaskActor 指定办理人
     * @return
     */
    @Override
    public boolean assigneeTask(Long taskId, TaskType taskType, FlwTaskActor taskActor, FlwTaskActor assigneeTaskActor) {
        // 转办权限验证
        List<FlwTaskActor> taskActors = taskActorMapper.selectList(Wrappers.<FlwTaskActor>lambdaQuery().eq(FlwTaskActor::getTaskId, taskId)
                .eq(FlwTaskActor::getActorId, taskActor.getActorId()));
        Assert.isTrue(ObjectUtils.isEmpty(taskActors), "无权转办该任务");

        // 设置任务为委派任务或者为转办任务
        FlwTask flwTask = new FlwTask();
        flwTask.setId(taskId);
        flwTask.setTaskType(taskType);
        flwTask.setAssignorId(taskActor.getActorId());
        flwTask.setAssignor(taskActor.getActorName());
        taskMapper.updateById(flwTask);

        // 删除任务历史参与者
        taskActorMapper.deleteBatchIds(taskActors.stream().map(t -> t.getId()).collect(Collectors.toList()));

        // 分配任务给办理人
        assignTask(taskActors.get(0).getInstanceId(), taskId, assigneeTaskActor);
        return true;
    }

    /**
     * 拿回任务、根据历史任务ID撤回下一个节点的任务、恢复历史任务
     */
    @Override
    public Optional<FlwTask> reclaimTask(Long taskId, FlowCreator flowCreator) {
        return this.undoHisTask(taskId, flowCreator, hisTask -> {
            List<FlwTask> flwTaskList = taskMapper.selectListByInstanceId(hisTask.getInstanceId());
            if (ObjectUtils.isNotEmpty(flwTaskList)) {
                List<Long> taskIds = flwTaskList.stream().map(t -> t.getId()).collect(Collectors.toList());
                // 删除当前任务
                taskMapper.deleteBatchIds(taskIds);
                // 删除当前任务处理人
                taskActorMapper.deleteByTaskIds(taskIds);
            }
        });
    }

    /**
     * 唤醒指定的历史任务
     */
    @Override
    public FlwTask resume(Long taskId, FlwHisTaskActor flwHisTaskActor) {
        FlwHisTask histTask = hisTaskMapper.getCheckById(taskId);
        Assert.isTrue(ObjectUtils.isEmpty(histTask.getCreateBy()) || !Objects.equals(histTask.getCreateBy(), flwHisTaskActor.getActorId()),
                "当前参与者[" + flwHisTaskActor.getActorId() + "]不允许唤醒历史任务[taskId=" + taskId + "]");

        // 流程实例结束情况恢复流程实例
        FlwInstance flwInstance = instanceMapper.selectById(histTask.getInstanceId());
        Assert.isNull(flwInstance, "已结束流程任务不支持唤醒");

        // 历史任务恢复
        FlwTask flwTask = histTask.cloneTask(null);
        taskMapper.insert(flwTask);

        // 分配任务
        assignTask(flwTask.getInstanceId(), taskId, flwHisTaskActor);
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
                flwTasks = taskMapper.selectList(Wrappers.<FlwTask>lambdaQuery().eq(FlwTask::getParentTaskId, hisTask.getId()));
            } else {
                List<Long> hisTaskIds = hisTaskMapper.selectList(Wrappers.<FlwHisTask>lambdaQuery().eq(FlwHisTask::getInstanceId, hisTask.getInstanceId())
                                .eq(FlwHisTask::getTaskName, hisTask.getTaskName()).eq(FlwHisTask::getParentTaskId, hisTask.getParentTaskId()))
                        .stream().map(FlwHisTask::getId).collect(Collectors.toList());
                if (ObjectUtils.isNotEmpty(hisTaskIds)) {
                    flwTasks = taskMapper.selectList(Wrappers.<FlwTask>lambdaQuery().in(FlwTask::getParentTaskId, hisTaskIds));
                }
            }
            if (ObjectUtils.isEmpty(flwTasks)) {
                throw new FlowLongException("后续活动任务已完成或不存在，无法撤回.");
            }
            List<Long> taskIds = flwTasks.stream().map(FlowEntity::getId).collect(Collectors.toList());
            // 查询任务参与者
            List<Long> taskActorIds = taskActorMapper.selectList(Wrappers.<FlwTaskActor>lambdaQuery().in(FlwTaskActor::getTaskId, taskIds))
                    .stream().map(FlwTaskActor::getId).collect(Collectors.toList());
            if (ObjectUtils.isNotEmpty(taskActorIds)) {
                taskActorMapper.deleteBatchIds(taskActorIds);
            }
            taskMapper.deleteBatchIds(flwTasks.stream().map(FlowEntity::getId).collect(Collectors.toList()));
        });
    }

    @Override
    public Optional<FlwTask> rejectTask(FlwTask currentFlwTask, FlowCreator flowCreator, Map<String, Object> args) {
        Long parentTaskId = currentFlwTask.getParentTaskId();
        Assert.isTrue(Objects.equals(parentTaskId, 0L), "上一步任务ID为空，无法驳回至上一步处理");

        // 执行任务驳回
        this.executeTask(currentFlwTask.getId(), flowCreator, args, TaskState.reject, EventType.reject);

        // 撤回至上一级任务
        return this.undoHisTask(parentTaskId, flowCreator, null);
    }

    /**
     * 撤回历史任务
     *
     * @param hisTaskId       历史任务ID
     * @param flowCreator     任务创建者
     * @param hisTaskConsumer 历史任务业务处理
     * @return
     */
    protected Optional<FlwTask> undoHisTask(Long hisTaskId, FlowCreator flowCreator, Consumer<FlwHisTask> hisTaskConsumer) {
        FlwHisTask hisTask = hisTaskMapper.getCheckById(hisTaskId);
        if (null != hisTaskConsumer) {
            hisTaskConsumer.accept(hisTask);
        }
        // 撤回历史任务
        FlwTask flwTask = hisTask.undoTask(flowCreator);
        taskMapper.insert(flwTask);
        // 撤回任务参与者
        List<FlwHisTaskActor> hisTaskActors = hisTaskActorMapper.selectListByTaskId(hisTaskId);
        if (null != hisTaskActors) {
            hisTaskActors.forEach(t -> {
                FlwTaskActor flwTaskActor = new FlwTaskActor();
                flwTaskActor.setTenantId(t.getTenantId());
                flwTaskActor.setInstanceId(t.getInstanceId());
                flwTaskActor.setTaskId(flwTask.getId());
                flwTaskActor.setActorType(t.getActorType());
                flwTaskActor.setActorId(t.getActorId());
                flwTaskActor.setActorName(t.getActorName());
                taskActorMapper.insert(flwTaskActor);
            });
        }
        return Optional.ofNullable(flwTask);
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
        taskActorMapper.insert(taskActor);
    }

    /**
     * 根据已有任务、任务类型、参与者创建新的任务
     * 适用于转派，动态协办处理
     */
    @Override
    public List<FlwTask> createNewTask(Long taskId, TaskType taskType, List<FlwTaskActor> taskActors) {
        Assert.isTrue(ObjectUtils.isEmpty(taskActors), "参与者不能为空");
        FlwTask flwTask = taskMapper.getCheckById(taskId);
        FlwTask newFlwTask = flwTask.cloneTask(null);
        newFlwTask.setTaskType(taskType);
        newFlwTask.setParentTaskId(taskId);
        return this.saveTask(newFlwTask, PerformType.sort, taskActors, null);
    }

    /**
     * 获取超时或者提醒的任务
     *
     * @return List<Task> 任务列表
     */
    @Override
    public List<FlwTask> getTimeoutOrRemindTasks() {
        Date currentDate = DateUtils.getCurrentDate();
        return taskMapper.selectList(Wrappers.<FlwTask>lambdaQuery().le(FlwTask::getExpireTime, currentDate).or().le(FlwTask::getRemindTime, currentDate));
    }

    /**
     * 获取任务模型
     *
     * @param taskId 任务ID
     * @return TaskModel
     */
    @Override
    public NodeModel getTaskModel(Long taskId) {
        FlwTask flwTask = taskMapper.getCheckById(taskId);
        FlwInstance flwInstance = instanceMapper.selectById(flwTask.getInstanceId());
        Assert.isNull(flwInstance);
        FlwProcess process = processMapper.selectById(flwInstance.getProcessId());
        ProcessModel model = process.model();
        NodeModel nodeModel = model.getNode(flwTask.getTaskName());
        Assert.isNull(nodeModel, "任务ID无法找到节点模型.");
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
        List<FlwTask> flwTasks = new LinkedList<>();

        // 处理流程任务
        Integer nodeType = nodeModel.getType();
        if (0 == nodeType || 1 == nodeType) {
            /**
             * 0，发起人 1，审批人
             */
            PerformType performType = PerformType.get(nodeModel.getExamineMode());
            flwTasks.addAll(this.saveTask(flwTask, performType, taskActors, execution));
        } else if (2 == nodeType) {
            /**
             * 2，抄送任务
             */
            this.saveTaskCc(nodeModel, execution);
            NodeModel nextNode = nodeModel.getChildNode();
            if (null != nextNode) {
                // 继续创建普通任务
                this.createTask(nextNode, execution);
            }
        } else if (3 == nodeType) {
            /**
             * 3，条件审批
             */
            FlwTask singleFlwTask = flwTask.cloneTask(null);
            PerformType performType = PerformType.get(nodeModel.getExamineMode());
            flwTasks.addAll(this.saveTask(singleFlwTask, performType, taskActors, execution));
        }
        return flwTasks;
    }

    /**
     * 保存抄送任务
     *
     * @param nodeModel 节点模型
     * @param execution 执行对象
     * @return Task任务对象
     */
    public void saveTaskCc(NodeModel nodeModel, Execution execution) {
        if (ObjectUtils.isNotEmpty(nodeModel.getNodeUserList())) {
            Long parentTaskId = execution.getFlwTask().getId();
            List<NodeAssignee> nodeUserList = nodeModel.getNodeUserList();
            for (NodeAssignee nodeUser : nodeUserList) {
                FlwTaskCc flwTaskCc = new FlwTaskCc();
                flwTaskCc.setCreateId(execution.getCreateId());
                flwTaskCc.setCreateBy(execution.getCreateBy());
                flwTaskCc.setCreateTime(DateUtils.getCurrentDate());
                flwTaskCc.setInstanceId(execution.getFlwInstance().getId());
                flwTaskCc.setParentTaskId(parentTaskId);
                flwTaskCc.setTaskName(nodeModel.getNodeName());
                flwTaskCc.setDisplayName(nodeModel.getNodeName());
                flwTaskCc.setActorId(nodeUser.getId());
                flwTaskCc.setActorName(nodeUser.getName());
                flwTaskCc.setTaskType(0);
                flwTaskCc.setTaskState(1);
                taskCcMapper.insert(flwTaskCc);
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
        flwTask.setCreateId(execution.getCreateId());
        flwTask.setCreateBy(execution.getCreateBy());
        flwTask.setCreateTime(DateUtils.getCurrentDate());
        flwTask.setInstanceId(execution.getFlwInstance().getId());
        flwTask.setTaskName(nodeModel.getNodeName());
        flwTask.setDisplayName(nodeModel.getNodeName());
        flwTask.setTaskType(nodeModel.getType());
        flwTask.setParentTaskId(execution.getFlwTask() == null ? 0L : execution.getFlwTask().getId());
        return flwTask;
    }

    /**
     * 保存任务及参与者信息
     *
     * @param flwTask    任务对象
     * @param taskActors 参与者ID集合
     * @return
     */
    protected List<FlwTask> saveTask(FlwTask flwTask, PerformType performType, List<FlwTaskActor> taskActors, Execution execution) {
        List<FlwTask> flwTasks = new ArrayList<>();
        flwTask.setPerformType(performType);
        if (performType == PerformType.unknown) {
            // 发起、其它
            flwTask.setVariable(execution.getArgs());
            taskMapper.insert(flwTask);
            if (ObjectUtils.isNotEmpty(taskActors)) {
                // 发起人保存参与者
                taskActors.forEach(t -> this.assignTask(flwTask.getInstanceId(), flwTask.getId(), t));
            }
            flwTasks.add(flwTask);
            return flwTasks;
        }

        Assert.isTrue(ObjectUtils.isEmpty(taskActors), "任务参与者不能为空");
        if (performType == PerformType.orSign) {
            /**
             * 或签一条任务多个参与者
             */
            taskMapper.insert(flwTask);
            taskActors.forEach(t -> this.assignTask(flwTask.getInstanceId(), flwTask.getId(), t));
            flwTasks.add(flwTask);

            // 创建任务监听
            this.taskNotify(EventType.create, flwTask);
            return flwTasks;
        }

        if (performType == PerformType.sort) {
            /**
             * 按顺序依次审批，一个任务按顺序多个参与者依次添加
             */
            taskMapper.insert(flwTask);
            flwTasks.add(flwTask);

            // 分配一个参与者
            FlwTaskActor nextFlwTaskActor = null;
            if (null != execution) {
                nextFlwTaskActor = execution.getNextFlwTaskActor();
            }
            this.assignTask(flwTask.getInstanceId(), flwTask.getId(), null == nextFlwTaskActor ? taskActors.get(0) : nextFlwTaskActor);

            // 创建任务监听
            this.taskNotify(EventType.create, flwTask);
            return flwTasks;
        }

        /**
         * 会签（票签）每个参与者生成一条任务
         */
        taskActors.forEach(t -> {
            FlwTask newFlwTask = flwTask.cloneTask(null);
            taskMapper.insert(newFlwTask);
            flwTasks.add(newFlwTask);

            // 分配参与者
            this.assignTask(newFlwTask.getInstanceId(), newFlwTask.getId(), t);

            // 创建任务监听
            this.taskNotify(EventType.create, newFlwTask);
        });
        return flwTasks;
    }

    /**
     * 根据 taskId、createId 判断创建人是否允许执行任务
     *
     * @param flwTask 任务对象
     * @param userId  用户ID
     * @return
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
        List<FlwTaskActor> actors = taskActorMapper.selectListByTaskId(flwTask.getId());
        if (ObjectUtils.isEmpty(actors)) {
            // 未设置参与者，默认返回 true
            return true;
        }
        return taskAccessStrategy.isAllowed(userId, actors);
    }

    /**
     * 向指定的任务ID添加参与者
     *
     * @param taskId           任务ID
     * @param flwHisTaskActors 参与者列表
     */
    @Override
    public boolean addTaskActor(Long taskId, PerformType performType, List<FlwHisTaskActor> flwHisTaskActors) {
        FlwTask flwTask = taskMapper.getCheckById(taskId);
        List<FlwTaskActor> taskActorList = this.getTaskActorsByTaskId(taskId);
        Map<String, FlwTaskActor> taskActorMap = taskActorList.stream().collect(Collectors.toMap(FlwTaskActor::getActorId, t -> t));
        for (FlwTaskActor flwHisTaskActor : flwHisTaskActors) {
            // 不存在的参与者
            if (null == taskActorMap.get(flwHisTaskActor.getActorId())) {
                this.assignTask(flwTask.getInstanceId(), taskId, flwHisTaskActor);
            }
        }
        // 更新任务参与类型
        FlwTask temp = new FlwTask();
        temp.setId(taskId);
        temp.setPerformType(performType);
        return taskMapper.updateById(temp) > 0;
    }

    protected List<FlwTaskActor> getTaskActorsByTaskId(Long taskId) {
        List<FlwTaskActor> taskActorList = taskActorMapper.selectListByTaskId(taskId);
        Assert.isTrue(ObjectUtils.isEmpty(taskActorList), "not found task actor");
        return taskActorList;
    }

    @Override
    public boolean removeTaskActor(Long taskId, List<String> actorIds) {
        List<FlwTaskActor> taskActorList = this.getTaskActorsByTaskId(taskId);
        Assert.isTrue(Objects.equals(actorIds.size(), taskActorList.size()), "cannot all be deleted");

        // 删除参与者表，任务关联关系
        taskActorMapper.delete(Wrappers.<FlwTaskActor>lambdaQuery().eq(FlwTaskActor::getTaskId, taskId).in(FlwTaskActor::getActorId, actorIds));
        return true;
    }

    /**
     * 级联删除 flw_his_task, flw_his_task_actor, flw_task, flw_task_cc, flw_task_actor
     *
     * @param instanceId 流程实例ID
     */
    @Override
    public void cascadeRemoveByInstanceId(Long instanceId) {
        // 删除历史任务及参与者
        List<FlwHisTask> hisTaskList = hisTaskMapper.selectList(Wrappers.<FlwHisTask>lambdaQuery().select(FlwHisTask::getId).eq(FlwHisTask::getInstanceId, instanceId));
        if (ObjectUtils.isNotEmpty(hisTaskList)) {
            List<Long> hisTaskIds = hisTaskList.stream().map(t -> t.getId()).collect(Collectors.toList());
            hisTaskActorMapper.deleteByTaskIds(hisTaskIds);
            hisTaskMapper.delete(Wrappers.<FlwHisTask>lambdaQuery().eq(FlwHisTask::getInstanceId, instanceId));
        }

        // 删除任务及参与者
        List<FlwTask> flwTaskList = taskMapper.selectList(Wrappers.<FlwTask>lambdaQuery().select(FlwTask::getId).eq(FlwTask::getInstanceId, instanceId));
        if (ObjectUtils.isNotEmpty(flwTaskList)) {
            List<Long> taskIds = flwTaskList.stream().map(t -> t.getId()).collect(Collectors.toList());
            taskActorMapper.delete(Wrappers.<FlwTaskActor>lambdaQuery().in(FlwTaskActor::getTaskId, taskIds));
            taskMapper.delete(Wrappers.<FlwTask>lambdaQuery().eq(FlwTask::getInstanceId, instanceId));
        }

        // 删除任务抄送
        // TODO
    }

}
