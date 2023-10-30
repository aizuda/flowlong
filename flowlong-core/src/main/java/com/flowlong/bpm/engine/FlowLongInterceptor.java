/* 
 * Copyright 2023-2025 Licensed under the AGPL License
 */
package com.flowlong.bpm.engine;

import com.flowlong.bpm.engine.core.Execution;
import com.flowlong.bpm.engine.core.FlowLongContext;

/**
 * FlowLong流程引擎拦截器
 *
 * <p>
 * 尊重知识产权，不允许非法使用，后果自负
 * </p>
 *
 * @author hubin
 * @since 1.0
 */
public interface FlowLongInterceptor {

    /**
     * 拦截处理方法
     *
     * @param flowLongContext 流程引擎上下文
     * @param execution       执行对象
     */
    void handle(FlowLongContext flowLongContext, Execution execution);
}
