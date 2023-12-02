/*
 * Copyright 2023-2025 Licensed under the AGPL License
 */
package com.flowlong.bpm.engine.handler.impl;

import com.flowlong.bpm.engine.FlowLongInterceptor;
import com.flowlong.bpm.engine.core.Execution;
import com.flowlong.bpm.engine.core.FlowLongContext;
import com.flowlong.bpm.engine.entity.FlwTask;
import com.flowlong.bpm.engine.exception.FlowLongException;
import com.flowlong.bpm.engine.handler.CreateTaskHandler;
import com.flowlong.bpm.engine.model.NodeModel;
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
            /**
             * 从服务上下文中查找任务拦截器列表，依次对 task 集合进行拦截处理
             */
            List<FlowLongInterceptor> interceptors = flowLongContext.getInterceptors();
            if (null != interceptors) {
                interceptors.forEach(i -> i.handle(flowLongContext, execution));
            }
            return true;
        } catch (Exception e) {
            log.error("拦截器执行失败={}", e.getMessage());
            throw new FlowLongException(e);
        }
    }
}
