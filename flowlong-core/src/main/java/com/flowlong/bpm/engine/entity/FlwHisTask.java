/* 
 * Copyright 2023-2025 Licensed under the AGPL License
 */
package com.flowlong.bpm.engine.entity;

import com.flowlong.bpm.engine.assist.Assert;
import com.flowlong.bpm.engine.core.FlowCreator;
import com.flowlong.bpm.engine.core.enums.TaskState;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

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
     * 任务状态 0，活动 1，结束 2，超时 3，终止
     */
    protected Integer taskState;

    public void setTaskState(TaskState taskState) {
        this.taskState = taskState.getValue();
    }

    public void setTaskState(Integer taskState) {
        Assert.isNull(TaskState.get(taskState), "插入的实例状态异常 [taskState=" + taskState + "]");
        this.taskState = taskState;
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
        hisTask.displayName = flwTask.getDisplayName();
        hisTask.taskType = flwTask.getTaskType();
        hisTask.performType = flwTask.getPerformType();
        hisTask.actionUrl = flwTask.getActionUrl();
        hisTask.variable = flwTask.getVariable();
        hisTask.expireTime = flwTask.getExpireTime();
        return hisTask;
    }

    /**
     * 根据历史任务产生撤回的任务对象
     *
     * @return 任务对象
     */
    public FlwTask undoTask(FlowCreator flowCreator) {
        return cloneTask(flowCreator.getCreateId(), flowCreator.getCreateBy());
    }

}
