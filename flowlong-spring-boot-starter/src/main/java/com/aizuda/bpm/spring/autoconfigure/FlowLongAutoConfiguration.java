/*
 * Copyright 2023-2025 Licensed under the Dual Licensing
 * website: https://aizuda.com
 */
package com.aizuda.bpm.spring.autoconfigure;

import com.aizuda.bpm.engine.*;
import com.aizuda.bpm.engine.cache.FlowCache;
import com.aizuda.bpm.engine.core.FlowLongContext;
import com.aizuda.bpm.engine.core.FlowLongEngineImpl;
import com.aizuda.bpm.engine.dao.*;
import com.aizuda.bpm.engine.handler.*;
import com.aizuda.bpm.engine.handler.impl.SimpleFlowCreateTimeHandler;
import com.aizuda.bpm.engine.impl.*;
import com.aizuda.bpm.engine.listener.InstanceListener;
import com.aizuda.bpm.engine.listener.TaskListener;
import com.aizuda.bpm.engine.scheduling.JobLock;
import com.aizuda.bpm.engine.scheduling.LocalLock;
import com.aizuda.bpm.spring.adaptive.FlowJacksonHandler;
import com.aizuda.bpm.spring.adaptive.SpelFlowLongExpression;
import com.aizuda.bpm.spring.event.EventInstanceListener;
import com.aizuda.bpm.spring.event.EventTaskListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * spring boot starter 启动自动配置处理类
 *
 * <p>
 * <a href="https://aizuda.com">官网</a>尊重知识产权，不允许非法使用，后果自负
 * </p>
 *
 * @author hubin
 * @since 1.0
 */
@Configuration
@Import(MybatisPlusConfiguration.class)
@EnableConfigurationProperties(FlowLongProperties.class)
public class FlowLongAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public FlowLongIdGenerator flowLongIdGenerator() {
        return new DefaultFlowLongIdGenerator();
    }

    @Bean
    @ConditionalOnMissingBean
    public TaskService taskService(@Autowired(required = false) TaskAccessStrategy taskAccessStrategy, @Autowired(required = false) TaskListener taskListener,
                                   @Autowired(required = false) TaskTrigger taskTrigger, FlowLongIdGenerator flowLongIdGenerator, FlwInstanceDao instanceDao, FlwExtInstanceDao extInstanceDao,
                                   FlwHisInstanceDao hisInstanceDao, FlwTaskDao taskDao, FlwTaskActorDao taskActorDao,
                                   FlwHisTaskDao hisTaskDao, FlwHisTaskActorDao hisTaskActorDao) {
        return new TaskServiceImpl(taskAccessStrategy, taskListener, taskTrigger, flowLongIdGenerator, instanceDao, extInstanceDao, hisInstanceDao,
                taskDao, taskActorDao, hisTaskDao, hisTaskActorDao);
    }

    @Bean
    @ConditionalOnMissingBean
    public QueryService queryService(FlwInstanceDao instanceDao, FlwHisInstanceDao hisInstanceDao, FlwExtInstanceDao extInstanceDao,
                                     FlwTaskDao taskDao, FlwTaskActorDao taskActorDao, FlwHisTaskDao hisTaskDao, FlwHisTaskActorDao hisTaskActorDao) {
        return new QueryServiceImpl(instanceDao, hisInstanceDao, extInstanceDao, taskDao, taskActorDao, hisTaskDao, hisTaskActorDao);
    }

    @Bean
    @ConditionalOnMissingBean
    public RuntimeService runtimeService(@Autowired(required = false) InstanceListener instanceListener, FlowLongIdGenerator flowLongIdGenerator, QueryService queryService,
                                         TaskService taskService, FlwInstanceDao instanceDao, FlwHisInstanceDao hisInstanceDao,
                                         FlwExtInstanceDao extInstanceDao) {
        return new RuntimeServiceImpl(instanceListener, flowLongIdGenerator, queryService, taskService, instanceDao, hisInstanceDao, extInstanceDao);
    }

    @Bean
    @ConditionalOnMissingBean
    public ProcessService processService(RuntimeService runtimeService, FlowLongIdGenerator flowLongIdGenerator, FlwProcessDao processDao) {
        return new ProcessServiceImpl(runtimeService, flowLongIdGenerator, processDao);
    }

    @Bean
    @ConditionalOnMissingBean
    public JobLock jobLock() {
        return new LocalLock();
    }

    @Bean
    @ConditionalOnMissingBean
    public FlowLongExpression flowLongExpression() {
        return new SpelFlowLongExpression();
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
                                           TaskService taskService, FlowLongExpression flowLongExpression, TaskAccessStrategy taskAccessStrategy,
                                           TaskActorProvider taskActorProvider, FlowLongEngine flowLongEngine, FlowLongProperties flp,
                                           @Autowired(required = false) FlowCache flowCache,
                                           @Autowired(required = false) ProcessModelParser processModelParser,
                                           @Autowired(required = false) FlowJsonHandler flowJsonHandler,
                                           @Autowired(required = false) FlowCreateTimeHandler flowCreateTimeHandler,
                                           @Autowired(required = false) FlowAiHandler flowAiHandler,
                                           @Autowired(required = false) ConditionNodeHandler conditionNodeHandler,
                                           @Autowired(required = false) TaskCreateInterceptor taskCreateInterceptor,
                                           @Autowired(required = false) CreateTaskHandler createTaskHandler,
                                           @Autowired(required = false) TaskReminder taskReminder,
                                           @Autowired(required = false) TaskTrigger taskTrigger) {
        // 静态注入 Jackson 解析 JSON 处理器
        if (null == flowJsonHandler) {
            flowJsonHandler = new FlowJacksonHandler();
        }
        FlowLongContext.setFlowJsonHandler(flowJsonHandler);
        // 静态注入流程创建时间处理器
        if (null == flowCreateTimeHandler) {
            flowCreateTimeHandler = new SimpleFlowCreateTimeHandler();
        }
        FlowLongContext.setFlowCreateTimeHandler(flowCreateTimeHandler);
        // 注入 FlowLong 上下文
        FlowLongContext flc = new FlowLongContext(flowCache, processModelParser);
        flc.setProcessService(processService);
        flc.setQueryService(queryService);
        flc.setRuntimeService(runtimeService);
        flc.setTaskService(taskService);
        flc.setFlowLongExpression(flowLongExpression);
        flc.setTaskAccessStrategy(taskAccessStrategy);
        flc.setTaskActorProvider(taskActorProvider);
        flc.setFlowAiHandler(flowAiHandler);
        flc.setConditionNodeHandler(conditionNodeHandler);
        flc.setTaskCreateInterceptor(taskCreateInterceptor);
        flc.setCreateTaskHandler(createTaskHandler);
        flc.setTaskReminder(taskReminder);
        flc.setTaskTrigger(taskTrigger);
        return flc.build(flowLongEngine, flp.isBanner());
    }

    /**
     * 注入自定义 TaskListener 实现该方法不再生效
     *
     * @param eventPublisher {@link ApplicationEventPublisher}
     * @return {@link EventTaskListener}
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
     * @return {@link EventInstanceListener}
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "flowlong", name = "eventing.instance", havingValue = "true")
    public EventInstanceListener instanceListener(ApplicationEventPublisher eventPublisher) {
        return new EventInstanceListener(eventPublisher);
    }
}
