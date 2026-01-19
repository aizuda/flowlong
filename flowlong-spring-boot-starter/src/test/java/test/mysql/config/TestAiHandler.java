package test.mysql.config;

import com.aizuda.bpm.engine.core.Execution;
import com.aizuda.bpm.engine.core.FlowCreator;
import com.aizuda.bpm.engine.core.FlowLongContext;
import com.aizuda.bpm.engine.core.enums.AiFallbackStrategy;
import com.aizuda.bpm.engine.core.enums.AiStatus;
import com.aizuda.bpm.engine.entity.FlwTask;
import com.aizuda.bpm.engine.handler.FlowAiHandler;
import com.aizuda.bpm.engine.model.AiConfig;
import com.aizuda.bpm.engine.model.AiResponse;
import com.aizuda.bpm.engine.model.NodeModel;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

@Slf4j
public class TestAiHandler implements FlowAiHandler {
    // 创建一个 AI 智能体处理用户
    public static final FlowCreator aiUser = new FlowCreator("1", "AI 智能体");

    @Override
    public AiResponse execute(FlowLongContext flowLongContext, Execution execution, NodeModel nodeModel) {
        AiConfig aiConfig = nodeModel.getAiConfig();

        // 1. 构建 Prompt 提示词
        if (null != aiConfig) {
            System.out.println("AI Prompt Template: " + aiConfig.getPromptTemplate());
        }

        System.out.println("handle 根据 callAi 识别处理具体逻辑：" + nodeModel.getCallAi());

        // 2. 调用 AI 服务（此处为示例实现，实际需对接真实 AI API）
        try {
            // TODO: 对接实际的 AI 服务，如 OpenAI、Claude、文心一言等
            // AiServiceResponse response = aiService.chat(prompt, aiConfig.getModelParams());

            // 返回一个模拟的成功响应，包含决策、建议、置信度和指标
            return AiResponse.builder()
                    .status(AiStatus.SUCCESS)
                    .decision("flk17631709068381")
                    .advice("AI 审批建议：经分析，该申请符合相关规定，建议通过。")
                    .confidence(0.95)
                    .metrics(AiResponse.AiMetrics.builder()
                            .modelName("default-model")
                            .promptTokens(100L)
                            .completionTokens(50L)
                            .totalTimeMs(500L)
                            .build())
                    .build();
        } catch (Exception e) {
            log.error("AI processing failed: {}", e.getMessage(), e);
            return AiResponse.failure("AI 服务调用失败: " + e.getMessage());
        }
    }

    /**
     * 处理 AI 响应结果
     */
    @Override
    public boolean processAiResponse(FlowLongContext flowLongContext, Execution execution, NodeModel nodeModel, AiResponse aiResponse) {
        if (null == aiResponse) {
            return this.handleAiFallback(execution, nodeModel, "AI 处理器返回空响应");
        }

        AiStatus status = aiResponse.getStatus();

        // 1. 处理异步情况
        if (aiResponse.isAsync()) {
            // 异步模式：流程挂起，等待回调
            return true;
        }

        // 获取 AI 配置
        AiConfig aiConfig = nodeModel.getAiConfig();

        // 2. 合并 AI 提取的变量到执行参数
        this.mergeAiVariables(execution, aiResponse, aiConfig);

        // 3. 检查置信度
        double confidenceThreshold = 0.8;
        if (null != aiConfig) {
            confidenceThreshold = aiConfig.getConfidenceThresholdOrDefault();
        }
        if (aiResponse.getConfidenceOrDefault() < confidenceThreshold) {
            return this.handleAiFallback(execution, nodeModel, "AI 置信度低: " + aiResponse.getConfidenceOrDefault() + ", 建议: " + aiResponse.getAdvice());
        }

        // 4. 根据状态处理
        switch (status) {
            case SUCCESS:
                return this.handleAiSuccess(execution, nodeModel, aiResponse);
            case LOW_CONFIDENCE:
            case FALLBACK:
            case FAILURE:
            case TIMEOUT:
                return this.handleAiFallback(execution, nodeModel, aiResponse.getErrorMessage());
            default:
                return true;
        }
    }

