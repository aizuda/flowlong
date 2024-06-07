/*
 * Copyright 2023-2025 Licensed under the AGPL License
 */
package com.aizuda.bpm.spring.event;

import com.aizuda.bpm.engine.core.FlowCreator;
import com.aizuda.bpm.engine.core.enums.EventType;
import com.aizuda.bpm.engine.entity.FlwTask;
import com.aizuda.bpm.engine.listener.TaskListener;
import com.aizuda.bpm.engine.model.NodeModel;
import org.springframework.context.ApplicationEventPublisher;

import java.util.function.Supplier;

/**
 * Spring boot Event 异步任务监听处理器
 *
 * <p>
 * 尊重知识产权，不允许非法使用，后果自负
 * </p>
 *
 * @author hubin
 * @since 1.0
 */
public class EventTaskListener implements TaskListener {
    private final ApplicationEventPublisher eventPublisher;

    public EventTaskListener(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    @Override
    public boolean notify(EventType eventType, Supplier<FlwTask> supplier, NodeModel nodeModel, FlowCreator flowCreator) {
        TaskEvent taskEvent = new TaskEvent();
        taskEvent.setEventType(eventType);
        taskEvent.setFlwTask(supplier.get());
        taskEvent.setFlowCreator(flowCreator);
        eventPublisher.publishEvent(taskEvent);
        return true;
    }
}
