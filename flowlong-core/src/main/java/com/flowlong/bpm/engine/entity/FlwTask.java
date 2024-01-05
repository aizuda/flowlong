/*
 * Copyright 2023-2025 Licensed under the AGPL License
 */
package com.flowlong.bpm.engine.entity;

import com.flowlong.bpm.engine.assist.Assert;
import com.flowlong.bpm.engine.assist.DateUtils;
import com.flowlong.bpm.engine.core.FlowLongContext;
import com.flowlong.bpm.engine.core.enums.PerformType;
import com.flowlong.bpm.engine.core.enums.TaskType;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.Objects;

/**
 * 任务实体类
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
public class FlwTask extends FlowEntity {
    /**
     * 流程实例ID
     */
    protected Long instanceId;
    /**
     * 父任务ID
     */
    protected Long parentTaskId;
    /**
     * 任务名称
     */
    protected String taskName;
    /**
     * 任务显示名称
     */
    protected String displayName;
    /**
     * 任务类型 {@link com.flowlong.bpm.engine.core.enums.TaskType}
     */
    protected Integer taskType;
    /**
     * 参与方式 {@link com.flowlong.bpm.engine.core.enums.PerformType}
     */
    protected Integer performType;
    /**
     * 任务关联的表单url
     */
    protected String actionUrl;
    /**
     * 变量json
     */
    protected String variable;
    /**
     * 委托人ID
     */
    protected String assignorId;
    /**
     * 委托人
     */
    protected String assignor;
    /**
     * 期望任务完成时间
     */
    protected Date expireTime;
    /**
     * 提醒时间
     */
    protected Date remindTime;
    /**
     * 提醒次数
     */
    protected Integer remindRepeat;
    /**
     * 已阅 0，否 1，是
     */
    protected Integer viewed;

    public boolean major() {
        return Objects.equals(this.taskType, TaskType.major.getValue());
    }

    public Map<String, Object> variableMap() {
        Map<String, Object> map = FlowLongContext.fromJson(this.variable, Map.class);
        return null == map ? Collections.emptyMap() : map;
    }

    public void setVariable(String variable) {
        this.variable = variable;
    }

    public void setTaskType(TaskType taskType) {
        this.taskType = taskType.getValue();
    }

    public void setTaskType(Integer taskType) {
        Assert.isNull(TaskType.get(taskType), "illegal type [taskType=" + taskType + "]");
        this.taskType = taskType;
    }

    public void setPerformType(PerformType performType) {
        this.performType = performType.getValue();
    }

    public void setPerformType(Integer performType) {
        Assert.isNull(PerformType.get(performType), "illegal type [performType=" + performType + "]");
        this.performType = performType;
    }

    public void setVariable(Map<String, Object> args) {
        if (null != args && !args.isEmpty()) {
            this.variable = FlowLongContext.toJson(args);
        }
    }

    /**
     * 开始节点判断
     *
     * @return true 是 false 非
     */
    public boolean startNode() {
        return Objects.equals(0L, this.parentTaskId);
    }

    public FlwTask cloneTask(FlwHisTaskActor flwHisTaskActor) {
        if (null != flwHisTaskActor) {
            this.createId = flwHisTaskActor.getActorId();
            this.createBy = flwHisTaskActor.getActorName();
        }
        return cloneTask(createId, createBy);
    }

    public FlwTask cloneTask(String createId, String createBy) {
        FlwTask newFlwTask = new FlwTask();
        newFlwTask.setTenantId(tenantId);
        newFlwTask.setInstanceId(instanceId);
        newFlwTask.setParentTaskId(parentTaskId);
        newFlwTask.setTaskName(taskName);
        newFlwTask.setDisplayName(displayName);
        newFlwTask.setTaskType(taskType);
        newFlwTask.setPerformType(performType);
        newFlwTask.setActionUrl(actionUrl);
        newFlwTask.setVariable(variable);
        newFlwTask.setAssignorId(assignorId);
        newFlwTask.setAssignor(assignor);
        newFlwTask.setExpireTime(expireTime);
        newFlwTask.setRemindTime(remindTime);
        newFlwTask.setRemindRepeat(remindRepeat);
        newFlwTask.setViewed(viewed);
        newFlwTask.setCreateId(createId);
        newFlwTask.setCreateBy(createBy);
        newFlwTask.setCreateTime(DateUtils.getCurrentDate());
        return newFlwTask;
    }
}
