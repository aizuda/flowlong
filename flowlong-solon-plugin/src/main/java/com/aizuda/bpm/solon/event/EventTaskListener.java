/*
 * Copyright 2023-2025 Licensed under the Dual Licensing
 * website: https://aizuda.com
 */
package com.aizuda.bpm.solon.event;

import com.aizuda.bpm.engine.core.FlowCreator;
import com.aizuda.bpm.engine.core.enums.TaskEventType;
import com.aizuda.bpm.engine.entity.FlwTask;
import com.aizuda.bpm.engine.entity.FlwTaskActor;
import com.aizuda.bpm.engine.listener.TaskListener;
import com.aizuda.bpm.engine.model.NodeModel;
import org.noear.solon.core.event.EventBus;

import java.util.List;
import java.util.function.Supplier;

/**
 * Solon Event 异步任务监听处理器
 *
 * <p>
 * <a href="https://aizuda.com">官网</a>尊重知识产权，不允许非法使用，后果自负
 * </p>
 *
 * @author hubin
 * @author noear
 * @since 1.0
 */
public class EventTaskListener implements TaskListener {
    @Override
    public boolean notify(TaskEventType eventType, Supplier<FlwTask> supplier, List<FlwTaskActor> taskActors,
                          NodeModel nodeModel, FlowCreator flowCreator) {
        TaskEvent taskEvent = new TaskEvent();
        taskEvent.setEventType(eventType);
        taskEvent.setFlwTask(supplier.get());
        taskEvent.setTaskActors(taskActors);
        taskEvent.setNodeModel(nodeModel);
        taskEvent.setFlowCreator(flowCreator);
        EventBus.publish(taskEvent);
        return true;
    }
}
