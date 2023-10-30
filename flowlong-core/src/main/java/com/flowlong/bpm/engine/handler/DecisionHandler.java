/* 
 * Copyright 2023-2025 Licensed under the AGPL License
 */
package com.flowlong.bpm.engine.handler;

import com.flowlong.bpm.engine.core.Execution;
import com.flowlong.bpm.engine.core.FlowLongContext;

/**
 * 决策处理器接口
 *
 * <p>
 * 尊重知识产权，不允许非法使用，后果自负
 * </p>
 *
 * @author hubin
 * @since 1.0
 */
public interface DecisionHandler {

    /**
     * 定义决策方法，实现类需要根据执行对象做处理，并返回后置流转的name
     *
     * @param flowLongContext 流程引擎上下文
     * @param execution       {@see Execution}
     * @return String 后置流转的节点名称
     */
    String decide(FlowLongContext flowLongContext, Execution execution);
}
