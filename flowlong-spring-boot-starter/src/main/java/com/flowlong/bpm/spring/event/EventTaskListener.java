/*
 * Copyright 2023-2025 Licensed under the AGPL License
 */
package com.flowlong.bpm.spring.event;

import com.flowlong.bpm.engine.core.enums.EventType;
import com.flowlong.bpm.engine.entity.FlwTask;
import com.flowlong.bpm.engine.listener.TaskListener;
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
    public boolean notify(EventType eventType, Supplier<FlwTask> supplier) {
        TaskEvent taskEvent = new TaskEvent();
        taskEvent.setEventType(eventType);
        taskEvent.setFlwTask(supplier.get());
        eventPublisher.publishEvent(taskEvent);
        return true;
    }
}
