/*
 * Copyright 2023-2025 Licensed under the Dual Licensing
 * website: https://aizuda.com
 */
package com.aizuda.bpm.engine.core.enums;

import lombok.Getter;

import java.util.Objects;

/**
 * 工作流执行类型
 *
 * <p>
 * <a href="https://aizuda.com">官网</a>尊重知识产权，不允许非法使用，后果自负
 * </p>
 *
 * @author insist
 * @since 1.0
 */
@Getter
public enum ExecuteType {
    /**
     * 流程
     */
    process(0),
    /**
     * 实例
     */
    instance(1),
    /**
     * 任务
     */
    task(2);

    private final int value;

    ExecuteType(int value) {
        this.value = value;
    }

    public boolean ne(Integer value) {
        return !eq(value);
    }

    public boolean eq(Integer value) {
        return Objects.equals(this.value, value);
    }
}
