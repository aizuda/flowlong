package com.aizuda.bpm.spring.example.config;

import com.aizuda.bpm.engine.FlowLongEngine;
import com.aizuda.bpm.engine.FlowLongScheduler;
import com.aizuda.bpm.engine.TaskReminder;
import com.aizuda.bpm.engine.scheduling.JobLock;
import com.aizuda.bpm.spring.autoconfigure.FlowLongProperties;
import com.aizuda.bpm.spring.event.TaskEvent;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;

@Configuration
public class TestConfig {

    @Bean
    @ConditionalOnBean(TaskReminder.class)
    @ConditionalOnMissingBean
    public FlowLongScheduler springBootScheduler(FlowLongEngine flowLongEngine, FlowLongProperties properties, JobLock jobLock) {
        FlowLongScheduler scheduler = new TestFlowLongScheduler();
        scheduler.setFlowLongEngine(flowLongEngine);
        scheduler.setRemindParam(properties.getRemind());
        scheduler.setJobLock(jobLock);
        return scheduler;
    }

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
