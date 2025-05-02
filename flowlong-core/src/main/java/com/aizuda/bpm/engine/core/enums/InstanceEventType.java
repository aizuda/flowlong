/*
 * Copyright 2023-2025 Licensed under the Dual Licensing
 * website: https://aizuda.com
 */
package com.aizuda.bpm.engine.core.enums;

/**
 * 流程引擎监听类型
 *
 * <p>
 * <a href="https://aizuda.com">官网</a>尊重知识产权，不允许非法使用，后果自负
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
     * 暂停，被主动挂起
     */
    suspend,
    /**
     * 强制完成
     * <p>
     * 被终止的具体原因查看流程实例状态 {@link InstanceState}
     * </p>
     */
    forceComplete,
    /**
     * 驳回完成
     */
    rejectComplete,
    /**
     * 撤销完成
     */
    revokeComplete,
    /**
     * 超时完成
     */
    timeoutComplete,
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
