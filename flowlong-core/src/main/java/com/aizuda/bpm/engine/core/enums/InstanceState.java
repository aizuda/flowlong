/*
 * Copyright 2023-2025 Licensed under the Dual Licensing
 * website: https://aizuda.com
 */
package com.aizuda.bpm.engine.core.enums;

import lombok.Getter;

import java.util.Arrays;
import java.util.Objects;

/**
 * 流程实例状态
 *
 * <p>
 * <a href="https://aizuda.com">官网</a>尊重知识产权，不允许非法使用，后果自负
 * </p>
 *
 * @author 江涛
 * @since 1.0
 */
@Getter
public enum InstanceState {
    /**
     * 作废状态，删除当前任务，保留了历史审批任务
     */
    destroy(-3),
    /**
     * 已暂停状态，被主动挂起，暂停执行
     */
    suspend(-2),
    /**
     * 暂存待审
     */
    saveAsDraft(-1),
    /**
     * 审批中
     */
    active(0),
    /**
     * 审批通过
     */
    complete(1),
    /**
     * 审批拒绝【 驳回结束流程 】
     */
    reject(2),
    /**
     * 撤销审批
     */
    revoke(3),
    /**
     * 超时结束
     */
    timeout(4),
    /**
     * 强制终止
     */
    terminate(5),
    /**
     * 自动通过
     */
    autoPass(6),
    /**
     * 自动拒绝
     */
    autoReject(7);

    private final int value;

    InstanceState(int value) {
        this.value = value;
    }

    public boolean ne(Integer value) {
        return !eq(value);
    }

    public boolean eq(Integer value) {
        return Objects.equals(this.value, value);
    }

    public static InstanceState get(int value) {
        return Arrays.stream(InstanceState.values()).filter(s -> s.getValue() == value).findFirst().orElse(null);
    }
}
