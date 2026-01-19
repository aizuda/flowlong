/*
 * Copyright 2023-2025 Licensed under the Dual Licensing
 * website: https://aizuda.com
 */
package com.aizuda.bpm.engine.handler.impl;

import com.aizuda.bpm.engine.core.Execution;
import com.aizuda.bpm.engine.core.FlowLongContext;
import com.aizuda.bpm.engine.entity.FlwTask;
import com.aizuda.bpm.engine.handler.CreateTaskHandler;
import com.aizuda.bpm.engine.handler.FlowAiHandler;
import com.aizuda.bpm.engine.model.NodeModel;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * 默认流程任务创建处理器
 *
 * <p>
 * <a href="https://aizuda.com">官网</a>尊重知识产权，不允许非法使用，后果自负
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
                // 设置当前创建任务列表
                execution.addTasks(flwTasks);

                // 执行 AI智能体 审批逻辑
                if (null != nodeModel.getCallAi()) {
                    FlowAiHandler flowAiHandler = flowLongContext.getFlowAiHandler();
                    if (null == flowAiHandler) {
                        log.warn("AI node [{}] configured but FlowAiHandler not found, skip AI processing", nodeModel.getNodeKey());
                        return true;
                    }
                    return flowAiHandler.handle(flowLongContext, execution, nodeModel);
                }
            }
            return true;
        } catch (Exception e) {
            log.error("SimpleCreateTaskHandler createTask failed. {}", e.getMessage());
            throw e;
        }
    }
}
