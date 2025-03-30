/*
 * Copyright 2023-2025 Licensed under the Dual Licensing
 * website: https://aizuda.com
 */
package com.aizuda.bpm.engine;

import com.aizuda.bpm.engine.assist.Assert;
import com.aizuda.bpm.engine.assist.DateUtils;
import com.aizuda.bpm.engine.assist.ObjectUtils;
import com.aizuda.bpm.engine.core.FlowCreator;
import com.aizuda.bpm.engine.core.FlowLongContext;
import com.aizuda.bpm.engine.core.enums.TaskType;
import com.aizuda.bpm.engine.entity.FlwTask;
import com.aizuda.bpm.engine.model.NodeModel;
import com.aizuda.bpm.engine.model.ProcessModel;
import com.aizuda.bpm.engine.scheduling.JobLock;
import com.aizuda.bpm.engine.scheduling.RemindParam;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * 定时任务实现流程提醒超时处理类
 *
 * <p>
 * <a href="https://aizuda.com">官网</a>尊重知识产权，不允许非法使用，后果自负
 * </p>
 *
 * @author hubin
 * @since 1.0
 */
@Slf4j
@Getter
@Setter
public abstract class FlowLongScheduler {
    /**
     * FlowLong流程引擎接口
     */
    private FlowLongEngine flowLongEngine;
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
                log.info("[FlowLong] Scheduling is already running, just return.");
                return;
            }
            FlowLongContext context = flowLongEngine.getContext();
            TaskService taskService = context.getTaskService();
            List<FlwTask> flwTaskList = taskService.getTimeoutOrRemindTasks();
            if (ObjectUtils.isNotEmpty(flwTaskList)) {
                for (FlwTask flwTask : flwTaskList) {
                    /*
                     * 任务提醒
                     */
                    if (null != flwTask.getRemindTime()) {
                        // 1，更新提醒次数加 1 次
                        FlwTask temp = new FlwTask();
                        temp.setId(flwTask.getId());
                        int remindRepeat = 1;
                        if (null != flwTask.getRemindRepeat()) {
                            remindRepeat += flwTask.getRemindRepeat();
                        }
                        temp.setRemindRepeat(remindRepeat);
                        // 2，调用提醒接口
                        TaskReminder taskReminder = context.getTaskReminder();
                        Assert.isNull(taskReminder, "Please make sure to implement the interface TaskReminder");
                        Date nextRemindTime = taskReminder.remind(context, flwTask.getInstanceId(), flwTask);
                        if (null != nextRemindTime) {
                            temp.setRemindTime(nextRemindTime);
                        }
                        taskService.updateTaskById(temp, this.getAutoFlowCreator());
                    }
                    /*
                     * 任务超时
                     */
                    else if (null != flwTask.getExpireTime()) {
                        // 定时器任务或触发器任务直接执行通过
                        if (TaskType.timer.eq(flwTask.getTaskType()) || TaskType.trigger.eq(flwTask.getTaskType())) {
                            if (!flowLongEngine.autoCompleteTask(flwTask.getId(), this.getAutoFlowCreator())) {
                                log.info("Scheduling [taskName={}] failed to execute autoCompleteTask", flwTask.getTaskName());
                            }
                            continue;
                        }

                        // 获取当前执行模型节点
                        ProcessModel processModel = flowLongEngine.runtimeService().getProcessModelByInstanceId(flwTask.getInstanceId());
                        NodeModel nodeModel = processModel.getNode(flwTask.getTaskKey());

                        // 超时自动审批
                        Boolean termAuto = nodeModel.getTermAuto();
                        if (termAuto != null && termAuto) {
                            Integer termMode = nodeModel.getTermMode();
                            if (null == termMode) {
                                // 执行超时
                                context.getRuntimeService().timeout(flwTask.getInstanceId());
                            } else if (Objects.equals(termMode, 0)) {
                                // 自动通过
                                if (!flowLongEngine.autoCompleteTask(flwTask.getId(), this.getAutoFlowCreator())) {
                                    log.info("Scheduling failed to execute autoCompleteTask, taskId = {}", flwTask.getId());
                                }
                            } else if (Objects.equals(termMode, 1)) {
                                // 自动拒绝
                                if (!flowLongEngine.autoRejectTask(flwTask, this.getAutoFlowCreator())) {
                                    log.info("Scheduling failed to execute autoRejectTask, taskId = {}", flwTask.getId());
                                }
                            }
                        }
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

    /**
     * 自动完成流程任务创建者
     * <p>默认为管理员，子类可以重写为自定义用户</p>
     */
    public FlowCreator getAutoFlowCreator() {
        return FlowCreator.ADMIN;
    }
}
