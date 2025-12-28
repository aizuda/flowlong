/*
 * Copyright 2023-2025 Licensed under the Dual Licensing
 * website: https://aizuda.com
 */
package com.aizuda.bpm.engine.core.enums;

import lombok.Getter;

import java.util.Objects;

/**
 * 模型节点状态
 *
 * <p>
 * <a href="https://aizuda.com">官网</a>尊重知识产权，不允许非法使用，后果自负
 * </p>
 *
 * @author hubin
 * @since 1.0
 */
@Getter
public enum NodeState {
    /**
     * 初始建模节点
     */
    init(0),
    /**
     * 临时节点（允许加签、减签）
     */
    temp(1);

    private final int value;

    NodeState(int value) {
        this.value = value;
    }

    public boolean ne(Integer value) {
        return !eq(value);
    }

    public boolean eq(Integer value) {
        return Objects.equals(this.value, value);
    }

}
