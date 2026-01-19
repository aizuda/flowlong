/*
 * Copyright 2023-2025 Licensed under the Dual Licensing
 * website: https://aizuda.com
 */
package com.aizuda.bpm.engine.model;

import lombok.Data;

import java.io.Serializable;
import java.util.Map;

/**
 * AI 节点配置模型
 *
 * <p>
 * <a href="https://aizuda.com">官网</a>尊重知识产权，不允许非法使用，后果自负
 * </p>
 *
 * @author hubin
 * @since 1.0
 */
@Data
public class AiConfig implements Serializable {

    /**
     * AI 智能体标识（用于匹配外部 AI 服务）
     */
    private String agentId;

    /**
     * 提示词模板（支持 SpEL 表达式，如 ${args.reason}）
     */
    private String promptTemplate;

    /**
     * 置信度阈值（0.0-1.0），低于此值转人工复核
     */
    private Double confidenceThreshold;

    /**
     * 超时时间（秒），超时后执行降级策略
     */
    private Integer timeoutSeconds;

    /**
     * 降级策略：MANUAL（转人工）、DEFAULT_PASS（默认通过）、DEFAULT_REJECT（默认拒绝）
     */
    private String fallbackStrategy;

    /**
     * 是否启用异步模式
     */
    private Boolean asyncMode;

    /**
     * 最大重试次数
     */
    private Integer maxRetries;

    /**
     * 输出字段映射：将 AI 返回的 JSON 字段映射到流程变量
     * 示例：{"ai_amount": "approvalAmount", "ai_reason": "aiReason"}
     */
    private Map<String, String> outputMapping;

    /**
     * 模型参数（如 temperature, max_tokens 等）
     */
    private Map<String, Object> modelParams;

    /**
     * 获取置信度阈值，默认 0.8
     */
    public double getConfidenceThresholdOrDefault() {
        return null != confidenceThreshold ? confidenceThreshold : 0.8;
    }

    /**
     * 获取超时时间，默认 30 秒
     */
    public int getTimeoutSecondsOrDefault() {
        return null != timeoutSeconds ? timeoutSeconds : 30;
    }

    /**
     * 是否启用异步模式，默认 false
     */
    public boolean isAsyncModeEnabled() {
        return null != asyncMode && asyncMode;
    }

    /**
     * 获取最大重试次数，默认 3 次
     */
    public int getMaxRetriesOrDefault() {
        return null != maxRetries ? maxRetries : 3;
    }

    /**
     * 获取降级策略，默认转人工
     */
    public String getFallbackStrategyOrDefault() {
        return null != fallbackStrategy ? fallbackStrategy : "MANUAL";
    }
}
