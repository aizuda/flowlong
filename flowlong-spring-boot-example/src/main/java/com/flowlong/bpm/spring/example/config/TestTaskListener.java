package com.flowlong.bpm.spring.example.config;

import com.flowlong.bpm.engine.core.FlowCreator;
import com.flowlong.bpm.engine.core.enums.EventType;
import com.flowlong.bpm.engine.entity.FlwTask;
import com.flowlong.bpm.engine.listener.TaskListener;

import java.util.function.Supplier;

/**
 * 同步监听任务事件，需要注入该监听器有效
 * <p>
 * 不可以 EventTaskListener 同时使用
 * </p>
 */
public class TestTaskListener implements TaskListener {

    @Override
    public boolean notify(EventType eventType, Supplier<FlwTask> supplier, FlowCreator flowCreator) {
        System.err.println("当前执行任务 = " + supplier.get().getTaskName() +
                " ，执行事件 = " + eventType.name() + "，创建人=" + flowCreator.getCreateBy());
        return true;
    }

}
