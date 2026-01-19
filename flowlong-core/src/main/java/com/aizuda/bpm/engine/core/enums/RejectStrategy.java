/*
 * Copyright 2023-2025 Licensed under the Dual Licensing
 * website: https://aizuda.com
 */
package com.aizuda.bpm.engine.core.enums;

/**
 * 驳回策略
 *
 * <p>
 * <a href="https://aizuda.com">官网</a>尊重知识产权，不允许非法使用，后果自负
 * </p>
 *
 * @author hubin
 * @since 1.0
 */

import lombok.Getter;

/**
 * 驳回策略枚举
 */
@Getter
public enum RejectStrategy {
    /**
     * 驳回到发起人
     */
    TO_INITIATOR(1),

    /**
     * 驳回到上一节点
     */
    TO_PREVIOUS_NODE(2),

    /**
     * 驳回到指定节点
     */
    TO_SPECIFIED_NODE(3),

    /**
     * 终止审批流程
     */
    TERMINATE_APPROVAL(4),

    /**
     * 驳回到模型父节点
     */
    TO_PARENT_NODE(5);

    private final Integer value;

    RejectStrategy(Integer value) {
        this.value = value;
    }

    /**
     * 根据值获取枚举
     */
    public static RejectStrategy of(Integer value) {
        for (RejectStrategy strategy : values()) {
            if (strategy.value.equals(value)) {
                return strategy;
            }
        }
        throw new IllegalArgumentException("Invalid reject strategy value: " + value);
    }

    /**
     * 判断是否匹配给定的值
     */
    public boolean eq(Integer value) {
        return this.value.equals(value);
    }
}

