package com.flowlong.bpm.spring.example.config;

import com.flowlong.bpm.spring.event.TaskEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;

@Configuration
public class TestConfig {

    /**
     * 异步任务事件监听处理
     * <p>
     * application.yml 开启  flowlong.eventing.task = true
     * </p>
     */
    @EventListener
    public void onTaskEvent(TaskEvent taskEvent) {
        System.err.println("当前执行任务 = " + taskEvent.getFlwTask().getTaskName() +
                " ，执行事件 = " + taskEvent.getEventType().name());
    }
}
