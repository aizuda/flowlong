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
import com.flowlong.bpm.engine.TaskService;
import com.flowlong.bpm.engine.assist.StringUtils;
import com.flowlong.bpm.engine.core.FlowLongContext;
import com.flowlong.bpm.engine.core.Execution;
import com.flowlong.bpm.engine.entity.Task;

/**
 * 委托代理拦截器
 * 负责查询wf_surrogate表获取委托代理人，并通过addTaskActor设置为参与者
 * 这里是对新创建的任务通过添加参与者进行委托代理(即授权人、代理人都可处理任务)
 * 对于运行中且未处理的待办任务，可调用engine.task().addTaskActor方法
 * {@link TaskService#addTaskActor(String, String...)}
 *
 * <p>
 * 尊重知识产权，CV 请保留版权，爱组搭 http://aizuda.com 出品
 * </p>
 *
 * @author hubin
 * @since 1.0
 */
public class SurrogateInterceptor implements FlowLongInterceptor {

    @Override
    public void intercept(FlowLongContext flowLongContext, Execution execution) {
        FlowLongEngine engine = execution.getEngine();
        for (Task task : execution.getTasks()) {
            if (task.actorIds() == null) {
                continue;
            }
            for (String actor : task.actorIds()) {
                if (StringUtils.isEmpty(actor)) {
                    continue;
                }
                String agent = engine.managerService().getSurrogate(actor, execution.getProcess().getName());
                if (StringUtils.isNotEmpty(agent) && !actor.equals(agent)) {
                    engine.taskService().addTaskActor(task.getId(), agent);
                }
            }
        }
    }
}
