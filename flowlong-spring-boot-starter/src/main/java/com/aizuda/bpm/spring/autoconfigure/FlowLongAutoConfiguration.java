/*
 * Copyright 2023-2025 Licensed under the AGPL License
 */
package com.aizuda.bpm.spring.autoconfigure;

import com.aizuda.bpm.engine.*;
import com.aizuda.bpm.engine.core.FlowLongContext;
import com.aizuda.bpm.engine.core.FlowLongEngineImpl;
import com.aizuda.bpm.engine.handler.ConditionArgsHandler;
import com.aizuda.bpm.engine.handler.CreateTaskHandler;
import com.aizuda.bpm.engine.handler.FlowJsonHandler;
import com.aizuda.bpm.engine.impl.GeneralAccessStrategy;
import com.aizuda.bpm.engine.impl.GeneralTaskActorProvider;
import com.aizuda.bpm.engine.listener.InstanceListener;
import com.aizuda.bpm.engine.listener.TaskListener;
import com.aizuda.bpm.engine.scheduling.JobLock;
import com.aizuda.bpm.engine.scheduling.LocalLock;
import com.aizuda.bpm.engine.scheduling.TaskReminder;
import com.aizuda.bpm.mybatisplus.mapper.*;
import com.aizuda.bpm.mybatisplus.service.ProcessServiceImpl;
import com.aizuda.bpm.mybatisplus.service.QueryServiceImpl;
import com.aizuda.bpm.mybatisplus.service.RuntimeServiceImpl;
import com.aizuda.bpm.mybatisplus.service.TaskServiceImpl;
import com.aizuda.bpm.spring.adaptive.FlowJacksonHandler;
import com.aizuda.bpm.spring.adaptive.SpelExpression;
import com.aizuda.bpm.spring.adaptive.SpringBootScheduler;
import com.aizuda.bpm.spring.event.EventInstanceListener;
import com.aizuda.bpm.spring.event.EventTaskListener;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * spring boot starter 启动自动配置处理类
 *
 * <p>
 * 尊重知识产权，不允许非法使用，后果自负
 * </p>
 *
 * @author hubin
 * @since 1.0
 */
