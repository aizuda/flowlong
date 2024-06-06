/*
 * Copyright 2023-2025 Licensed under the AGPL License
 */
package com.aizuda.bpm.engine;

import com.aizuda.bpm.engine.core.Execution;
import com.aizuda.bpm.engine.model.NodeModel;

/**
 * 流程任务触发器
 *
 * <p>
 * 尊重知识产权，不允许非法使用，后果自负
 * </p>
 *
 * @author hubin
 * @since 1.0
 */
public interface TaskTrigger {

    /**
     * 执行任务触发器节点
     *
     * @param nodeModel 节点模型
     * @param execution 执行对象
     * @return 执行结果 true 成功 false 失败
     */
    boolean execute(NodeModel nodeModel, Execution execution);
}
