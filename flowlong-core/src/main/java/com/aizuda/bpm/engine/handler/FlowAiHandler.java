package com.aizuda.bpm.engine.handler;

import com.aizuda.bpm.engine.core.Execution;
import com.aizuda.bpm.engine.core.FlowLongContext;
import com.aizuda.bpm.engine.model.NodeModel;

import java.util.Map;

/**
 * 流程AI智能体处理器接口
 *
 * @author hubin
 * @since 1.0
 */
public interface FlowAiHandler {

    /**
     * 获取AI分析后的参数内容
     *
     * @param flowLongContext {@link FlowLongContext}
     * @param execution {@link Execution}
     * @param nodeModel {@link NodeModel}
     * @param args 路由参数内容
     * @return AI 分析后的参数内容
     */
    Map<String, Object> getArgs(FlowLongContext flowLongContext, Execution execution, NodeModel nodeModel, Map<String, Object> args);

}
