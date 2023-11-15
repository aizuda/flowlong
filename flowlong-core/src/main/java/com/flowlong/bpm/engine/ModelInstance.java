/* 
 * Copyright 2023-2025 Licensed under the AGPL License
 */
package com.flowlong.bpm.engine;

import com.flowlong.bpm.engine.core.Execution;
import com.flowlong.bpm.engine.core.FlowLongContext;

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
     */
    void execute(FlowLongContext flowLongContext, Execution execution);
}
