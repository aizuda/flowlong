package com.aizuda.bpm.solon.example.config;

import com.aizuda.bpm.engine.TaskReminder;
import com.aizuda.bpm.engine.assist.DateUtils;
import com.aizuda.bpm.engine.core.FlowLongContext;
import com.aizuda.bpm.engine.entity.FlwTask;
import org.noear.solon.annotation.Component;
import org.noear.solon.scheduling.annotation.EnableScheduling;

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

        // 一天后继续提醒，直到用户处理完
        return DateUtils.toDate(DateUtils.now().plusDays(1));
    }
}
