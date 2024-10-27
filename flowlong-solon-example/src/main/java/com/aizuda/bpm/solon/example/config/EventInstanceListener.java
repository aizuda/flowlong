package com.aizuda.bpm.solon.example.config;

import com.aizuda.bpm.engine.core.FlowCreator;
import com.aizuda.bpm.engine.core.enums.InstanceEventType;
import com.aizuda.bpm.engine.entity.FlwHisInstance;
import com.aizuda.bpm.engine.listener.InstanceListener;
import com.aizuda.bpm.engine.model.NodeModel;
import org.noear.solon.annotation.Component;

import java.util.function.Supplier;

@Component
public class EventInstanceListener implements InstanceListener {

    @Override
    public boolean notify(InstanceEventType eventType, Supplier<FlwHisInstance> supplier, NodeModel nodeModel, FlowCreator flowCreator) {
        System.out.println("eventType:" + eventType + ",supplier:" + supplier + ",nodeModel:" + nodeModel + ",flowCreator:" + flowCreator);
        return true;
    }
}