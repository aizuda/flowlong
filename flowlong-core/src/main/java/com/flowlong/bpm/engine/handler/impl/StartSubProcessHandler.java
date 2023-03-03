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
import com.flowlong.bpm.engine.core.FlowLongContext;
import com.flowlong.bpm.engine.entity.Instance;
import com.flowlong.bpm.engine.entity.Process;
import com.flowlong.bpm.engine.exception.FlowLongException;
import com.flowlong.bpm.engine.handler.FlowLongHandler;
import com.flowlong.bpm.engine.model.SubProcessModel;

import java.util.concurrent.*;

/**
 * 启动子流程的处理器
 *
 * <p>
 * 尊重知识产权，CV 请保留版权，爱组搭 http://aizuda.com 出品
 * </p>
 *
 * @author hubin
 * @since 1.0
 */
public class StartSubProcessHandler implements FlowLongHandler {
    private SubProcessModel model;
    /**
     * 是否以future方式执行启动子流程任务
     */
    private boolean isFutureRunning = false;

    public StartSubProcessHandler(SubProcessModel model) {
        this.model = model;
    }

    public StartSubProcessHandler(SubProcessModel model, boolean isFutureRunning) {
        this.model = model;
        this.isFutureRunning = isFutureRunning;
    }

    /**
     * 子流程执行的处理
     */
    @Override
    public void handle(FlowLongContext flowLongContext, Execution execution) {
        //根据子流程模型名称获取子流程定义对象
        FlowLongEngine engine = execution.getEngine();
        Process process = engine.processService().getProcessByVersion(model.getProcessName(), model.getVersion());

        Execution child = execution.createSubExecution(execution, process, model.getName());
        Instance instance;
        if (isFutureRunning) {
            //创建单个线程执行器来执行启动子流程的任务
            ExecutorService es = Executors.newSingleThreadExecutor();
            //提交执行任务，并返回future
            Future<Instance> future = es.submit(new ExecuteTask(execution, process, model.getName()));
            try {
                es.shutdown();
                instance = future.get();
            } catch (InterruptedException e) {
                throw new FlowLongException("创建子流程线程被强制终止执行", e.getCause());
            } catch (ExecutionException e) {
                throw new FlowLongException("创建子流程线程执行异常.", e.getCause());
            }
        } else {
            instance = engine.startInstanceByExecution(child);
        }
        Assert.notNull(instance, "子流程创建失败");
        execution.addTasks(engine.queryService().getActiveTasksByInstanceId(instance.getId()));
    }

    /**
     * Future模式的任务执行
     * <p>
     * 通过call返回任务结果集
     */
    class ExecuteTask implements Callable<Instance> {
        private FlowLongEngine engine;
        private Execution child;

        /**
         * 构造函数
         *
         * @param execution
         * @param process
         * @param parentNodeName
         */
        public ExecuteTask(Execution execution, Process process, String parentNodeName) {
            this.engine = execution.getEngine();
            child = execution.createSubExecution(execution, process, parentNodeName);
        }

        @Override
        public Instance call() throws Exception {
            return engine.startInstanceByExecution(child);
        }
    }
}
