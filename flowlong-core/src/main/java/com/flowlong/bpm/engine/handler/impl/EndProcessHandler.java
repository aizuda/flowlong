/* 
 * Copyright 2023-2025 Licensed under the AGPL License
 */
package com.flowlong.bpm.engine.handler.impl;

import com.flowlong.bpm.engine.FlowLongEngine;
import com.flowlong.bpm.engine.assist.Assert;
import com.flowlong.bpm.engine.core.Execution;
import com.flowlong.bpm.engine.core.FlowCreator;
import com.flowlong.bpm.engine.core.FlowLongContext;
import com.flowlong.bpm.engine.core.enums.InstanceState;
import com.flowlong.bpm.engine.entity.FlwInstance;
import com.flowlong.bpm.engine.entity.FlwTask;
import com.flowlong.bpm.engine.handler.FlowLongHandler;

import java.util.List;

/**
 * 结束流程实例的处理器
 *
 * <p>
 * 尊重知识产权，不允许非法使用，后果自负
 * </p>
 *
 * @author hubin
 * @since 1.0
 */
public class EndProcessHandler implements FlowLongHandler {

    /**
     * 结束当前流程实例，如果存在父流程，则触发父流程继续执行
     */
    @Override
    public void handle(FlowLongContext flowLongContext, Execution execution) {
        FlowLongEngine engine = execution.getEngine();
        FlwInstance flwInstance = execution.getFlwInstance();
        List<FlwTask> flwTasks = engine.queryService().getTasksByInstanceId(flwInstance.getId());
        for (FlwTask flwTask : flwTasks) {
            Assert.illegalArgument(flwTask.major(), "存在未完成的主办任务");
            engine.taskService().complete(flwTask.getId(), FlowCreator.ADMIN);
        }
        /**
         * 结束当前流程实例
         */
        engine.runtimeService().complete(flwInstance.getId(), InstanceState.complete);
    }
}
