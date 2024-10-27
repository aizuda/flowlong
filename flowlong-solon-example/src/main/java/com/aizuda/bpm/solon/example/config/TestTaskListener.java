package com.aizuda.bpm.solon.example.config;

import com.aizuda.bpm.engine.core.FlowCreator;
import com.aizuda.bpm.engine.core.enums.TaskEventType;
import com.aizuda.bpm.engine.entity.FlwTask;
import com.aizuda.bpm.engine.listener.TaskListener;
import com.aizuda.bpm.engine.model.NodeModel;
import org.noear.solon.annotation.Component;

import java.util.function.Supplier;

/**
 * 同步监听任务事件，需要注入该监听器有效
 * <p>
 * 不可以 EventTaskListener 同时使用
 * </p>
 */
@Component
public class TestTaskListener implements TaskListener {

    @Override
    public boolean notify(TaskEventType eventType, Supplier<FlwTask> supplier, NodeModel nodeModel, FlowCreator flowCreator) {
        System.err.println("当前执行任务 = " + supplier.get().getTaskName() +
                " ，执行事件 = " + eventType.name() + "，创建人=" + flowCreator.getCreateBy());
        return true;
    }

}
