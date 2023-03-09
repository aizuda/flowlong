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
package com.flowlong.bpm.autoconfigure;

import com.flowlong.bpm.engine.*;
import com.flowlong.bpm.engine.core.FlowLongContext;
import com.flowlong.bpm.engine.scheduling.JobLock;
import com.flowlong.bpm.engine.scheduling.LocalLock;
import com.flowlong.bpm.engine.scheduling.SpringBootScheduler;
import com.flowlong.bpm.engine.scheduling.TaskReminder;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * spring boot starter 启动自动配置处理类
 *
 * <p>
 * 尊重知识产权，CV 请保留版权，爱组搭 http://aizuda.com 出品
 * </p>
 *
 * @author hubin
 * @since 1.0
 */
@Configuration
@MapperScan("com.flowlong.bpm.engine.core.mapper")
@ComponentScan(basePackages = {"com.flowlong.bpm.engine.core.service"})
@EnableConfigurationProperties(FlowLongProperties.class)
public class FlowLongAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public FlowLongContext flowLongContext(ProcessService processService, QueryService queryService,
                                           RuntimeService runtimeService, TaskService taskService,
                                           ManagerService managerService) {
        FlowLongContext flc = new FlowLongContext();
        flc.setProcessService(processService);
        flc.setQueryService(queryService);
        flc.setRuntimeService(runtimeService);
        flc.setTaskService(taskService);
        flc.setManagerService(managerService);
        return flc;
    }

    @Bean
    @ConditionalOnMissingBean
    public FlowLongEngine flowLongEngine(FlowLongContext flowLongContext) {
        return flowLongContext.build();
    }

    @Bean
    @ConditionalOnMissingBean
    public JobLock jobLock() {
        return new LocalLock();
    }

    @Bean
    @ConditionalOnBean({FlowLongContext.class, TaskReminder.class})
    @ConditionalOnMissingBean
    public SpringBootScheduler springBootScheduler(FlowLongContext flowLongContext, FlowLongProperties properties,
                                                   TaskReminder taskReminder, JobLock jobLock) {
        SpringBootScheduler scheduler = new SpringBootScheduler();
        scheduler.setContext(flowLongContext);
        scheduler.setRemindParam(properties.getRemind());
        scheduler.setTaskReminder(taskReminder);
        scheduler.setJobLock(jobLock);
        return scheduler;
    }
}
