/*
 * Copyright 2023-2025 Licensed under the Dual Licensing
 * website: https://aizuda.com
 */
package com.aizuda.bpm.engine.listener;

import com.aizuda.bpm.engine.core.FlowCreator;
import com.aizuda.bpm.engine.core.enums.InstanceEventType;
import com.aizuda.bpm.engine.entity.FlwHisInstance;
import com.aizuda.bpm.engine.model.NodeModel;

import java.util.function.Supplier;

/**
 * 流程实例监听
 *
 * <p>
 * <a href="https://aizuda.com">官网</a>尊重知识产权，不允许非法使用，后果自负
 * </p>
 *
 * @author hubin
 * @since 1.0
 */
public interface InstanceListener {

    /**
     * 流程引擎监听通知
     *
     * @param eventType   事件类型
     * @param supplier    监听实例提供者
     * @param nodeModel   当前执行节点 {@link NodeModel} 可能为 null
     * @param flowCreator 处理人员
     * @return 通知结果 true 成功 false 失败
     */
    boolean notify(InstanceEventType eventType, Supplier<FlwHisInstance> supplier, NodeModel nodeModel, FlowCreator flowCreator);

}
