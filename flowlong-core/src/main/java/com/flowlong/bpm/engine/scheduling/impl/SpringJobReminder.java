package com.flowlong.bpm.engine.scheduling.impl;

import com.flowlong.bpm.engine.entity.Process;
import com.flowlong.bpm.engine.model.NodeModel;
import com.flowlong.bpm.engine.scheduling.JobReminder;

import java.util.Map;

public class SpringJobReminder implements JobReminder {

    @Override
    public void remind(Process process, String orderId, String taskId, NodeModel nodeModel, Map<String, Object> data) {

    }
}
