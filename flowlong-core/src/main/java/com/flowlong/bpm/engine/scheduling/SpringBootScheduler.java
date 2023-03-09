package com.flowlong.bpm.engine.scheduling;

import com.flowlong.bpm.engine.core.FlowLongContext;
import lombok.Getter;
import lombok.Setter;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.scheduling.support.CronTrigger;


/**
 * Spring Boot 内置定时任务实现流程提醒处理类
 *
 * <p>
 * 尊重知识产权，CV 请保留版权，爱组搭 http://aizuda.com 出品
 * </p>
 *
 * @author hubin
 * @since 1.0
 */
@Getter
@Setter
public class SpringBootScheduler implements SchedulingConfigurer {
    /**
     * 流程引擎上下文
     */
    private FlowLongContext context;
    /**
     * 任务提醒接口
     */
    private TaskReminder taskReminder;
    /**
     * 任务锁，可注入分布式锁实现
     */
    private JobLock jobLock;
    /**
     * 提醒参数
     */
    private RemindParam remindParam;

    /**
     * 流程提醒处理
     */
    public void remind() {
        try {
            jobLock.lock();

            // TODO 数据库中定时读取待提醒流程实例和任务
            taskReminder.remind(context, null, null);
        } finally {
            jobLock.unlock();
        }
    }

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        taskRegistrar.addTriggerTask(() -> remind(), triggerContext ->
                new CronTrigger(remindParam.getCron()).nextExecutionTime(triggerContext));
    }

    public void setRemindParam(RemindParam remindParam) {
        if (null == remindParam) {
            /*
             * 未配置定时任务提醒参数，默认 cron 为5秒钟执行一次
             */
            remindParam = new RemindParam();
            remindParam.setCron("*/5 * * * * ?");
        }
        this.remindParam = remindParam;
    }
}
