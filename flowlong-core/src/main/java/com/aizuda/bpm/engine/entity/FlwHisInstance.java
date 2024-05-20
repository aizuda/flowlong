/*
 * Copyright 2023-2025 Licensed under the AGPL License
 */
package com.aizuda.bpm.engine.entity;

import com.aizuda.bpm.engine.assist.Assert;
import com.aizuda.bpm.engine.assist.DateUtils;
import com.aizuda.bpm.engine.core.enums.InstanceState;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;

/**
 * 历史流程实例实体类
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
public class FlwHisInstance extends FlwInstance {
    /**
     * 状态 0，活动 1，结束
     */
    protected Integer instanceState;
    /**
     * 结束时间
     */
    protected Date endTime;
    /**
     * 处理耗时
     */
    protected Long duration;

    public void setVariable(Integer instanceState) {
        this.instanceState = instanceState;
    }

    public void setInstanceState(InstanceState instanceState) {
        this.instanceState = instanceState.getValue();
    }

    public void setInstanceState(Integer instanceState) {
        Assert.isNull(InstanceState.get(instanceState), "插入的实例状态异常 [instanceState=" + instanceState + "]");
        this.instanceState = instanceState;
    }

    public static FlwHisInstance of(FlwInstance flwInstance, InstanceState instanceState) {
        FlwHisInstance his = new FlwHisInstance();
        his.id = flwInstance.getId();
        his.tenantId = flwInstance.getTenantId();
        his.createId = flwInstance.getCreateId();
        his.createBy = flwInstance.getCreateBy();
        his.createTime = flwInstance.getCreateTime();
        his.processId = flwInstance.getProcessId();
        his.parentInstanceId = flwInstance.getParentInstanceId();
        his.priority = flwInstance.getPriority();
        his.instanceNo = flwInstance.getInstanceNo();
        his.businessKey = flwInstance.getBusinessKey();
        his.variable = flwInstance.getVariable();
        his.currentNodeName = flwInstance.getCurrentNodeName();
        his.currentNodeKey = flwInstance.getCurrentNodeKey();
        his.expireTime = flwInstance.getExpireTime();
        his.lastUpdateBy = flwInstance.getLastUpdateBy();
        his.lastUpdateTime = flwInstance.getLastUpdateTime();
        his.instanceState = instanceState.getValue();
        if (InstanceState.active != instanceState) {
            his.calculateDuration();
        }
        return his;
    }

    /**
     * 计算流程实例处理耗时
     */
    public void calculateDuration() {
        this.endTime = DateUtils.getCurrentDate();
        this.duration = DateUtils.calculateDateDifference(this.createTime, this.endTime);
    }
}