@Configuration
@MapperScan("com.aizuda.bpm.mybatisplus.mapper")
@EnableConfigurationProperties(FlowLongProperties.class)
public class FlowLongAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public TaskService taskService(@Autowired(required = false) TaskAccessStrategy taskAccessStrategy, @Autowired(required = false) TaskListener taskListener,
                                   FlwProcessMapper processMapper, FlwInstanceMapper instanceMapper, FlwHisInstanceMapper hisInstanceMapper,
                                   FlwTaskMapper taskMapper, FlwTaskActorMapper taskActorMapper, FlwHisTaskMapper hisTaskMapper,
                                   FlwHisTaskActorMapper hisTaskActorMapper) {
        return new TaskServiceImpl(taskAccessStrategy, taskListener, processMapper, instanceMapper, hisInstanceMapper,
                taskMapper, taskActorMapper, hisTaskMapper, hisTaskActorMapper);
    }

    @Bean
    @ConditionalOnMissingBean
    public QueryService queryService(FlwInstanceMapper instanceMapper, FlwHisInstanceMapper hisInstanceMapper,
                                     FlwTaskMapper taskMapper, FlwTaskActorMapper taskActorMapper,
                                     FlwHisTaskMapper hisTaskMapper, FlwHisTaskActorMapper hisTaskActorMapper) {
        return new QueryServiceImpl(instanceMapper, hisInstanceMapper, taskMapper, taskActorMapper, hisTaskMapper, hisTaskActorMapper);
    }

    @Bean
    @ConditionalOnMissingBean
    public RuntimeService runtimeService(@Autowired(required = false) InstanceListener instanceListener, QueryService queryService,
                                         TaskService taskService, FlwInstanceMapper instanceMapper, FlwHisInstanceMapper hisInstanceMapper,
                                         FlwExtInstanceMapper extInstanceMapper) {
        return new RuntimeServiceImpl(instanceListener, queryService, taskService, instanceMapper, hisInstanceMapper, extInstanceMapper);
    }

    @Bean
    @ConditionalOnMissingBean
    public ProcessService processService(RuntimeService runtimeService, FlwProcessMapper processMapper) {
        return new ProcessServiceImpl(runtimeService, processMapper);
    }

    @Bean
    @ConditionalOnMissingBean
    public JobLock jobLock() {
        return new LocalLock();
    }

    @Bean
    @ConditionalOnMissingBean
    public Expression expression() {
        return new SpelExpression();
    }

    @Bean
    @ConditionalOnMissingBean
    public TaskAccessStrategy taskAccessStrategy() {
        return new GeneralAccessStrategy();
    }


    @Bean
    @ConditionalOnMissingBean
    public TaskActorProvider taskActorProvider() {
        return new GeneralTaskActorProvider();
    }

    @Bean
    @ConditionalOnMissingBean
    public FlowLongEngine flowLongEngine() {
        return new FlowLongEngineImpl();
    }

    @Bean
    @ConditionalOnMissingBean
    public FlowLongContext flowLongContext(ProcessService processService, QueryService queryService, RuntimeService runtimeService,
                                           TaskService taskService, Expression expression, TaskAccessStrategy taskAccessStrategy,
                                           TaskActorProvider taskActorProvider, FlowLongEngine flowLongEngine,
                                           @Autowired(required = false) ProcessModelParser processModelParser,
                                           @Autowired(required = false) FlowJsonHandler flowJsonHandler,
                                           @Autowired(required = false) ConditionArgsHandler conditionArgsHandler,
                                           @Autowired(required = false) CreateTaskHandler createTaskHandler) {
        // 静态注入 Jackson 解析 JSON 处理器
        if (null == flowJsonHandler) {
            flowJsonHandler = new FlowJacksonHandler();
        }
        FlowLongContext.setFlowJsonHandler(flowJsonHandler);
        // 注入 FlowLong 上下文
        FlowLongContext flc = new FlowLongContext(processModelParser);
        flc.setProcessService(processService);
        flc.setQueryService(queryService);
        flc.setRuntimeService(runtimeService);
        flc.setTaskService(taskService);
        flc.setExpression(expression);
        flc.setTaskAccessStrategy(taskAccessStrategy);
        flc.setTaskActorProvider(taskActorProvider);
        flc.setConditionArgsHandler(conditionArgsHandler);
        flc.setCreateTaskHandler(createTaskHandler);
        return flc.build(flowLongEngine);
    }

    @Bean
    @ConditionalOnBean({FlowLongContext.class, TaskReminder.class})
    @ConditionalOnMissingBean
    public SpringBootScheduler springBootScheduler(FlowLongContext flowLongContext, FlowLongProperties properties,
                                                   TaskReminder taskReminder, JobLock jobLock) {
        SpringBootScheduler scheduler = new SpringBootScheduler();
        scheduler.setContext(flowLongContext);
        scheduler.setRemindParam(properties.getRemind());
        scheduler.setTaskReminder(taskReminder);
        scheduler.setJobLock(jobLock);
        return scheduler;
    }

    /**
     * 注入自定义 TaskListener 实现该方法不再生效
     *
     * @param eventPublisher {@link ApplicationEventPublisher}
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "flowlong", name = "eventing.task", havingValue = "true")
    public EventTaskListener taskListener(ApplicationEventPublisher eventPublisher) {
        return new EventTaskListener(eventPublisher);
    }

    /**
     * 注入自定义 InstanceListener 实现该方法不再生效
     *
     * @param eventPublisher {@link ApplicationEventPublisher}
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "flowlong", name = "eventing.instance", havingValue = "true")
    public EventInstanceListener instanceListener(ApplicationEventPublisher eventPublisher) {
        return new EventInstanceListener(eventPublisher);
    }
}
