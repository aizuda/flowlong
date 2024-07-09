/*
 * Copyright 2023-2025 Licensed under the AGPL License
 */
package com.aizuda.bpm.engine.entity;

import com.aizuda.bpm.engine.FlowConstants;
import com.aizuda.bpm.engine.ProcessModelCache;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

/**
 * 扩展流程实例实体类
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
public class FlwExtInstance implements ProcessModelCache, Serializable {
    /**
     * 主键ID
     */
    private Long id;
    /**
     * 租户ID
     */
    private String tenantId;
    /**
     * 流程定义ID
     */
    private Long processId;
    /**
     * 流程定义类型（冗余业务直接可用）
     */
    protected String processType;
    /**
     * 流程模型定义JSON内容
     * <p>
     * 在发起的时候拷贝自流程定义模型内容，用于记录当前实例节点的动态改变。
     * </p>
     */
    private String modelContent;

    public static FlwExtInstance of(FlwInstance flwInstance, FlwProcess flwProcess) {
        FlwExtInstance ext = new FlwExtInstance();
        ext.id = flwInstance.getId();
        ext.tenantId = flwInstance.getTenantId();
        ext.processId = flwInstance.getProcessId();
        ext.processType = flwProcess.getProcessType();
        ext.modelContent = flwProcess.getModelContent();
        return ext;
    }

    @Override
    public String modelCacheKey() {
        return FlowConstants.processInstanceCacheKey + this.id;
    }
}
