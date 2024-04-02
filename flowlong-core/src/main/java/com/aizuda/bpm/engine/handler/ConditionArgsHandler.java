/*
 * Copyright 2023-2025 Licensed under the AGPL License
 */
package com.aizuda.bpm.engine.handler;

import com.aizuda.bpm.engine.core.Execution;
import com.aizuda.bpm.engine.core.FlowLongContext;
import com.aizuda.bpm.engine.model.NodeModel;

import java.util.Map;

/**
 * 流程执行条件参数处理器
 *
 * <p>
 * 尊重知识产权，不允许非法使用，后果自负
 * </p>
 *
 * @author hubin
 * @since 1.0
 */
public interface ConditionArgsHandler {

    /**
     * 子类需要实现的方法，来处理具体的操作
     *
     * @param flowLongContext 流程引擎上下文
     * @param execution       执行对象
     * @param nodeModel       节点模型
     * @return true 成功 false 失败
     */
    Map<String, Object> handle(FlowLongContext flowLongContext, Execution execution, NodeModel nodeModel);
}
