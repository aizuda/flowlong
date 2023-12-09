/* 
 * Copyright 2023-2025 Licensed under the AGPL License
 */
package com.flowlong.bpm.engine.entity;

import com.flowlong.bpm.engine.assist.Assert;
import com.flowlong.bpm.engine.assist.DateUtils;
import com.flowlong.bpm.engine.core.enums.InstanceState;
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
        FlwHisInstance hisInstance = new FlwHisInstance();
        hisInstance.id = flwInstance.getId();
        hisInstance.tenantId = flwInstance.getTenantId();
        hisInstance.createId = flwInstance.getCreateId();
        hisInstance.createBy = flwInstance.getCreateBy();
        hisInstance.createTime = flwInstance.getCreateTime();
        hisInstance.processId = flwInstance.getProcessId();
        hisInstance.priority = flwInstance.getPriority();
        hisInstance.instanceNo = flwInstance.getInstanceNo();
        hisInstance.businessKey = flwInstance.getBusinessKey();
        hisInstance.variable = flwInstance.getVariable();
        hisInstance.instanceVersion = flwInstance.getInstanceVersion();
        hisInstance.expireTime = flwInstance.getExpireTime();
        hisInstance.lastUpdateBy = flwInstance.getLastUpdateBy();
        hisInstance.lastUpdateTime = flwInstance.getLastUpdateTime();
        hisInstance.instanceState = instanceState.getValue();
        hisInstance.endTime = DateUtils.getCurrentDate();
        return hisInstance;
    }
}
