/*
 * Copyright 2023-2025 Licensed under the Dual Licensing
 * website: https://aizuda.com
 */
package com.aizuda.bpm.engine.entity;

import com.aizuda.bpm.engine.assist.Assert;
import com.aizuda.bpm.engine.assist.DateUtils;
import com.aizuda.bpm.engine.core.FlowLongContext;
import com.aizuda.bpm.engine.core.enums.PerformType;
import com.aizuda.bpm.engine.core.enums.TaskType;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;
import java.util.Map;
import java.util.Objects;

/**
 * 任务实体类
 *
 * <p>
 * <a href="https://aizuda.com">官网</a>尊重知识产权，不允许非法使用，后果自负
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
     * 任务 key 唯一标识
     */
    protected String taskKey;
    /**
     * 任务类型 {@link TaskType}
     */
    protected Integer taskType;
    /**
     * 参与方式 {@link PerformType}
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

    @SuppressWarnings({"all"})
    public Map<String, Object> variableMap() {
        if (null == this.variable) {
            return null;
        }
        return FlowLongContext.fromJson(this.variable, Map.class);
    }

    public void taskType(TaskType taskType) {
        this.taskType = taskType.getValue();
    }

    public void setTaskType(Integer taskType) {
        Assert.isNull(TaskType.get(taskType), "illegal type [taskType=" + taskType + "]");
        this.taskType = taskType;
    }

    public void performType(PerformType performType) {
        this.performType = performType.getValue();
    }

    public void setPerformType(Integer performType) {
        Assert.isNull(PerformType.get(performType), "illegal type [performType=" + performType + "]");
        this.performType = performType;
    }

    public void putAllVariable(Map<String, Object> args) {
        this.variable = FlowLongContext.putAllVariable(this.variable, args);
    }

    /**
     * 从扩展配置中加载期望任务完成时间
     *
     * @param extendConfig 扩展配置
     * @param checkEmpty   检查是否为空
     */
    public void loadExpireTime(Map<String, Object> extendConfig, boolean checkEmpty) {
        Date expireTime = null;
        if (null != extendConfig) {
            String time = (String) extendConfig.get("time");
            if (null != time) {
                expireTime = DateUtils.parseTimerTaskTime(time);
            }
        }
        if (checkEmpty) {
            Assert.isEmpty(expireTime, "Timer task config error");
        }
        this.expireTime = expireTime;
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
        newFlwTask.setTaskKey(taskKey);
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
