/*
 * Copyright 2023-2025 Licensed under the AGPL License
 */
package com.aizuda.bpm.engine.listener;

import com.aizuda.bpm.engine.core.FlowCreator;
import com.aizuda.bpm.engine.core.enums.EventType;

import java.util.function.Supplier;

/**
 * 流程引擎监听接口
 *
 * <p>
 * 尊重知识产权，不允许非法使用，后果自负
 * </p>
 *
 * @author hubin
 * @since 1.0
 */
public interface FlowLongListener<T> {

    /**
     * 流程引擎监听通知
     *
     * @param eventType   事件类型
     * @param supplier    监听实体提供者
     * @param flowCreator 处理人员
     * @return 通知结果 true 成功 false 失败
     */
    boolean notify(EventType eventType, Supplier<T> supplier, FlowCreator flowCreator);

}
