/* Copyright 2023-2025 jobob@qq.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.flowlong.bpm.engine.impl;

import com.flowlong.bpm.engine.FlowLongInterceptor;
import com.flowlong.bpm.engine.core.Execution;
import com.flowlong.bpm.engine.core.FlowLongContext;
import com.flowlong.bpm.engine.entity.Task;
import com.flowlong.bpm.engine.scheduling.FlowLongScheduler;
import com.flowlong.bpm.engine.scheduling.JobEntity;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;
import java.util.Map;

/**
 * 时限控制拦截器
 * 主要拦截任务的expireDate(期望完成时间)
 * 再交给具体的调度器完成调度处理
 *
 * <p>
 * 尊重知识产权，CV 请保留版权，爱组搭 http://aizuda.com 出品
 * </p>
 *
 * @author hubin
 * @since 1.0
 */
@Slf4j
public class SchedulerInterceptor implements FlowLongInterceptor {
    /**
     * 调度器接口
     */
    private FlowLongScheduler scheduler;
    /**
     * 是否调度
     */
    private boolean isScheduled = true;

    /**
     * 时限控制拦截方法
     */
    @Override
    public void handle(FlowLongContext flowLongContext, Execution execution) {
        if (!isScheduled) {
            return;
        }
        for (Task task : execution.getTasks()) {
            String id = execution.getProcess().getId()
                    + "-" + execution.getInstance().getId()
                    + "-" + task.getId();
            Date expireDate = task.getExpireTime();
            if (expireDate != null) {
                schedule(flowLongContext, id, task, expireDate, JobEntity.JobType.EXECUTER.ordinal(), execution.getArgs());
            }
//            Date remindDate = task.getRemindDate();
//            if (remindDate != null) {
//                schedule(flowLongContext, id, task, remindDate, JobEntity.JobType.REMINDER.ordinal(), execution.getArgs());
//            }
        }
    }

    public void schedule(FlowLongContext flowLongContext, String id, Task task, Date startDate, int jobType, Map<String, Object> args) {
        try {
            JobEntity entity = new JobEntity(id, task, startDate, args);
            entity.setModelName(task.getTaskName());
            entity.setJobType(jobType);
            if (jobType == JobEntity.JobType.REMINDER.ordinal()) {
//                TaskModel taskModel = task.getTaskModel();
//                if (taskModel != null && NumberUtils.isNumber(taskModel.getReminderRepeat())) {
//                    entity.setPeriod(Integer.parseInt(taskModel.getReminderRepeat()));
//                }
            }
            if (scheduler == null) {
                scheduler = flowLongContext.getScheduler();
            }
            if (scheduler != null) {
                scheduler.schedule(entity);
            } else {
                isScheduled = false;
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            log.info("scheduler failed.task is:" + task);
        }
    }
}
