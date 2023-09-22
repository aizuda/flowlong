package com.flowlong.bpm.solon.autoconfigure;


import com.flowlong.bpm.engine.*;
import com.flowlong.bpm.engine.core.FlowLongContext;
import com.flowlong.bpm.engine.scheduling.JobLock;
import com.flowlong.bpm.engine.scheduling.LocalLock;
import com.flowlong.bpm.engine.scheduling.TaskReminder;
import com.flowlong.bpm.solon.adaptive.SolonFlowJsonHandler;
import com.flowlong.bpm.solon.adaptive.SolonScheduler;
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
 * 尊重知识产权，CV 请保留版权，爱组搭 http://aizuda.com 出品，不允许非法使用，后果自负
 * </p>
 *
 * @author hubin
 * @author noear
 * @since 1.0
 */
@Configuration
public class FlowLongAutoConfiguration {
    @Bean
    public FlowLongProperties properties(@Inject("${flowlong}") FlowLongProperties properties){
        return properties;
    }

    @Bean
    @Condition(onMissingBean = FlowLongContext.class)
    public FlowLongContext flowLongContext(ProcessService processService, QueryService queryService,
                                           RuntimeService runtimeService, TaskService taskService) {
        // 静态注入 Jackson 解析 JSON 处理器
        FlowLongContext.setFlowJsonHandler(new SolonFlowJsonHandler());
        // 注入 FlowLong 上下文
        FlowLongContext flc = new FlowLongContext();
        flc.setProcessService(processService);
        flc.setQueryService(queryService);
        flc.setRuntimeService(runtimeService);
        flc.setTaskService(taskService);
        return flc;
    }

    @Bean
    @Condition(onMissingBean = FlowLongEngine.class)
    public FlowLongEngine flowLongEngine(FlowLongContext flowLongContext) {
        return flowLongContext.build();
    }

    @Bean
    @Condition(onMissingBean = JobLock.class)
    public JobLock jobLock() {
        return new LocalLock();
    }

    @Bean
    public void scheduler(FlowLongContext flowLongContext,
                                    FlowLongProperties properties,
                                    TaskReminder taskReminder,
                                    JobLock jobLock,
                                    IJobManager jobManager) {
        if (flowLongContext == null || taskReminder == null) {
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
