/*
 * Copyright 2023-2025 Licensed under the AGPL License
 */
package com.aizuda.bpm.engine.handler.impl;

import com.aizuda.bpm.engine.core.Execution;
import com.aizuda.bpm.engine.core.FlowLongContext;
import com.aizuda.bpm.engine.handler.ConditionArgsHandler;
import com.aizuda.bpm.engine.model.NodeModel;

import java.util.Map;

/**
 * 默认流程执行条件参数处理器
 *
 * <p>
 * 尊重知识产权，不允许非法使用，后果自负
 * </p>
 *
 * @author hubin
 * @since 1.0
 */
public class DefaultConditionArgsHandler implements ConditionArgsHandler {
    private static DefaultConditionArgsHandler defaultConditionArgsHandler;

    public static DefaultConditionArgsHandler getInstance() {
        if (null == defaultConditionArgsHandler) {
            synchronized (DefaultCreateTaskHandler.class) {
                defaultConditionArgsHandler = new DefaultConditionArgsHandler();
            }
        }
        return defaultConditionArgsHandler;
    }

    @Override
    public Map<String, Object> handle(FlowLongContext flowLongContext, Execution execution, NodeModel nodeModel) {
        return execution.getArgs();
    }
}
