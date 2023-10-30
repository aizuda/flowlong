/* 
 * Copyright 2023-2025 Licensed under the AGPL License
 */
package com.flowlong.bpm.engine.impl;

import com.flowlong.bpm.engine.FlowLongEngine;
import com.flowlong.bpm.engine.FlowLongInterceptor;
import com.flowlong.bpm.engine.core.Execution;
import com.flowlong.bpm.engine.core.FlowLongContext;
import com.flowlong.bpm.engine.entity.FlwTask;

/**
 * 委托代理拦截器
 *
 * <p>
 * 尊重知识产权，不允许非法使用，后果自负
 * </p>
 *
 * @author hubin
 * @since 1.0
 */
public class SurrogateInterceptor implements FlowLongInterceptor {

    @Override
    public void handle(FlowLongContext flowLongContext, Execution execution) {
        FlowLongEngine engine = execution.getEngine();
        for (FlwTask flwTask : execution.getFlwTasks()) {
//            if (task.actorIds() == null) {
//                continue;
//            }
//            for (String actor : task.actorIds()) {
//                if (ObjectUtils.isEmpty(actor)) {
//                    continue;
//                }
//                String agent = engine.managerService().getSurrogate(actor, execution.getProcess().getName());
//                if (ObjectUtils.isNotEmpty(agent) && !actor.equals(agent)) {
//                    engine.taskService().addTaskActor(task.getId(), agent);
//                }
//            }
        }
    }
}
