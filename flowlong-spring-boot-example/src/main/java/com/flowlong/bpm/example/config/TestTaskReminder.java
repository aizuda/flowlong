package com.flowlong.bpm.example.config;

import com.flowlong.bpm.engine.core.FlowLongContext;
import com.flowlong.bpm.engine.scheduling.TaskReminder;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Component;

/**
 * 注入自定义任务提醒处理类
 * 注解 EnableScheduling 必须启动
 */
@Component
@EnableScheduling
public class TestTaskReminder implements TaskReminder {

    @Override
    public void remind(FlowLongContext context, String instanceId, String currentTaskId) {
        System.out.println("测试提醒：" + instanceId);
    }
}
