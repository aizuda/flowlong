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
import com.flowlong.bpm.engine.listener.TaskListener;
import com.flowlong.bpm.engine.model.NodeAssignee;
import com.flowlong.bpm.engine.model.NodeModel;
import com.flowlong.bpm.engine.model.ProcessModel;
import com.flowlong.bpm.mybatisplus.mapper.*;

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
    private final FlwProcessMapper processMapper;
    private final TaskListener taskListener;
    private final FlwInstanceMapper instanceMapper;
    private final FlwHisInstanceMapper hisInstanceMapper;
    private final FlwTaskMapper taskMapper;
    private final FlwTaskActorMapper taskActorMapper;
    private final FlwHisTaskMapper hisTaskMapper;
    private final FlwHisTaskActorMapper hisTaskActorMapper;

    public TaskServiceImpl(TaskAccessStrategy taskAccessStrategy, TaskListener taskListener, FlwProcessMapper processMapper,
                           FlwInstanceMapper instanceMapper, FlwHisInstanceMapper hisInstanceMapper, FlwTaskMapper taskMapper,
                           FlwTaskActorMapper taskActorMapper, FlwHisTaskMapper hisTaskMapper,
                           FlwHisTaskActorMapper hisTaskActorMapper) {
        this.taskAccessStrategy = taskAccessStrategy;
        this.processMapper = processMapper;
        this.taskListener = taskListener;
        this.instanceMapper = instanceMapper;
        this.hisInstanceMapper = hisInstanceMapper;
        this.taskMapper = taskMapper;
        this.taskActorMapper = taskActorMapper;
        this.hisTaskMapper = hisTaskMapper;
        this.hisTaskActorMapper = hisTaskActorMapper;
    }

    /**
     * 更新当前执行节点信息
     *
     * @param flwTask 当前所在执行任务
     */
    protected void updateCurrentNode(FlwTask flwTask) {
        FlwInstance flwInstance = new FlwInstance();
        flwInstance.setId(flwTask.getInstanceId());
        flwInstance.setCurrentNode(flwTask.getTaskName());
        flwInstance.setLastUpdateBy(flwTask.getCreateBy());
        flwInstance.setLastUpdateTime(DateUtils.getCurrentDate());
        instanceMapper.updateById(flwInstance);
        FlwHisInstance flwHisInstance = new FlwHisInstance();
        flwHisInstance.setId(flwInstance.getId());
        flwHisInstance.setCurrentNode(flwInstance.getCurrentNode());
        flwHisInstance.setLastUpdateBy(flwInstance.getLastUpdateBy());
        flwHisInstance.setLastUpdateTime(flwInstance.getLastUpdateTime());
        hisInstanceMapper.updateById(flwHisInstance);
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
        this.taskNotify(eventType, () -> flwTask, flowCreator);
        return flwTask;
    }

    /**
     * 执行节点跳转任务
     */
    @Override
    public boolean executeJumpTask(Long taskId, String nodeName, FlowCreator flowCreator, Function<FlwTask, Execution> executionFunction) {
        FlwTask flwTask = this.getAllowedFlwTask(taskId, flowCreator, null, null);

        // 执行跳转到目标节点
        Execution execution = executionFunction.apply(flwTask);
        ProcessModel processModel = execution.getProcessModel();
        Assert.isNull(processModel, "当前任务未找到流程定义模型");

        // 查找模型节点
        NodeModel nodeModel;
        if (null == nodeName) {
            // 1，找到当前节点的父节点
            nodeModel = processModel.getNode(flwTask.getTaskName()).getParentNode();
        } else {
            // 2，找到指定 nodeName 节点
            nodeModel = processModel.getNode(nodeName);
        }
        Assert.isNull(nodeModel, "根据节点名称[" + nodeName + "]无法找到节点模型");

        // 获取当前执行实例的所有正在执行的任务，强制终止执行并跳到指定节点
        this.getTasksByInstanceId(flwTask.getInstanceId()).forEach(t -> this.moveToHisTask(t, TaskState.jump, flowCreator));

        if (0 == nodeModel.getType()) {
            // 发起节点，创建发起任务，分配发起人
            FlwTask initiationTask = this.createTaskBase(nodeModel, execution);
            initiationTask.setPerformType(PerformType.start);
            Assert.isFalse(taskMapper.insert(initiationTask) > 0, "Failed to create initiation task");
            taskActorMapper.insert(FlwTaskActor.ofFlwInstance(execution.getFlwInstance(), initiationTask.getId()));
        } else {
            // 其它节点创建
            this.createTask(nodeModel, execution);
        }

        // 任务监听器通知
        this.taskNotify(EventType.jump, () -> flwTask, flowCreator);
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
        FlwTask flwTask = taskMapper.getCheckById(taskId);
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

    protected void taskNotify(EventType eventType, Supplier<FlwTask> supplier, FlowCreator flowCreator) {
        if (null != taskListener) {
            taskListener.notify(eventType, supplier, flowCreator);
        }
    }

    protected List<FlwTask> getTasksByInstanceId(Long instanceId) {
        return taskMapper.selectList(Wrappers.<FlwTask>lambdaQuery().eq(FlwTask::getInstanceId, instanceId));
    }

    /**
     * 完成指定实例ID活动任务
     *
     * @param instanceId 实例ID
     * @return true 成功 false 失败
     */
    @Override
    public boolean completeActiveTasksByInstanceId(Long instanceId, FlowCreator flowCreator) {
        List<FlwTask> flwTasks = taskMapper.selectList(Wrappers.<FlwTask>lambdaQuery().eq(FlwTask::getInstanceId, instanceId));
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
        taskMapper.updateById(flwTask);
        // 任务监听器通知
        this.taskNotify(EventType.update, () -> flwTask, flowCreator);
    }

    /**
     * 查看任务设置为已阅状态
     *
     * @param taskId    任务ID
     * @param taskActor 任务参与者
     */
    @Override
    public boolean viewTask(Long taskId, FlwTaskActor taskActor) {
        if (taskActorMapper.selectCount(Wrappers.<FlwTaskActor>lambdaQuery().eq(FlwTaskActor::getTaskId, taskId)
                .eq(FlwTaskActor::getActorId, taskActor.getActorId())) > 0) {
            /*
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
     * 根据 任务ID 认领任务，删除其它任务参与者
     */
    @Override
    public FlwTask claim(Long taskId, FlowCreator flowCreator) {
        FlwTask flwTask = taskMapper.getCheckById(taskId);
        if (!isAllowed(flwTask, flowCreator.getCreateId())) {
            Assert.illegal("当前执行用户ID [" + flowCreator.getCreateBy() + "] 不允许提取任务 [taskId=" + taskId + "]");
        }
        // 删除任务参与者
        taskActorMapper.deleteByTaskId(taskId);
        // 插入当前用户ID作为唯一参与者
        taskActorMapper.insert(FlwTaskActor.of(flowCreator, flwTask));

        // 任务监听器通知
        this.taskNotify(EventType.claim, () -> flwTask, flowCreator);
        return flwTask;
    }

    /**
     * 根据 任务ID 分配任务给指定办理人、重置任务类型
     *
     * @param taskId              任务ID
     * @param taskType            任务类型
     * @param flowCreator         任务参与者
     * @param assigneeFlowCreator 指定办理人
     * @return true 成功 false 失败
     */
    @Override
    public boolean assigneeTask(Long taskId, TaskType taskType, FlowCreator flowCreator, FlowCreator assigneeFlowCreator) {
        // 受理任务权限验证
        FlwTaskActor flwTaskActor = this.getAllowedFlwTaskActor(taskId, flowCreator);

        // 不允许重复分配
        FlwTask dbFlwTask = taskMapper.selectById(taskId);
        if (ObjectUtils.isNotEmpty(dbFlwTask.getAssignorId())) {
            Assert.illegal("Do not allow duplicate assign , taskId = " + taskId);
        }

        // 设置任务为委派任务或者为转办任务
        FlwTask flwTask = new FlwTask();
        flwTask.setId(taskId);
        flwTask.setTaskType(taskType);
        flwTask.setAssignorId(flowCreator.getCreateId());
        flwTask.setAssignor(flowCreator.getCreateBy());
        taskMapper.updateById(flwTask);

        // 删除任务历史参与者
        taskActorMapper.deleteById(flwTaskActor.getId());

        // 分配任务给办理人
        this.assignTask(flwTaskActor.getInstanceId(), taskId, FlwTaskActor.ofFlowCreator(assigneeFlowCreator));
        return true;
    }

    /**
     * 获取指定 任务ID 合法参与者对象
     *
     * @param taskId      任务ID
     * @param flowCreator 任务参与者
     */
    protected FlwTaskActor getAllowedFlwTaskActor(Long taskId, FlowCreator flowCreator) {
        List<FlwTaskActor> taskActors = taskActorMapper.selectList(Wrappers.<FlwTaskActor>lambdaQuery().eq(FlwTaskActor::getTaskId, taskId)
                .eq(FlwTaskActor::getActorId, flowCreator.getCreateId()));
        Optional<FlwTaskActor> taskActorOpt = taskActors.stream().filter(t -> Objects.equals(t.getActorId(), flowCreator.getCreateId())).findFirst();
        Assert.isTrue(!taskActorOpt.isPresent(), "Not authorized to perform this task");
        return taskActorOpt.get();
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
        FlwTask flwTask = taskMapper.getCheckById(taskId);

        // 任务归还至委托人
        FlwTaskActor taskActor = new FlwHisTaskActor();
        taskActor.setId(flwTaskActor.getId());
        taskActor.setActorId(flwTask.getAssignorId());
        taskActor.setActorName(flwTask.getAssignor());
        if (taskActorMapper.updateById(taskActor) > 0) {
            // 设置任务状态为委托归还，委托人设置为归还人
            FlwTask temp = new FlwTask();
            temp.setId(taskId);
            temp.setTaskType(TaskType.delegateReturn);
            temp.setAssignorId(flowCreator.getCreateId());
            temp.setAssignor(flowCreator.getCreateBy());
            Assert.isFalse(taskMapper.updateById(temp) > 0, "resolveTask failed");
        }
        return true;
    }

    /**
     * 拿回任务、根据历史任务ID撤回下一个节点的任务、恢复历史任务
     */
    @Override
    public Optional<FlwTask> reclaimTask(Long taskId, FlowCreator flowCreator) {
        Optional<FlwTask> flwTaskOptional = this.undoHisTask(taskId, flowCreator, hisTask -> {
            List<FlwTask> flwTaskList = taskMapper.selectListByInstanceId(hisTask.getInstanceId());
            Assert.isEmpty(flwTaskList, "No approval tasks found");
            Assert.isFalse(Objects.equals(flwTaskList.get(0).getParentTaskId(), taskId), "Do not allow cross level reclaim task");
            flwTaskList.forEach(flwTask -> this.moveToHisTask(flwTask, TaskState.revoke, flowCreator));
        });

        // 任务监听器通知
        flwTaskOptional.ifPresent(flwTask ->  this.taskNotify(EventType.reclaim, () -> flwTask, flowCreator));
        return flwTaskOptional;
    }

    /**
     * 唤醒指定的历史任务
     */
    @Override
    public FlwTask resume(Long taskId, FlowCreator flowCreator) {
        FlwHisTask histTask = hisTaskMapper.getCheckById(taskId);
        Assert.isTrue(ObjectUtils.isEmpty(histTask.getCreateBy()) || !Objects.equals(histTask.getCreateBy(), flowCreator.getCreateBy()),
                "当前参与者[" + flowCreator.getCreateBy() + "]不允许唤醒历史任务[taskId=" + taskId + "]");

        // 流程实例结束情况恢复流程实例
        FlwInstance flwInstance = instanceMapper.selectById(histTask.getInstanceId());
        Assert.isNull(flwInstance, "已结束流程任务不支持唤醒");

        // 历史任务恢复
        FlwTask flwTask = histTask.cloneTask(null);
        taskMapper.insert(flwTask);

        // 分配任务
        assignTask(flwTask.getInstanceId(), taskId, FlwTaskActor.of(flowCreator, flwTask));

        // 更新当前执行节点信息
        this.updateCurrentNode(flwTask);

        // 任务监听器通知
        this.taskNotify(EventType.resume, () -> flwTask, flowCreator);
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
            Assert.isEmpty(flwTasks, "后续活动任务已完成或不存在，无法撤回.");
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
        Assert.isTrue(currentFlwTask.startNode(), "上一步任务ID为空，无法驳回至上一步处理");

        // 执行任务驳回
        this.executeTask(currentFlwTask.getId(), flowCreator, args, TaskState.reject, EventType.reject);

        // 撤回至上一级任务
        Long parentTaskId = currentFlwTask.getParentTaskId();
        return this.undoHisTask(parentTaskId, flowCreator, null);
    }

    /**
     * 撤回历史任务
     *
     * @param hisTaskId       历史任务ID
     * @param flowCreator     任务创建者
     * @param hisTaskConsumer 历史任务业务处理
     */
    protected Optional<FlwTask> undoHisTask(Long hisTaskId, FlowCreator flowCreator, Consumer<FlwHisTask> hisTaskConsumer) {
        FlwHisTask hisTask = hisTaskMapper.getCheckById(hisTaskId);
        if (null != hisTaskConsumer) {
            hisTaskConsumer.accept(hisTask);
        }
        // 撤回历史任务
        FlwTask flwTask = hisTask.undoTask();
        taskMapper.insert(flwTask);
        if (flwTask.startNode()) {
            // 如果直接撤回到发起人，构建发起人关联信息
            taskActorMapper.insert(FlwTaskActor.ofFlwTask(flwTask));
        } else {
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
        }

        // 更新当前执行节点信息
        this.updateCurrentNode(flwTask);
        return Optional.of(flwTask);
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

        // 更新当前执行节点信息，抄送节点除外
        if (2 != nodeType) {
            this.updateCurrentNode(flwTask);
        }

        if (0 == nodeType) {
            /*
             * 0，发起人 （ 直接保存历史任务、执行进入下一个节点逻辑 ）
             */
            flwTasks.addAll(this.saveTask(flwTask, PerformType.start, taskActors, execution));

            /*
             * 执行进入下一个节点
             */
            nodeModel.nextNode().ifPresent(nextNode -> nextNode.execute(execution.getEngine().getContext(), execution));
        } else if (1 == nodeType) {
            /*
             * 1，审批人
             */
            PerformType performType = PerformType.get(nodeModel.getExamineMode());
            flwTasks.addAll(this.saveTask(flwTask, performType, taskActors, execution));
        } else if (2 == nodeType) {
            /*
             * 2，抄送任务
             */
            this.saveTaskCc(nodeModel, flwTask);

            // 任务监听器通知
            this.taskNotify(EventType.cc, () -> flwTask, execution.getFlowCreator());

            /*
             * 可能存在子节点
             */
            nodeModel.nextNode().ifPresent(nextNode -> nextNode.execute(execution.getEngine().getContext(), execution));
        } else if (3 == nodeType) {
            /*
             * 3，条件审批
             */
            FlwTask singleFlwTask = flwTask.cloneTask(null);
            PerformType performType = PerformType.get(nodeModel.getExamineMode());
            flwTasks.addAll(this.saveTask(singleFlwTask, performType, taskActors, execution));
        } else if (5 == nodeType) {
            /*
             * 5，办理子流程
             */
            FlowCreator flowCreator = execution.getFlowCreator();
            execution.getEngine().startInstanceByProcessKey(nodeModel.getCallProcessKey(), null, flowCreator, null, () -> {
                FlwInstance flwInstance = new FlwInstance();
                flwInstance.setParentInstanceId(flwTask.getInstanceId());
                return flwInstance;
            }).ifPresent(instance -> hisTaskMapper.insert(FlwHisTask.ofCallInstance(nodeModel, instance)));
        }

        return flwTasks;
    }

    /**
     * 保存抄送任务
     *
     * @param nodeModel 节点模型
     * @param flwTask   流程任务对象
     */
    public void saveTaskCc(NodeModel nodeModel, FlwTask flwTask) {
        List<NodeAssignee> nodeUserList = nodeModel.getNodeUserList();
        if (ObjectUtils.isNotEmpty(nodeUserList)) {
            // 抄送任务
            FlwHisTask flwHisTask = FlwHisTask.of(flwTask, TaskState.complete);
            flwHisTask.setTaskType(TaskType.cc);
            flwHisTask.setPerformType(PerformType.copy);
            flwHisTask.calculateDuration();
            hisTaskMapper.insert(flwHisTask);
            for (NodeAssignee nodeUser : nodeUserList) {
                hisTaskActorMapper.insert(FlwHisTaskActor.ofNodeAssignee(nodeUser, flwHisTask.getInstanceId(), flwHisTask.getId()));
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
        flwTask.setDisplayName(nodeModel.getNodeName());
        flwTask.setTaskType(nodeModel.getType());
        flwTask.setParentTaskId(execution.getFlwTask() == null ? 0L : execution.getFlwTask().getId());
        return flwTask;
    }

    /**
     * 保存任务及参与者信息
     *
     * @param flwTask    流程任务对象
     * @param taskActors 参与者ID集合
     * @return 流程任务列表
     */
    protected List<FlwTask> saveTask(FlwTask flwTask, PerformType performType, List<FlwTaskActor> taskActors, Execution execution) {
        List<FlwTask> flwTasks = new ArrayList<>();
        flwTask.setPerformType(performType);

        if (performType == PerformType.start) {
            // 发起任务
            FlwHisTask flwHisTask = FlwHisTask.of(flwTask, TaskState.complete);
            flwHisTask.calculateDuration();
            if (hisTaskMapper.insert(flwHisTask) > 0) {
                // 设置为执行任务
                execution.setFlwTask(flwHisTask);
                // 记录发起人
                hisTaskActorMapper.insert(FlwHisTaskActor.ofFlwHisTask(flwHisTask));
                flwTasks.add(flwTask);
            }
            return flwTasks;
        }


        if (performType == PerformType.unknown) {
            // 未知任务
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

        final FlowCreator flowCreator = execution.getFlowCreator();
        if (performType == PerformType.orSign) {
            /*
             * 或签一条任务多个参与者
             */
            taskMapper.insert(flwTask);
            taskActors.forEach(t -> this.assignTask(flwTask.getInstanceId(), flwTask.getId(), t));
            flwTasks.add(flwTask);

            // 创建任务监听
            this.taskNotify(EventType.create, () -> flwTask, flowCreator);
            return flwTasks;
        }

        if (performType == PerformType.sort) {
            /*
             * 按顺序依次审批，一个任务按顺序多个参与者依次添加
             */
            taskMapper.insert(flwTask);
            flwTasks.add(flwTask);

            // 分配一个参与者
            FlwTaskActor nextFlwTaskActor = execution.getNextFlwTaskActor();
            this.assignTask(flwTask.getInstanceId(), flwTask.getId(), null == nextFlwTaskActor ? taskActors.get(0) : nextFlwTaskActor);

            // 创建任务监听
            this.taskNotify(EventType.create, () -> flwTask, flowCreator);
            return flwTasks;
        }

        /*
         * 会签（票签）每个参与者生成一条任务
         */
        taskActors.forEach(t -> {
            FlwTask newFlwTask = flwTask.cloneTask(null);
            taskMapper.insert(newFlwTask);
            flwTasks.add(newFlwTask);

            // 分配参与者
            this.assignTask(newFlwTask.getInstanceId(), newFlwTask.getId(), t);

            // 创建任务监听
            this.taskNotify(EventType.create, () -> newFlwTask, flowCreator);
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
     * @param taskId        任务ID
     * @param flwTaskActors 参与者列表
     */
    @Override
    public boolean addTaskActor(Long taskId, PerformType performType, List<FlwTaskActor> flwTaskActors) {
        FlwTask flwTask = taskMapper.getCheckById(taskId);
        List<FlwTaskActor> taskActorList = this.getTaskActorsByTaskId(taskId);
        Map<String, FlwTaskActor> taskActorMap = taskActorList.stream().collect(Collectors.toMap(FlwTaskActor::getActorId, t -> t));
        for (FlwTaskActor flwTaskActor : flwTaskActors) {
            // 不存在的参与者
            if (null == taskActorMap.get(flwTaskActor.getActorId())) {
                this.assignTask(flwTask.getInstanceId(), taskId, flwTaskActor);
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
    public void endCallProcessTask(Long callProcessId, Long callInstanceId) {
        List<FlwHisTask> flwHisTasks = hisTaskMapper.selectList(Wrappers.<FlwHisTask>lambdaQuery()
                .eq(FlwHisTask::getCallProcessId, callProcessId)
                .eq(FlwHisTask::getCallInstanceId, callInstanceId));
        if (ObjectUtils.isNotEmpty(flwHisTasks)) {
            FlwHisTask dbHis = flwHisTasks.get(0);
            FlwHisTask his = new FlwHisTask();
            his.setId(dbHis.getId());
            his.setCreateTime(dbHis.getCreateTime());
            his.setTaskState(TaskState.complete);
            his.calculateDuration();
            his.setCreateTime(null);
            hisTaskMapper.updateById(his);
        }
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
     * 级联删除表 flw_his_task_actor, flw_his_task, flw_task_actor, flw_task, flw_task_cc, flw_his_instance, flw_instance
     *
     * @param instanceIds 流程实例ID列表
     */
    @Override
    public void cascadeRemoveByInstanceIds(List<Long> instanceIds) {
        // 删除历史任务及参与者
        hisTaskActorMapper.delete(Wrappers.<FlwHisTaskActor>lambdaQuery().in(FlwHisTaskActor::getInstanceId, instanceIds));
        hisTaskMapper.delete(Wrappers.<FlwHisTask>lambdaQuery().in(FlwHisTask::getInstanceId, instanceIds));

        // 删除任务及参与者
        taskActorMapper.delete(Wrappers.<FlwTaskActor>lambdaQuery().in(FlwTaskActor::getInstanceId, instanceIds));
        taskMapper.delete(Wrappers.<FlwTask>lambdaQuery().in(FlwTask::getInstanceId, instanceIds));
    }

}
