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
package com.flowlong.bpm.solon.adaptive;

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

import java.util.Date;
import java.util.List;

/**
 * Solon 定时任务实现流程提醒处理类适配（其实没适配，哈哈）
 *
 * <p>
 * 尊重知识产权，CV 请保留版权，爱组搭 http://aizuda.com 出品，不允许非法使用，后果自负
 * </p>
 *
 * @author noear
 * @since 1.0
 */
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
                        taskService.taskTimeout(flwTask.getId());
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