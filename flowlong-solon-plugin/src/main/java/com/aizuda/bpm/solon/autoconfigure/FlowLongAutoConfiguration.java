/*
 * Copyright 2023-2025 Licensed under the AGPL License
 */
package com.aizuda.bpm.solon.autoconfigure;

import com.aizuda.bpm.engine.*;
import com.aizuda.bpm.engine.cache.FlowCache;
import com.aizuda.bpm.engine.core.FlowLongContext;
import com.aizuda.bpm.engine.core.FlowLongEngineImpl;
import com.aizuda.bpm.engine.dao.*;
import com.aizuda.bpm.engine.handler.ConditionNodeHandler;
import com.aizuda.bpm.engine.handler.CreateTaskHandler;
import com.aizuda.bpm.engine.handler.FlowJsonHandler;
import com.aizuda.bpm.engine.impl.*;
import com.aizuda.bpm.engine.listener.InstanceListener;
import com.aizuda.bpm.engine.listener.TaskListener;
import com.aizuda.bpm.engine.scheduling.JobLock;
import com.aizuda.bpm.engine.scheduling.LocalLock;
import com.aizuda.bpm.solon.adaptive.SolonExpression;
import com.aizuda.bpm.solon.adaptive.SolonFlowJsonHandler;
import com.aizuda.bpm.solon.adaptive.SolonScheduler;
import org.noear.solon.annotation.Bean;
import org.noear.solon.annotation.Condition;
import org.noear.solon.annotation.Configuration;
import org.noear.solon.annotation.Inject;
import org.noear.solon.scheduling.ScheduledAnno;
import org.noear.solon.scheduling.scheduled.manager.IJobManager;

/**
 * 配置处理类
 *
 * <p>
 * 尊重知识产权，不允许非法使用，后果自负
 * </p>
 *
 * @author hubin
 * @author noear
 * @since 1.0
 */
@Configuration
public class FlowLongAutoConfiguration {

    @Bean
    @Condition(onMissingBean = TaskService.class)
    public TaskService taskService(@Inject(required = false) TaskAccessStrategy taskAccessStrategy, @Inject(required = false) TaskListener taskListener,
                                   @Inject(required = false) TaskTrigger taskTrigger, FlwInstanceDao instanceDao, FlwExtInstanceDao extInstanceDao,
                                   FlwHisInstanceDao hisInstanceDao, FlwTaskDao taskDao, FlwTaskActorDao taskActorDao,
                                   FlwHisTaskDao hisTaskDao, FlwHisTaskActorDao hisTaskActorDao) {
        return new TaskServiceImpl(taskAccessStrategy, taskListener, taskTrigger, instanceDao, extInstanceDao, hisInstanceDao,
                taskDao, taskActorDao, hisTaskDao, hisTaskActorDao);
    }

    @Bean
    @Condition(onMissingBean = QueryService.class)
    public QueryService queryService(FlwInstanceDao instanceDao, FlwHisInstanceDao hisInstanceDao, FlwExtInstanceDao extInstanceDao,
                                     FlwTaskDao taskDao, FlwTaskActorDao taskActorDao, FlwHisTaskDao hisTaskDao, FlwHisTaskActorDao hisTaskActorDao) {
        return new QueryServiceImpl(instanceDao, hisInstanceDao, extInstanceDao, taskDao, taskActorDao, hisTaskDao, hisTaskActorDao);
    }

    @Bean
    @Condition(onMissingBean = RuntimeService.class)
    public RuntimeService runtimeService(@Inject(required = false) InstanceListener instanceListener, QueryService queryService,
                                         TaskService taskService, FlwInstanceDao instanceDao, FlwHisInstanceDao hisInstanceDao,
                                         FlwExtInstanceDao extInstanceDao) {
        return new RuntimeServiceImpl(instanceListener, queryService, taskService, instanceDao, hisInstanceDao, extInstanceDao);
    }

