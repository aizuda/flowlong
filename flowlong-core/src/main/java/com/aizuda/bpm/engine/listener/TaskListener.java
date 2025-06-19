/*
 * Copyright 2023-2025 Licensed under the Dual Licensing
 * website: https://aizuda.com
 */
package com.aizuda.bpm.engine.listener;

import com.aizuda.bpm.engine.core.FlowCreator;
import com.aizuda.bpm.engine.core.enums.TaskEventType;
import com.aizuda.bpm.engine.entity.FlwTask;
import com.aizuda.bpm.engine.entity.FlwTaskActor;
import com.aizuda.bpm.engine.model.NodeModel;

import java.util.List;
import java.util.function.Supplier;

/**
 * 流程任务监听
 *
 * <p>
 * <a href="https://aizuda.com">官网</a>尊重知识产权，不允许非法使用，后果自负
 * </p>
 *
 * @author hubin
 * @since 1.0
 */
public interface TaskListener {

    /**
     * 流程引擎监听通知
     *
     * @param eventType   事件类型
     * @param supplier    监听任务提供者
     * @param taskActors  监听任务参与者
     * @param nodeModel   当前执行节点 {@link NodeModel} 为 null 需要根据 runtimeService.getNodeModel(instanceId, nodeKey) 方法获取
     * @param flowCreator 处理人员
     * @return 通知结果 true 成功 false 失败
     */
    boolean notify(TaskEventType eventType, Supplier<FlwTask> supplier, List<FlwTaskActor> taskActors,
                   NodeModel nodeModel, FlowCreator flowCreator);

}
