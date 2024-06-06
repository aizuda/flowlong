/*
 * Copyright 2023-2025 Licensed under the AGPL License
 */
package com.aizuda.bpm.engine;

import com.aizuda.bpm.engine.core.Execution;
import com.aizuda.bpm.engine.core.FlowLongContext;

/**
 * 模型实例接口
 *
 * <p>
 * 尊重知识产权，不允许非法使用，后果自负
 * </p>
 *
 * @author hubin
 * @since 1.0
 */
public interface ModelInstance {

    /**
     * 执行流程元素
     *
     * @param flowLongContext 流程引擎上下文
     * @param execution       执行对象
     * @return 执行结果 true 成功 false 失败
     */
    boolean execute(FlowLongContext flowLongContext, Execution execution);
}
