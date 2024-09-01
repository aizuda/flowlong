package com.aizuda.bpm.spring.example.config;

import com.aizuda.bpm.engine.TaskReminder;
import com.aizuda.bpm.engine.core.FlowLongContext;
import com.aizuda.bpm.engine.entity.FlwTask;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * 注入自定义任务提醒处理类
 * 注解 EnableScheduling 必须启动
 */
@Component
@EnableScheduling
public class TestTaskReminder implements TaskReminder {

    @Override
    public Date remind(FlowLongContext context, Long instanceId, FlwTask currentTask) {
        System.out.println("测试提醒：" + instanceId);
        return null;
    }
}
