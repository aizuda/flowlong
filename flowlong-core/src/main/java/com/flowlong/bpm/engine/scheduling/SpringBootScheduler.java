package com.flowlong.bpm.engine.scheduling;

import com.flowlong.bpm.engine.TaskService;
import com.flowlong.bpm.engine.assist.DateUtils;
import com.flowlong.bpm.engine.assist.ObjectUtils;
import com.flowlong.bpm.engine.core.FlowLongContext;
import com.flowlong.bpm.engine.entity.Task;
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
            TaskService taskService = context.getTaskService();
            List<Task> taskList = taskService.getTimeoutOrRemindTasks();
            if (ObjectUtils.isNotEmpty(taskList)) {
                Date currentDate = DateUtils.getCurrentDate();
                for (Task task : taskList) {
                    if (null != task.getRemindTime() && DateUtils.after(task.getRemindTime(), currentDate)) {
                        /**
                         * 任务提醒
                         */
                        try {
                            // 1，更新提醒次数减去 1 次
                            Task temp = new Task();
                            temp.setId(task.getId());
                            temp.setRemindRepeat(task.getRemindRepeat() - 1);
                            taskService.updateTaskById(temp);

                            // 2，调用提醒接口
                            taskReminder.remind(context, task.getInstanceId(), task.getId());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        /**
                         * 任务超时
                         */
                        taskService.taskTimeout(task.getId());
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
