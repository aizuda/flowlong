/* 
 * Copyright 2023-2025 Licensed under the AGPL License
 */
package com.flowlong.bpm.engine.listener;

import com.flowlong.bpm.engine.core.enums.EventType;

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
     * @param eventType 事件类型
     * @param t         监听实体
     */
    void notify(EventType eventType, T t);

}
