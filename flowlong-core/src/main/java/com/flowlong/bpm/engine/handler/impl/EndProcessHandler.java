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
import com.flowlong.bpm.engine.core.Execution;
import com.flowlong.bpm.engine.core.FlowLongContext;
import com.flowlong.bpm.engine.entity.Instance;
import com.flowlong.bpm.engine.entity.Process;
import com.flowlong.bpm.engine.entity.Task;
import com.flowlong.bpm.engine.exception.FlowLongException;
import com.flowlong.bpm.engine.handler.FlowLongHandler;
import com.flowlong.bpm.engine.model.ProcessModel;
import com.flowlong.bpm.engine.model.SubProcessModel;

import java.util.List;

/**
 * 结束流程实例的处理器
 *
 * <p>
 * 尊重知识产权，CV 请保留版权，爱组搭 http://aizuda.com 出品
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
        Instance instance = execution.getInstance();
        List<Task> tasks = engine.queryService().getTasksByInstanceId(instance.getId());
        for (Task task : tasks) {
            if (task.major()) throw new FlowLongException("存在未完成的主办任务,请确认.");
            engine.taskService().complete(task.getId(), FlowLongEngine.AUTO);
        }
        /**
         * 结束当前流程实例
         */
        engine.runtimeService().complete(instance.getId());

        /**
         * 如果存在父流程，则重新构造Execution执行对象，交给父流程的SubProcessModel模型execute
         */
        if (null != instance.getParentId()) {
            Instance parentInstance = engine.queryService().getInstance(instance.getParentId());
            if (parentInstance == null) {
                return;
            }
            Process process = engine.processService().getProcessById(parentInstance.getProcessId());
            ProcessModel pm = process.getProcessModel();
            if (pm == null) {
                return;
            }
            SubProcessModel spm = (SubProcessModel) pm.getNode(instance.getParentNodeName());
            Execution newExecution = new Execution(engine, process, parentInstance, execution.getArgs());
            newExecution.setChildInstanceId(instance.getId());
            newExecution.setTask(execution.getTask());
            spm.execute(flowLongContext, newExecution);
            /**
             * SubProcessModel执行结果的tasks合并到当前执行对象execution的tasks列表中
             */
            execution.addTasks(newExecution.getTasks());
        }
    }
}
