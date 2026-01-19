/*
 * Copyright 2023-2025 Licensed under the Dual Licensing
 * website: https://aizuda.com
 */
package com.aizuda.bpm.engine.model;

import com.aizuda.bpm.engine.core.enums.AiStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Map;

/**
 * AI 处理响应对象
 *
 * <p>
 * <a href="https://aizuda.com">官网</a>尊重知识产权，不允许非法使用，后果自负
 * </p>
 *
 * @author hubin
 * @since 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiResponse implements Serializable {

    /**
     * AI 执行状态
     */
    private AiStatus status;

    /**
     * AI 决策结果
     * <p>
     * 对于审批节点：PASS（通过）、REJECT（拒绝）
     * 对于路由节点：返回目标 NodeKey
     * </p>
     */
    private String decision;

    /**
     * AI 审批意见 / 推理过程（Chain of Thought）
     * <p>
     * 用于展示给人工复核或记录到审批历史
     * </p>
     */
    private String advice;

    /**
     * AI 从非结构化文本中提取的业务参数
     * <p>
     * 如：{"amount": 5000, "days": 3, "category": "差旅"}
     * 将自动合并至流程执行参数 args
     * </p>
     */
    private Map<String, Object> variables;

    /**
     * 置信度评分 (0.0 - 1.0)
     * <p>
     * 引擎根据此分值决定是直接流转，还是转为"人工复核"
     * </p>
     */
    private Double confidence;

    /**
     * 审计与性能指标
     */
    private AiMetrics metrics;

    /**
     * 原始响应内容
     * <p>
     * 保留大模型的原始 JSON 或 String，便于排查幻觉问题
     * </p>
     */
    private String rawContent;

    /**
     * 错误信息（当 status 为 FAILURE 时）
     */
    private String errorMessage;

    /**
     * 异步处理凭证（当 status 为 ASYNC 时）
     * <p>
     * 用于后续回调时匹配任务
     * </p>
     */
    private String asyncToken;

    /**
     * AI 审计与性能指标
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AiMetrics implements Serializable {
        /**
         * 使用的模型名称（如 gpt-4, claude-3）
         */
        private String modelName;

        /**
         * 输入 Token 消耗
         */
        private Long promptTokens;

        /**
         * 输出 Token 消耗
         */
        private Long completionTokens;

        /**
         * 总耗时（毫秒）
         */
        private Long totalTimeMs;

        /**
         * 请求唯一标识（用于追踪）
         */
        private String requestId;

        /**
         * 获取总 Token 消耗
         */
        public long getTotalTokens() {
            long prompt = null != promptTokens ? promptTokens : 0L;
            long completion = null != completionTokens ? completionTokens : 0L;
            return prompt + completion;
        }
    }

    // ==================== 静态工厂方法 ====================

    /**
     * 创建成功响应
     */
    public static AiResponse success(String decision, String advice, Double confidence) {
        return AiResponse.builder()
                .status(AiStatus.SUCCESS)
                .decision(decision)
                .advice(advice)
                .confidence(confidence)
                .build();
    }

    /**
     * 创建成功响应（带提取变量）
     */
    public static AiResponse success(String decision, String advice, Double confidence, Map<String, Object> variables) {
        return AiResponse.builder()
                .status(AiStatus.SUCCESS)
                .decision(decision)
                .advice(advice)
                .confidence(confidence)
                .variables(variables)
                .build();
    }

    /**
     * 创建失败响应
     */
    public static AiResponse failure(String errorMessage) {
        return AiResponse.builder()
                .status(AiStatus.FAILURE)
                .errorMessage(errorMessage)
                .build();
    }

    /**
     * 创建低置信度响应（需人工复核）
     */
    public static AiResponse lowConfidence(String decision, String advice, Double confidence) {
        return AiResponse.builder()
                .status(AiStatus.LOW_CONFIDENCE)
                .decision(decision)
                .advice(advice)
                .confidence(confidence)
                .build();
    }

    /**
     * 创建异步处理响应
     */
    public static AiResponse async(String asyncToken) {
        return AiResponse.builder()
                .status(AiStatus.ASYNC)
                .asyncToken(asyncToken)
                .build();
    }

    /**
     * 创建降级响应（转人工处理）
     */
    public static AiResponse fallback(String reason) {
        return AiResponse.builder()
                .status(AiStatus.FALLBACK)
                .errorMessage(reason)
                .build();
    }

    // ==================== 业务判断方法 ====================

    /**
     * 是否需要人工复核
     */
    public boolean needManualReview() {
        return null != status && status.needManualReview();
    }

    /**
     * 是否处理成功
     */
    public boolean isSuccess() {
        return null != status && status.isSuccess();
    }

    /**
     * 是否为异步处理
     */
    public boolean isAsync() {
        return null != status && status.isAsync();
    }

    /**
     * 获取置信度，默认 0.0
     */
    public double getConfidenceOrDefault() {
        return null != confidence ? confidence : 0.0;
    }

    /**
     * 决策是否为通过
     */
    public boolean isPass() {
        return "PASS".equalsIgnoreCase(decision);
    }

    /**
     * 决策是否为拒绝
     */
    public boolean isReject() {
        return "REJECT".equalsIgnoreCase(decision);
    }
}
