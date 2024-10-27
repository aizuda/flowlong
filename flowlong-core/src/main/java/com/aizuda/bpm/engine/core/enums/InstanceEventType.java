/*
 * Copyright 2023-2025 Licensed under the AGPL License
 */
package com.aizuda.bpm.engine.core.enums;

/**
 * 流程引擎监听类型
 *
 * <p>
 * 尊重知识产权，不允许非法使用，后果自负
 * </p>
 *
 * @author hubin
 * @since 1.0
 */
public enum InstanceEventType {
    /**
     * 发起
     */
    start,
    /**
     * 强制完成
     * <p>
     * 被终止的具体原因查看流程实例状态 {@link InstanceState}
     * </p>
     */
    forceComplete,
    /**
     * 结束
     */
    end;

    public boolean eq(InstanceEventType eventType) {
        return this == eventType;
    }

    public boolean ne(InstanceEventType eventType) {
        return this != eventType;
    }
}