    @Bean
    @Condition(onMissingBean = ProcessService.class)
    public ProcessService processService(RuntimeService runtimeService, FlwProcessDao processDao) {
        return new ProcessServiceImpl(runtimeService, processDao);
    }

    @Bean
    @Condition(onMissingBean = Expression.class)
    public Expression expression() {
        return new SolonExpression();
    }

    @Bean
    @Condition(onMissingBean = TaskAccessStrategy.class)
    public TaskAccessStrategy taskAccessStrategy() {
        return new GeneralAccessStrategy();
    }


    @Bean
    @Condition(onMissingBean = TaskActorProvider.class)
    public TaskActorProvider taskActorProvider() {
        return new GeneralTaskActorProvider();
    }

    @Bean
    @Condition(onMissingBean = FlowLongEngine.class)
    public FlowLongEngine flowLongEngine() {
        return new FlowLongEngineImpl();
    }

    @Bean
    @Condition(onMissingBean = FlowLongContext.class)
    public FlowLongContext flowLongContext(ProcessService processService,
                                           QueryService queryService,
                                           RuntimeService runtimeService,
                                           TaskService taskService,
                                           Expression expression,
                                           TaskAccessStrategy taskAccessStrategy,
                                           TaskActorProvider taskActorProvider,
                                           FlowLongEngine flowLongEngine,
                                           @Inject(required = false) FlowCache flowCache,
                                           @Inject(required = false) ProcessModelParser processModelParser,
                                           @Inject(required = false) FlowJsonHandler flowJsonHandler,
                                           @Inject(required = false) ConditionNodeHandler conditionNodeHandler,
                                           @Inject(required = false) TaskCreateInterceptor taskCreateInterceptor,
                                           @Inject(required = false) CreateTaskHandler createTaskHandler,
                                           @Inject(required = false) TaskReminder taskReminder,
                                           @Inject(required = false) TaskTrigger taskTrigger) {

        // 静态注入 Jackson 解析 JSON 处理器
        if (null == flowJsonHandler) {
            flowJsonHandler = new SolonFlowJsonHandler();
        }
        FlowLongContext.setFlowJsonHandler(flowJsonHandler);
        // 注入 FlowLong 上下文
        FlowLongContext flc = new FlowLongContext(flowCache, processModelParser);
        flc.setProcessService(processService);
        flc.setQueryService(queryService);
        flc.setRuntimeService(runtimeService);
        flc.setTaskService(taskService);
        flc.setExpression(expression);
        flc.setTaskAccessStrategy(taskAccessStrategy);
        flc.setTaskActorProvider(taskActorProvider);
        flc.setConditionNodeHandler(conditionNodeHandler);
        flc.setTaskCreateInterceptor(taskCreateInterceptor);
        flc.setCreateTaskHandler(createTaskHandler);
        flc.setTaskReminder(taskReminder);
        flc.setTaskTrigger(taskTrigger);
        return flc.build(flowLongEngine);
    }

    @Bean
    @Condition(onMissingBean = JobLock.class)
    public JobLock jobLock() {
        return new LocalLock();
    }

    @Bean
    public void scheduler(FlowLongContext flowLongContext,
                          FlowLongProperties properties,
                          @Inject(required = false) TaskReminder taskReminder,
                          JobLock jobLock,
                          IJobManager jobManager) {
        if (taskReminder == null) {
            return;
        }

        SolonScheduler scheduler = new SolonScheduler();
        scheduler.setContext(flowLongContext);
        scheduler.setRemindParam(properties.getRemind());
        scheduler.setTaskReminder(taskReminder);
        scheduler.setJobLock(jobLock);

        //注册 job（不再需要返回了）
        jobManager.jobAdd("flowlong",
                new ScheduledAnno().cron(scheduler.getRemindParam().getCron()), ctx -> {
                    scheduler.remind();
                });

    }
}
