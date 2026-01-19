/*
 * Copyright 2023-2025 Licensed under the Dual Licensing
 * website: https://aizuda.com
 */
package com.aizuda.bpm.engine.handler;

import com.aizuda.bpm.engine.core.Execution;
import com.aizuda.bpm.engine.core.FlowLongContext;
import com.aizuda.bpm.engine.model.AiConfig;
import com.aizuda.bpm.engine.model.AiResponse;
import com.aizuda.bpm.engine.model.NodeModel;

import java.util.List;
import java.util.Map;

/**
 * 流程 AI 智能体处理器接口
 *
 * <p>
 * <a href="https://aizuda.com">官网</a>尊重知识产权，不允许非法使用，后果自负
 * </p>
 *
 * @author hubin
 * @since 1.0
 */
public interface FlowAiHandler {

    default boolean handle(FlowLongContext flowLongContext, Execution execution, NodeModel nodeModel) {

        // 执行 AI 处理
        AiResponse aiResponse = this.execute(flowLongContext, execution, nodeModel);

        // 处理 AI 响应
        return this.processAiResponse(flowLongContext, execution, nodeModel, aiResponse);
    }

    /**
     * 处理 AI 相应对象逻辑
     *
     * @param flowLongContext {@link FlowLongContext}
     * @param execution       {@link Execution}
     * @param nodeModel       {@link NodeModel}
     * @param aiResponse      {@link AiResponse}
     * @return true 处理成功 false 处理失败
     */
    boolean processAiResponse(FlowLongContext flowLongContext, Execution execution, NodeModel nodeModel, AiResponse aiResponse);

    /**
     * 合并 AI 提取的变量到执行参数
     */
    default void mergeAiVariables(Execution execution, AiResponse aiResponse, AiConfig aiConfig) {
        Map<String, Object> aiVariables = aiResponse.getVariables();
        if (null == aiVariables || aiVariables.isEmpty()) {
            return;
        }

        Map<String, Object> args = execution.getArgs();
        if (null == args) {
            return;
        }

        // 应用输出映射
        Map<String, String> outputMapping = null != aiConfig ? aiConfig.getOutputMapping() : null;
        for (Map.Entry<String, Object> entry : aiVariables.entrySet()) {
            String key = entry.getKey();
            // 如果有映射配置，使用映射后的 key
            if (null != outputMapping && outputMapping.containsKey(key)) {
                key = outputMapping.get(key);
            }
            args.put(key, entry.getValue());
        }
    }

    /**
     * 执行 AI 处理并返回结构化响应
     *
     * @param flowLongContext {@link FlowLongContext}
     * @param execution       {@link Execution}
     * @param nodeModel       {@link NodeModel}
     * @return {@link AiResponse} AI 结构化响应
     */
    AiResponse execute(FlowLongContext flowLongContext, Execution execution, NodeModel nodeModel);

    /**
     * AI 智能路由决策：直接返回应该走的分支 NodeKey
     *
     * @param flowLongContext {@link FlowLongContext}
     * @param execution       {@link Execution}
     * @param nodeModel       {@link NodeModel}
     * @param args 路由参数内容
     * @return 目标分支的 NodeKey，返回 null 表示 AI 无法决策，需走普通条件判断
     */
    default String decideRoute(FlowLongContext flowLongContext, Execution execution, NodeModel nodeModel, Map<String, Object> args) {
        return null;
    }

    /**
     * AI 智能包容分支决策：返回应该同时执行的多个分支 NodeKey 列表
     * <p>
     * 包容分支与条件分支不同，可以同时执行多个分支
     * </p>
     *
     * @param flowLongContext {@link FlowLongContext}
     * @param execution       {@link Execution}
     * @param nodeModel       {@link NodeModel}
     * @param args 路由参数内容
     * @return 目标分支的 NodeKey 列表，返回 null 或空列表表示 AI 无法决策，需走普通条件判断
     */
    default List<String> decideInclusiveRoutes(FlowLongContext flowLongContext, Execution execution, NodeModel nodeModel, Map<String, Object> args) {
        return null;
    }

    /**
     * 异步回调处理：当 AI 异步处理完成后调用
     *
     * @param flowLongContext {@link FlowLongContext}
     * @param asyncToken      异步处理凭证
     * @param aiResponse      AI 响应结果
     * @return true 恢复流程成功 false 失败
     */
    default boolean onAsyncComplete(FlowLongContext flowLongContext, String asyncToken, AiResponse aiResponse) {
        return true;
    }
}
