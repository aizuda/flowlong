/*
 * Copyright 2023-2025 Licensed under the AGPL License
 */
package com.aizuda.bpm.engine.entity;

import com.aizuda.bpm.engine.assist.Assert;
import com.aizuda.bpm.engine.assist.DateUtils;
import com.aizuda.bpm.engine.core.enums.TaskState;
import com.aizuda.bpm.engine.model.NodeModel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;

/**
 * 历史任务实体类
 *
 * <p>
 * 尊重知识产权，不允许非法使用，后果自负
 * </p>
 *
 * @author hubin
 * @since 1.0
 */
@Getter
@Setter
@ToString
public class FlwHisTask extends FlwTask {
    /**
     * 调用外部流程定义ID
     */
    protected Long callProcessId;
    /**
     * 调用外部流程实例ID
     */
    protected Long callInstanceId;
    /**
     * 完成时间
     */
    protected Date finishTime;
    /**
     * 任务状态 0，活动 1，跳转 2，完成 3，拒绝 4，撤销审批  5，超时 6，终止 7，驳回终止
     */
    protected Integer taskState;
    /**
     * 处理耗时
     */
    protected Long duration;

    public FlwHisTask setTaskState(TaskState taskState) {
        this.taskState = taskState.getValue();
        return this;
    }

    public FlwHisTask setTaskState(Integer taskState) {
        Assert.isNull(TaskState.get(taskState), "插入的实例状态异常 [taskState=" + taskState + "]");
        this.taskState = taskState;
        return this;
    }

    public static FlwHisTask of(FlwTask flwTask, TaskState taskState) {
        return of(flwTask).setTaskState(taskState);
    }

    public static FlwHisTask of(FlwTask flwTask) {
        FlwHisTask hisTask = new FlwHisTask();
        hisTask.id = flwTask.getId();
        hisTask.tenantId = flwTask.getTenantId();
        hisTask.createId = flwTask.getCreateId();
        hisTask.createBy = flwTask.getCreateBy();
        hisTask.createTime = flwTask.getCreateTime();
        hisTask.instanceId = flwTask.getInstanceId();
        hisTask.parentTaskId = flwTask.getParentTaskId();
        hisTask.taskName = flwTask.getTaskName();
        hisTask.taskKey = flwTask.getTaskKey();
        hisTask.taskType = flwTask.getTaskType();
        hisTask.performType = flwTask.getPerformType();
        hisTask.actionUrl = flwTask.getActionUrl();
        hisTask.variable = flwTask.getVariable();
        hisTask.assignorId = flwTask.getAssignorId();
        hisTask.assignor = flwTask.getAssignor();
        hisTask.expireTime = flwTask.getExpireTime();
        hisTask.remindTime = flwTask.getRemindTime();
        hisTask.remindRepeat = flwTask.getRemindRepeat();
        hisTask.viewed = flwTask.getViewed();
        return hisTask;
    }

    public static FlwHisTask ofCallInstance(NodeModel nodeModel, FlwInstance instance) {
        FlwHisTask flwHisTask = new FlwHisTask();
        flwHisTask.setTenantId(instance.getTenantId());
        flwHisTask.setCreateId(instance.getCreateId());
        flwHisTask.setCreateBy(instance.getCreateBy());
        flwHisTask.setCreateTime(instance.getCreateTime());
        flwHisTask.setInstanceId(instance.getParentInstanceId());
        flwHisTask.setTaskName(nodeModel.getNodeName());
        flwHisTask.setTaskKey(nodeModel.getNodeKey());
        flwHisTask.setCallProcessId(instance.getProcessId());
        flwHisTask.setCallInstanceId(instance.getId());
        flwHisTask.setTaskType(nodeModel.getType());
        return flwHisTask;
    }

    /**
     * 根据历史任务产生撤回的任务对象
     * <p>
     * 创建人信息保留
     * </p>
     *
     * @return 任务对象
     */
    public FlwTask undoTask() {
        return cloneTask(this.createId, this.createBy);
    }

    /**
     * 计算流程实例处理耗时
     */
    public void calculateDuration() {
        this.finishTime = DateUtils.getCurrentDate();
        this.duration = DateUtils.calculateDateDifference(this.createTime, this.finishTime);
    }
}
