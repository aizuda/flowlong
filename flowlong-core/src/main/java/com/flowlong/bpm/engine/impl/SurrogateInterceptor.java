/* Copyright 2023-2025 jobob@qq.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
 * 尊重知识产权，CV 请保留版权，爱组搭 http://aizuda.com 出品，不允许非法使用，后果自负
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
