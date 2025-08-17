/*
 * Copyright 2023-2025 Licensed under the Dual Licensing
 * website: https://aizuda.com
 */
package com.aizuda.bpm.engine.core;

import com.aizuda.bpm.engine.entity.FlwTaskActor;
import lombok.Getter;

import java.io.Serializable;

/**
 * 流程创建者
 *
 * <p>
 * <a href="https://aizuda.com">官网</a>尊重知识产权，不允许非法使用，后果自负
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
    /**
     * 初始化管理员，用于操作权限忽略等场景
     */
    public static final FlowCreator ADMIN = new FlowCreator("0", "admin");

    public FlowCreator(String createId, String createBy) {
        this.createId = createId;
        this.createBy = createBy;
    }

    public FlowCreator tenantId(String tenantId) {
        this.tenantId = tenantId;
        return this;
    }

    public static FlowCreator of(FlwTaskActor fta) {
        return of(fta.getTenantId(), fta.getActorId(), fta.getActorName());
    }

    public static FlowCreator of(String createId, String createBy) {
        return of(null, createId, createBy);
    }

    public static FlowCreator of(String tenantId, String createId, String createBy) {
        return new FlowCreator(createId, createBy).tenantId(tenantId);
    }
}
