/* 
 * Copyright 2023-2025 Licensed under the AGPL License
 */
package com.flowlong.bpm.engine.core;

import lombok.Getter;

import java.io.Serializable;

/**
 * 流程创建者
 *
 * <p>
 * 尊重知识产权，不允许非法使用，后果自负
 * </p>
 *
 * @author hubin
 * @since 1.0
 */
@Getter
public class FlowCreator implements Serializable {
    /**
     * 租户ID
     */
    private String tenantId;
    /**
     * 创建人ID
     */
    private String createId;
    /**
     * 创建人
     */
    private String createBy;

    public static FlowCreator ADMIN = new FlowCreator("0", "管理员");

    public FlowCreator(String createId, String createBy) {
        this.createId = createId;
        this.createBy = createBy;
    }

    public FlowCreator tenantId(String tenantId) {
        this.tenantId = tenantId;
        return this;
    }

    public static FlowCreator of(String createId, String createBy) {
        return new FlowCreator(createId, createBy);
    }
}
