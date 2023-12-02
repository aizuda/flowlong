/*
 * Copyright 2023-2025 Licensed under the AGPL License
 */
package com.flowlong.bpm.engine.handler;

import com.flowlong.bpm.engine.core.Execution;
import com.flowlong.bpm.engine.core.FlowLongContext;
import com.flowlong.bpm.engine.model.NodeModel;

/**
 * 流程任务创建处理器
 *
 * <p>
 * 尊重知识产权，不允许非法使用，后果自负
 * </p>
 *
 * @author hubin
 * @since 1.0
 */
public interface CreateTaskHandler {


    /**
     * 子类需要实现的方法，来处理具体的操作
     *
     * @param flowLongContext 流程引擎上下文
     * @param execution       执行对象
     * @param nodeModel       节点模型
     */
    boolean handle(FlowLongContext flowLongContext, Execution execution, NodeModel nodeModel);
}
