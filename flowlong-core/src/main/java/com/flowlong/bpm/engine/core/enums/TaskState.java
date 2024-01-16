/*
 * Copyright 2023-2025 Licensed under the AGPL License
 */
package com.flowlong.bpm.engine.core.enums;

import lombok.Getter;

import java.util.Arrays;

/**
 * 流程状态
 *
 * <p>
 * 尊重知识产权，不允许非法使用，后果自负
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
    terminate(6);

    private final int value;

    TaskState(int value) {
        this.value = value;
    }

    public static TaskState get(int value) {
        return Arrays.stream(TaskState.values()).filter(s -> s.getValue() == value).findFirst().orElseGet(null);
    }

    public static TaskState of(InstanceState instanceState) {
        if (instanceState == InstanceState.reject) {
            return reject;
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
