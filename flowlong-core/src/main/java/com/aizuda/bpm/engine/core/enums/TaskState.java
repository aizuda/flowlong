/*
 * Copyright 2023-2025 Licensed under the Dual Licensing
 * website: https://aizuda.com
 */
package com.aizuda.bpm.engine.core.enums;

import lombok.Getter;

import java.util.Arrays;
import java.util.Objects;

/**
 * 流程状态
 *
 * <p>
 * <a href="https://aizuda.com">官网</a>尊重知识产权，不允许非法使用，后果自负
 * </p>
 *
 * @author 江涛
 * @since 1.0
 */
@Getter
public enum TaskState {
    /**
     * 活动
     */
    active(0),
    /**
     * 跳转
     */
    jump(1),
    /**
     * 完成
     */
    complete(2),
    /**
     * 拒绝
     */
    reject(3),
    /**
     * 撤销审批
     */
    revoke(4),
    /**
     * 超时
     */
    timeout(5),
    /**
     * 终止
     */
    terminate(6),
    /**
     * 驳回终止
     */
    rejectEnd(7),
    /**
     * 自动完成
     */
    autoComplete(8),
    /**
     * 自动驳回
     */
    autoReject(9),
    /**
     * 自动跳转
     */
    autoJump(10),
    /**
     * 驳回跳转
     */
    rejectJump(11),
    /**
     * 驳回重新审批跳转
     */
    reApproveJump(12),
    /**
     * 路由跳转
     */
    routeJump(13);

    private final int value;

    TaskState(int value) {
        this.value = value;
    }

    public boolean ne(Integer value) {
        return !eq(value);
    }

    public boolean eq(Integer value) {
        return Objects.equals(this.value, value);
    }

    public static TaskState get(int value) {
        return Arrays.stream(TaskState.values()).filter(s -> s.getValue() == value).findFirst().orElse(null);
    }

    public static TaskState of(InstanceState instanceState) {
        if (instanceState == InstanceState.reject) {
            return rejectEnd;
        }
        if (instanceState == InstanceState.revoke) {
            return revoke;
        }
        if (instanceState == InstanceState.timeout) {
            return timeout;
        }
        if (instanceState == InstanceState.terminate) {
            return terminate;
        }
        return complete;
    }

    public static boolean allowedCheck(TaskState taskState) {
        return active == taskState || jump == taskState || complete == taskState;
    }
}
