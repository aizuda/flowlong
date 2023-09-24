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
package com.flowlong.bpm.solon.autoconfigure;

import com.flowlong.bpm.engine.*;
import com.flowlong.bpm.engine.core.FlowLongContext;
import com.flowlong.bpm.engine.listener.InstanceListener;
import com.flowlong.bpm.engine.listener.TaskListener;
import com.flowlong.bpm.engine.scheduling.JobLock;
import com.flowlong.bpm.engine.scheduling.LocalLock;
import com.flowlong.bpm.engine.scheduling.TaskReminder;
import com.flowlong.bpm.mybatisplus.mapper.*;
import com.flowlong.bpm.mybatisplus.service.ProcessServiceImpl;
import com.flowlong.bpm.mybatisplus.service.QueryServiceImpl;
import com.flowlong.bpm.mybatisplus.service.RuntimeServiceImpl;
import com.flowlong.bpm.mybatisplus.service.TaskServiceImpl;
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
    @Condition(onMissingBean = TaskService.class)
    public TaskService taskService(@Inject(required = false) TaskAccessStrategy taskAccessStrategy, @Inject(required = false) TaskListener taskListener,
                                   FlwProcessMapper processMapper, FlwInstanceMapper instanceMapper, FlwTaskMapper taskMapper,
                                   FlwTaskCcMapper taskCcMapper, FlwTaskActorMapper taskActorMapper, FlwHisTaskMapper hisTaskMapper,
                                   FlwHisTaskActorMapper hisTaskActorMapper) {
        return new TaskServiceImpl(taskAccessStrategy, taskListener, processMapper, instanceMapper, taskMapper,
                taskCcMapper, taskActorMapper, hisTaskMapper, hisTaskActorMapper);
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
    public RuntimeService runtimeService(@Inject(required = false) InstanceListener instanceListener,
                                         QueryService queryService, TaskService taskService, FlwInstanceMapper instanceMapper,
                                         FlwHisInstanceMapper hisInstanceMapper) {
        return new RuntimeServiceImpl(instanceListener, queryService, taskService, instanceMapper, hisInstanceMapper);
    }

    @Bean
    @Condition(onMissingBean = ProcessService.class)
    public ProcessService processService(RuntimeService runtimeService, FlwProcessMapper processMapper) {
        return new ProcessServiceImpl(runtimeService, processMapper);
    }

    @Bean
    @Condition(onMissingBean = FlowLongContext.class)
    public FlowLongContext flowLongContext(ProcessService processService,
                                           QueryService queryService,
                                           RuntimeService runtimeService,
                                           TaskService taskService) {

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
