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
package com.flowlong.bpm.engine.handler.impl;

import com.flowlong.bpm.engine.exception.FlowLongException;
import com.flowlong.bpm.engine.FlowLongInterceptor;
import com.flowlong.bpm.engine.core.FlowLongContext;
import com.flowlong.bpm.engine.core.Execution;
import com.flowlong.bpm.engine.entity.Task;
import com.flowlong.bpm.engine.handler.FlowLongHandler;
import com.flowlong.bpm.engine.model.TaskModel;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * 任务创建操作的处理器
 *
 * <p>
 * 尊重知识产权，CV 请保留版权，爱组搭 http://aizuda.com 出品
 * </p>
 *
 * @author hubin
 * @since 1.0
 */
@Slf4j
public class CreateTaskHandler implements FlowLongHandler {
    /**
     * 任务模型
     */
    private TaskModel model;

    /**
     * 调用者需要提供任务模型
     *
     * @param model 模型
     */
    public CreateTaskHandler(TaskModel model) {
        this.model = model;
    }

    /**
     * 根据任务模型、执行对象，创建下一个任务，并添加到execution对象的tasks集合中
     */
    @Override
    public void handle(FlowLongContext flowLongContext, Execution execution) {
        List<Task> tasks = execution.getEngine().taskService().createTask(model, execution);
        execution.addTasks(tasks);
        /**
         * 从服务上下文中查找任务拦截器列表，依次对task集合进行拦截处理
         */
        try {
            List<FlowLongInterceptor> interceptors = flowLongContext.getInterceptors();
            if (null != interceptors) {
                for (FlowLongInterceptor interceptor : interceptors) {
                    interceptor.intercept(flowLongContext, execution);
                }
            }
        } catch (Exception e) {
            log.error("拦截器执行失败=" + e.getMessage());
            throw new FlowLongException(e);
        }
    }
}
