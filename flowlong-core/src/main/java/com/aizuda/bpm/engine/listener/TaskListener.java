/*
 * Copyright 2023-2025 Licensed under the AGPL License
 */
package com.aizuda.bpm.engine.listener;

import com.aizuda.bpm.engine.core.enums.TaskEventType;
import com.aizuda.bpm.engine.entity.FlwTask;

/**
 * 流程任务监听
 *
 * <p>
 * 尊重知识产权，不允许非法使用，后果自负
 * </p>
 *
 * @author hubin
 * @since 1.0
 */
public interface TaskListener extends FlowLongListener<TaskEventType, FlwTask> {

}
