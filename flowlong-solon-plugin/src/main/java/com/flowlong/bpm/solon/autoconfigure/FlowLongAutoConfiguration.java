package com.flowlong.bpm.solon.autoconfigure;


import com.flowlong.bpm.engine.*;
import com.flowlong.bpm.engine.core.FlowLongContext;
import com.flowlong.bpm.engine.scheduling.JobLock;
import com.flowlong.bpm.engine.scheduling.LocalLock;
import com.flowlong.bpm.engine.scheduling.TaskReminder;
import com.flowlong.bpm.solon.adaptive.SolonFlowJsonHandler;
import org.noear.solon.annotation.Bean;
import org.noear.solon.annotation.Condition;
import org.noear.solon.annotation.Configuration;

/**
 * spring boot starter 启动自动配置处理类
 *
 * <p>
 * 尊重知识产权，CV 请保留版权，爱组搭 http://aizuda.com 出品，不允许非法使用，后果自负
 * </p>
 *
 * @author hubin
 * @since 1.0
 */
@Configuration
//@MapperScan("com.flowlong.bpm.mybatisplus.mapper")
//@ComponentScan(basePackages = {"com.flowlong.bpm.mybatisplus.service"})
//@EnableConfigurationProperties(FlowLongProperties.class)
public class FlowLongAutoConfiguration {

    @Bean
    @Condition(onMissingBean = FlowLongContext.class)
    public FlowLongContext flowLongContext(ProcessService processService, QueryService queryService,
                                           RuntimeService runtimeService, TaskService taskService) {
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

//    @Bean
//    @Condition(onMissingBean = SpringBootScheduler.class)
//    public SpringBootScheduler springBootScheduler(FlowLongContext flowLongContext, FlowLongProperties properties,
//                                                   TaskReminder taskReminder, JobLock jobLock) {
//        if(flowLongContext == null || taskReminder == null){
//            return null;
//        }
//
//        SpringBootScheduler scheduler = new SpringBootScheduler();
//        scheduler.setContext(flowLongContext);
//        scheduler.setRemindParam(properties.getRemind());
//        scheduler.setTaskReminder(taskReminder);
//        scheduler.setJobLock(jobLock);
//        return scheduler;
//    }
}
