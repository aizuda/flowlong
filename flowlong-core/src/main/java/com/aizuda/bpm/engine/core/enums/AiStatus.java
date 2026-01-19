/*
 * Copyright 2023-2025 Licensed under the Dual Licensing
 * website: https://aizuda.com
 */
package com.aizuda.bpm.engine.core.enums;

import lombok.Getter;

/**
 * AI 处理状态枚举
 *
 * <p>
 * <a href="https://aizuda.com">官网</a>尊重知识产权，不允许非法使用，后果自负
 * </p>
 *
 * @author hubin
 * @since 1.0
 */
@Getter
public enum AiStatus {
    /**
     * AI 处理成功
     */
    SUCCESS(0),
    /**
     * AI 处理失败
     */
    FAILURE(1),
    /**
     * AI 置信度低，需人工复核
     */
    LOW_CONFIDENCE(2),
    /**
     * AI 处理超时
     */
    TIMEOUT(3),
    /**
     * AI 需要异步处理
     */
    ASYNC(4),
    /**
     * AI 降级到人工处理
     */
    FALLBACK(5);

    private final int value;

    AiStatus(int value) {
        this.value = value;
    }

    public boolean isSuccess() {
        return this == SUCCESS;
    }

    public boolean needManualReview() {
        return this == LOW_CONFIDENCE || this == FALLBACK || this == FAILURE;
    }

    public boolean isAsync() {
        return this == ASYNC;
    }
}
