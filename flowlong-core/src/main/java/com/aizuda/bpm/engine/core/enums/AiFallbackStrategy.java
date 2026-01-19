/*
 * Copyright 2023-2025 Licensed under the Dual Licensing
 * website: https://aizuda.com
 */
package com.aizuda.bpm.engine.core.enums;

import lombok.Getter;

/**
 * AI 降级策略枚举
 *
 * <p>
 * <a href="https://aizuda.com">官网</a>尊重知识产权，不允许非法使用，后果自负
 * </p>
 *
 * @author hubin
 * @since 1.0
 */
@Getter
public enum AiFallbackStrategy {

    /**
     * 默认通过：当AI处理失败时自动通过任务
     */
    DEFAULT_PASS("DEFAULT_PASS", "默认通过"),

    /**
     * 默认拒绝：当AI处理失败时自动拒绝任务
     */
    DEFAULT_REJECT("DEFAULT_REJECT", "默认拒绝"),

    /**
     * 转人工处理：当AI处理失败时转由人工处理
     */
    MANUAL("MANUAL", "转人工处理");

    private final String code;
    private final String description;

    AiFallbackStrategy(String code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 根据编码获取枚举值
     *
     * @param code 编码
     * @return 枚举值
     */
    public static AiFallbackStrategy of(String code) {
        for (AiFallbackStrategy strategy : values()) {
            if (strategy.code.equals(code)) {
                return strategy;
            }
        }
        throw new IllegalArgumentException("Invalid AI fallback strategy code: " + code);
    }

    /**
     * 判断是否匹配给定的编码
     *
     * @param code 编码
     * @return 是否匹配
     */
    public boolean eq(String code) {
        return this.code.equals(code);
    }
}
