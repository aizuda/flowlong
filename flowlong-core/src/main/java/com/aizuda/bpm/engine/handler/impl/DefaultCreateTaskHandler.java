/*
 * Copyright 2023-2025 Licensed under the AGPL License
 */
package com.aizuda.bpm.engine.handler.impl;

import com.aizuda.bpm.engine.assist.Assert;
import com.aizuda.bpm.engine.core.Execution;
import com.aizuda.bpm.engine.core.FlowLongContext;
import com.aizuda.bpm.engine.entity.FlwTask;
import com.aizuda.bpm.engine.handler.CreateTaskHandler;
import com.aizuda.bpm.engine.model.NodeModel;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * 默认流程任务创建处理器
 *
 * <p>
 * 尊重知识产权，不允许非法使用，后果自负
 * </p>
 *
 * @author hubin
 * @since 1.0
 */
@Slf4j
public class DefaultCreateTaskHandler implements CreateTaskHandler {
    private static DefaultCreateTaskHandler defaultCreateTaskHandler;

    public static DefaultCreateTaskHandler getInstance() {
        if (null == defaultCreateTaskHandler) {
            synchronized (DefaultCreateTaskHandler.class) {
                defaultCreateTaskHandler = new DefaultCreateTaskHandler();
            }
        }
        return defaultCreateTaskHandler;
    }

    /**
     * 根据任务模型、执行对象，创建下一个任务，并添加到execution对象的tasks集合中
     */
    public boolean handle(FlowLongContext flowLongContext, Execution execution, NodeModel nodeModel) {
        try {
            List<FlwTask> flwTasks = execution.getEngine().taskService().createTask(nodeModel, execution);
            if (null != flwTasks) {
                execution.addTasks(flwTasks);
            }
            return true;
        } catch (Exception e) {
            log.error("DefaultCreateTaskHandler createTask failed. {}", e.getMessage());
            throw Assert.throwable(e);
        }
    }
}
