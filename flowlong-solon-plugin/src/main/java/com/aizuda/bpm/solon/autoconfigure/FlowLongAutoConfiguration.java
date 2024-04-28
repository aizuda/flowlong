/*
 * Copyright 2023-2025 Licensed under the AGPL License
 */
package com.aizuda.bpm.solon.autoconfigure;

import com.aizuda.bpm.engine.*;
import com.aizuda.bpm.engine.cache.FlowCache;
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
                                   @Inject(required = false) TaskTrigger taskTrigger, FlwInstanceMapper instanceMapper, FlwExtInstanceMapper extInstanceMapper,
                                   FlwHisInstanceMapper hisInstanceMapper, FlwTaskMapper taskMapper, FlwTaskActorMapper taskActorMapper,
                                   FlwHisTaskMapper hisTaskMapper, FlwHisTaskActorMapper hisTaskActorMapper) {
        return new TaskServiceImpl(taskAccessStrategy, taskListener, taskTrigger, instanceMapper, extInstanceMapper, hisInstanceMapper,
                taskMapper, taskActorMapper, hisTaskMapper, hisTaskActorMapper);
    }

    @Bean
    @Condition(onMissingBean = QueryService.class)
    public QueryService queryService(FlwInstanceMapper instanceMapper, FlwHisInstanceMapper hisInstanceMapper,
                                     FlwTaskMapper taskMapper, FlwTaskActorMapper taskActorMapper,
                                     FlwHisTaskMapper hisTaskMapper, FlwHisTaskActorMapper hisTaskActorMapper) {
        return new QueryServiceImpl(instanceMapper, hisInstanceMapper, taskMapper, taskActorMapper, hisTaskMapper, hisTaskActorMapper);
    }

    @Bean
    @Condition(onMissingBean = RuntimeService.class)
    public RuntimeService runtimeService(@Inject(required = false) InstanceListener instanceListener, QueryService queryService,
                                         TaskService taskService, FlwInstanceMapper instanceMapper, FlwHisInstanceMapper hisInstanceMapper,
                                         FlwExtInstanceMapper extInstanceMapper) {
        return new RuntimeServiceImpl(instanceListener, queryService, taskService, instanceMapper, hisInstanceMapper, extInstanceMapper);
    }

    @Bean
    @Condition(onMissingBean = ProcessService.class)
    public ProcessService processService(RuntimeService runtimeService, FlwProcessMapper processMapper) {
        return new ProcessServiceImpl(runtimeService, processMapper);
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
                                           @Inject(required = false) ConditionArgsHandler conditionArgsHandler,
                                           @Inject(required = false) CreateTaskHandler createTaskHandler) {

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
        flc.setConditionArgsHandler(conditionArgsHandler);
        flc.setCreateTaskHandler(createTaskHandler);
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
