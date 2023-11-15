/* 
 * Copyright 2023-2025 Licensed under the AGPL License
 */
package com.flowlong.bpm.engine;

import com.flowlong.bpm.engine.core.Execution;
import com.flowlong.bpm.engine.model.NodeModel;

/**
 * 分配参与者的处理接口
 *
 * <p>
 * 尊重知识产权，不允许非法使用，后果自负
 * </p>
 *
 * @author hubin
 * @since 1.0
 */
public interface Assignment {

    /**
     * 分配参与者方法，可获取到当前的执行对象
     *
     * @param execution 执行对象
     * @return Object 任务参与者
     */
    default Object assign(Execution execution) {
        return assign(null, execution);
    }

    /**
     * 分配参与者方法，可获取到当前的任务模型、执行对象
     *
     * @param nodeModel 节点模型
     * @param execution 执行对象
     * @return Object 任务参与者
     */
    Object assign(NodeModel nodeModel, Execution execution);

}
