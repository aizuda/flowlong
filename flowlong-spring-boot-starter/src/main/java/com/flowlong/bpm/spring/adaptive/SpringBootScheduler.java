package com.flowlong.bpm.spring.adaptive;

import com.flowlong.bpm.engine.TaskService;
import com.flowlong.bpm.engine.assist.DateUtils;
import com.flowlong.bpm.engine.assist.ObjectUtils;
import com.flowlong.bpm.engine.core.FlowLongContext;
import com.flowlong.bpm.engine.entity.FlwTask;
import com.flowlong.bpm.engine.scheduling.JobLock;
import com.flowlong.bpm.engine.scheduling.RemindParam;
import com.flowlong.bpm.engine.scheduling.TaskReminder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.scheduling.support.CronTrigger;

import java.util.Date;
import java.util.List;


/**
 * Spring Boot 内置定时任务实现流程提醒处理类
 *
 * <p>
 * 尊重知识产权，不允许非法使用，后果自负
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
            TaskService taskService = context.getTaskService();
            List<FlwTask> flwTaskList = taskService.getTimeoutOrRemindTasks();
            if (ObjectUtils.isNotEmpty(flwTaskList)) {
                Date currentDate = DateUtils.getCurrentDate();
                for (FlwTask flwTask : flwTaskList) {
                    if (null != flwTask.getRemindTime() && DateUtils.after(flwTask.getRemindTime(), currentDate)) {
                        /**
                         * 任务提醒
                         */
                        try {
                            if (flwTask.getRemindRepeat() > 0) {
                                // 1，更新提醒次数减去 1 次
                                FlwTask temp = new FlwTask();
                                temp.setId(flwTask.getId());
                                temp.setRemindRepeat(flwTask.getRemindRepeat() - 1);
                                taskService.updateTaskById(temp);

                                // 2，调用提醒接口
                                taskReminder.remind(context, flwTask.getInstanceId(), flwTask.getId());
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        /**
                         * 任务超时
                         */
                        context.getRuntimeService().timeout(flwTask.getInstanceId());
                    }
                }
            }
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
