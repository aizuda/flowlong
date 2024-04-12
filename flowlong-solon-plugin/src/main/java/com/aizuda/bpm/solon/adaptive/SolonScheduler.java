/*
 * Copyright 2023-2025 Licensed under the AGPL License
 */
package com.aizuda.bpm.solon.adaptive;

import com.aizuda.bpm.engine.TaskService;
import com.aizuda.bpm.engine.assist.DateUtils;
import com.aizuda.bpm.engine.assist.ObjectUtils;
import com.aizuda.bpm.engine.core.FlowLongContext;
import com.aizuda.bpm.engine.entity.FlwTask;
import com.aizuda.bpm.engine.scheduling.JobLock;
import com.aizuda.bpm.engine.scheduling.RemindParam;
import com.aizuda.bpm.engine.scheduling.TaskReminder;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;
import java.util.List;

/**
 * Solon 定时任务实现流程提醒处理类适配（其实没适配，哈哈）
 *
 * <p>
 * 尊重知识产权，不允许非法使用，后果自负
 * </p>
 *
 * @author noear
 * @since 1.0
 */
@Slf4j
@Setter
@Getter
public class SolonScheduler {

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
                        context.getRuntimeService().timeout(flwTask.getInstanceId());
                    }
                }
            }
        } finally {
            jobLock.unlock();
        }
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
