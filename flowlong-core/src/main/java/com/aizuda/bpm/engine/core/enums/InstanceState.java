/*
 * Copyright 2023-2025 Licensed under the apache-2.0 License
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
public enum InstanceState {
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
    terminate(5);

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
