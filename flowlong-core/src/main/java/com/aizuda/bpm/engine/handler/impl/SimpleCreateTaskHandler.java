/*
 * Copyright 2023-2025 Licensed under the AGPL License
 */
package com.aizuda.bpm.engine.handler.impl;

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
public class SimpleCreateTaskHandler implements CreateTaskHandler {
    private static SimpleCreateTaskHandler createTaskHandler;

    public static SimpleCreateTaskHandler getInstance() {
        if (null == createTaskHandler) {
            synchronized (SimpleCreateTaskHandler.class) {
                createTaskHandler = new SimpleCreateTaskHandler();
            }
        }
        return createTaskHandler;
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
            log.error("SimpleCreateTaskHandler createTask failed. {}", e.getMessage());
            throw e;
        }
    }
}
