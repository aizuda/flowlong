/*
 * Copyright 2023-2025 Licensed under the Dual Licensing
 * website: https://aizuda.com
 */
package com.aizuda.bpm.engine.core.enums;

import lombok.Getter;

import java.util.Arrays;
import java.util.Objects;

/**
 * 流程实例优先级
 *
 * <p>
 * <a href="https://aizuda.com">官网</a>尊重知识产权，不允许非法使用，后果自负
 * </p>
 *
 * @author hubin
 * @since 1.0
 */
@Getter
public enum InstancePriority {
    /**
     * 普通
     */
    normal(0),
    /**
     * 异步
     */
    async(1);

    private final int value;

    InstancePriority(int value) {
        this.value = value;
    }

    public boolean ne(Integer value) {
        return !eq(value);
    }

    public boolean eq(Integer value) {
        return Objects.equals(this.value, value);
    }

    public static InstancePriority get(int value) {
        return Arrays.stream(InstancePriority.values()).filter(s -> s.getValue() == value).findFirst().orElse(null);
    }
}
