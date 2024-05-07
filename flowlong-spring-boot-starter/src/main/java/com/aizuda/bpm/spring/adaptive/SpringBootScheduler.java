package com.aizuda.bpm.spring.adaptive;

import com.aizuda.bpm.engine.FlowLongEngine;
import com.aizuda.bpm.engine.TaskService;
import com.aizuda.bpm.engine.assist.DateUtils;
import com.aizuda.bpm.engine.assist.ObjectUtils;
import com.aizuda.bpm.engine.core.FlowCreator;
import com.aizuda.bpm.engine.core.FlowLongContext;
import com.aizuda.bpm.engine.core.enums.EventType;
import com.aizuda.bpm.engine.core.enums.TaskState;
import com.aizuda.bpm.engine.entity.FlwTask;
import com.aizuda.bpm.engine.scheduling.JobLock;
import com.aizuda.bpm.engine.scheduling.RemindParam;
import com.aizuda.bpm.engine.scheduling.TaskReminder;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
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


    private FlowLongEngine flowLongEngine;

    /**
     * 流程提醒处理
     */
    public void remind() {
        try {
            if (!jobLock.tryLock()) {
                log.info("[FlowLong] remind is already running, just return.");
                return;
            }
            TaskService taskService = context.getTaskService();
            List<FlwTask> flwTaskList = taskService.getTimeoutOrRemindTasks();
            if (ObjectUtils.isNotEmpty(flwTaskList)) {
                Date currentDate = DateUtils.getCurrentDate();
                for (FlwTask flwTask : flwTaskList) {
                    if (null != flwTask.getRemindTime() && DateUtils.after(flwTask.getRemindTime(), currentDate)) {
                        /*
                         * 任务提醒
                         */
                        if (flwTask.getRemindRepeat() > 0) {
                            // 1，更新提醒次数减去 1 次
                            FlwTask temp = new FlwTask();
                            temp.setId(flwTask.getId());
                            temp.setRemindRepeat(flwTask.getRemindRepeat() - 1);
                            taskService.updateTaskById(temp, null);

                            // 2，调用提醒接口
                            taskReminder.remind(context, flwTask.getInstanceId(), flwTask.getId());
                        }
                    } else {
                        /*
                         * 任务超时
                         */
                        if (flwTask.getTermMode() == null) {
                            context.getRuntimeService().timeout(flwTask.getInstanceId());
                        }else {
                            //自动同意或拒绝
                            if (flwTask.getTermMode() == 0){
                                flowLongEngine.autoCompleteTask(flwTask.getId());
                            }else if (flwTask.getTermMode() == 1){
                                flowLongEngine.autoRejectTask(flwTask.getId());
                            }
                        }
                    }
                }
            }
        } finally {
            jobLock.unlock();
        }
    }

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        taskRegistrar.addTriggerTask(this::remind, triggerContext ->
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
