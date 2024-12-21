/*
 * Copyright 2023-2025 Licensed under the Dual Licensing
 * website: https://aizuda.com
 */
package com.aizuda.bpm.engine.core.enums;

import lombok.Getter;

import java.util.Objects;

/**
 * 流程状态
 */
@Getter
public enum FlowState {
    /**
     * 启用
     */
    active(1),
    /**
     * 未启用
     */
    inactive(0),
    /**
     * 历史版本
     */
    history(2);

    private final int value;

    FlowState(int value) {
        this.value = value;
    }

    public boolean ne(Integer value) {
        return !eq(value);
    }

    public boolean eq(Integer value) {
        return Objects.equals(this.value, value);
    }
}