    /**
     * AI 处理成功
     */
    protected boolean handleAiSuccess(Execution execution, NodeModel nodeModel, AiResponse aiResponse) {

        // 根据决策结果进行自动审批
        List<FlwTask> flwTasks = execution.getFlwTasks();
        for (FlwTask ft : flwTasks) {

            // 记录 AI 审批意见相关数据到任务变量（用于历史记录）
            Map<String, Object> args = execution.getArgs();
            if (null != args) {
                // 审批意见
                if (null != aiResponse.getAdvice()) {
                    args.put("_ai_advice", aiResponse.getAdvice());
                    args.put("_ai_decision", aiResponse.getDecision());
                    args.put("_ai_confidence", aiResponse.getConfidenceOrDefault());
                }

                // 记录指标数据
                if (null != aiResponse.getMetrics()) {
                    AiResponse.AiMetrics metrics = aiResponse.getMetrics();
                    args.put("_ai_model", metrics.getModelName());
                    args.put("_ai_tokens", metrics.getTotalTokens());
                    args.put("_ai_time_ms", metrics.getTotalTimeMs());
                }
            }

            // AI 建议通过，自动完成任务
            if (aiResponse.isPass()) {
                execution.getEngine().autoCompleteTask(ft.getId(), args, aiUser);
            }

            // AI 建议拒绝，自动拒绝任务
            if (aiResponse.isReject()) {
                execution.getEngine().autoRejectTask(ft, args, aiUser);
            }
        }

        // 其他决策结果，不自动处理，等待人工介入
        return true;
    }

    /**
     * AI 降级处理
     */
    protected boolean handleAiFallback(Execution execution, NodeModel nodeModel, String reason) {
        AiConfig aiConfig = nodeModel.getAiConfig();
        if (null == aiConfig) {
            return true;
        }
        String fallbackStrategy = aiConfig.getFallbackStrategyOrDefault();
        if (AiFallbackStrategy.MANUAL.eq(fallbackStrategy)) {
            return true;
        }

        // 记录降级原因
        Map<String, Object> args = execution.getArgs();
        if (null != args) {
            args.put("_ai_fallback", true);
            args.put("_ai_fallback_reason", reason);
        }

        List<FlwTask> flwTasks = execution.getFlwTasks();
        for (FlwTask ft : flwTasks) {
            if (AiFallbackStrategy.DEFAULT_PASS.eq(fallbackStrategy)) {
                // 默认通过
                execution.getEngine().autoCompleteTask(ft.getId(), args, aiUser);
            } else if (AiFallbackStrategy.DEFAULT_REJECT.eq(fallbackStrategy)) {
                // 默认拒绝
                execution.getEngine().autoRejectTask(ft, args, aiUser);
            }
        }
        return true;
    }


    @Override
    public String decideRoute(FlowLongContext flowLongContext, Execution execution, NodeModel nodeModel, Map<String, Object> args) {
        // 默认实现：返回 null，表示不由 AI 决定路由
        System.out.println("AI Decision: " + args.get("content"));
        // 这里模拟决策返回 审批 A 所在分支
        return "flk17631709068381";
    }

    @Override
    public List<String> decideInclusiveRoutes(FlowLongContext flowLongContext, Execution execution, NodeModel nodeModel, Map<String, Object> args) {
        // 默认实现：返回 null，表示不由 AI 决定包容分支
        return null;
    }

    @Override
    public boolean onAsyncComplete(FlowLongContext flowLongContext, String asyncToken, AiResponse aiResponse) {
        // 异步回调处理
        log.info("AI async complete, token={}, status={}", asyncToken, aiResponse.getStatus());

        // TODO: 根据 asyncToken 找到对应的任务，并恢复流程执行
        // 1. 根据 asyncToken 查询挂起的任务
        // 2. 根据 aiResponse 结果决定是自动完成还是转人工
        // 3. 恢复流程执行
        return true;
    }
}
