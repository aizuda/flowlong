/*
 * Copyright 2023-2025 Licensed under the Dual Licensing
 * website: https://aizuda.com
 */
package com.aizuda.bpm.engine.entity;

import com.aizuda.bpm.engine.core.FlowCreator;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

/**
 * 流程表实体基类
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
public class FlowEntity implements Serializable {
    /**
     * 主键ID
     */
    protected Long id;
    /**
     * 租户ID
     */
    protected String tenantId;
    /**
     * 创建人ID
     */
    protected String createId;
    /**
     * 创建人名称
     */
    protected String createBy;
    /**
     * 创建时间
     */
    protected Date createTime;

    public void setFlowCreator(FlowCreator flowCreator) {
        this.tenantId = flowCreator.getTenantId();
        this.createId = flowCreator.getCreateId();
        this.createBy = flowCreator.getCreateBy();
    }
}
