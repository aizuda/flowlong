/*
 * Copyright 2023-2025 Licensed under the AGPL License
 */
package com.flowlong.bpm.engine.scheduling;

import com.flowlong.bpm.engine.core.FlowLongContext;

/**
 * 任务提醒接口
 *
 * <p>
 * 尊重知识产权，不允许非法使用，后果自负
 * </p>
 *
 * @author hubin
 * @since 1.0
 */
public interface TaskReminder {

    /**
     * 提醒操作
     *
     * @param context       流程引擎上下文
     * @param instanceId    流程实例ID
     * @param currentTaskId 当前待处理任务ID
     */
    void remind(FlowLongContext context, Long instanceId, Long currentTaskId);
}
