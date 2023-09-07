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

import com.flowlong.bpm.engine.FlowLongEngine;
import com.flowlong.bpm.engine.assist.Assert;
import com.flowlong.bpm.engine.core.Execution;
import com.flowlong.bpm.engine.core.FlowCreator;
import com.flowlong.bpm.engine.core.FlowLongContext;
import com.flowlong.bpm.engine.entity.FlwInstance;
import com.flowlong.bpm.engine.entity.FlwTask;
import com.flowlong.bpm.engine.handler.FlowLongHandler;

import java.util.List;

/**
 * 结束流程实例的处理器
 *
 * <p>
 * 尊重知识产权，CV 请保留版权，爱组搭 http://aizuda.com 出品，不允许非法使用，后果自负
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
        engine.runtimeService().complete(flwInstance.getId());
    }
}
