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
package com.flowlong.bpm.engine.core;

import com.flowlong.bpm.engine.FlowLongEngine;
import com.flowlong.bpm.engine.assist.Assert;
import com.flowlong.bpm.engine.assist.DateUtils;
import com.flowlong.bpm.engine.entity.Instance;
import com.flowlong.bpm.engine.entity.Process;
import com.flowlong.bpm.engine.entity.Task;
import com.flowlong.bpm.engine.model.NodeModel;
import com.flowlong.bpm.engine.model.ProcessModel;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 基本的流程引擎实现类
 *
 * <p>
 * 尊重知识产权，CV 请保留版权，爱组搭 http://aizuda.com 出品，不允许非法使用，后果自负
 * </p>
 *
 * @author hubin
 * @since 1.0
 */
@Slf4j
public class FlowLongEngineImpl implements FlowLongEngine {
    /**
     * 配置对象
     */
    protected FlowLongContext flowLongContext;

    @Override
    public FlowLongEngine configure(FlowLongContext flowLongContext) {
        this.flowLongContext = flowLongContext;
        return this;
    }

    @Override
    public FlowLongContext getContext() {
        return this.flowLongContext;
    }

    /**
     * 根据流程定义ID，创建人ID，参数列表启动流程实例
     */
    @Override
    public Optional<Instance> startInstanceById(Long id, FlowCreator flowCreator, Map<String, Object> args) {
        Process process = processService().getProcessById(id);
        if (null == process) {
            return Optional.empty();
        }
        return this.startProcess(process.checkState(), flowCreator, args);
    }

    /**
     * 根据流程名称、版本号、创建人、参数列表启动流程实例
     */
    @Override
    public Optional<Instance> startInstanceByName(String name, Integer version, FlowCreator flowCreator, Map<String, Object> args) {
        Process process = processService().getProcessByVersion(name, version);
        return this.startProcess(process, flowCreator, args);
    }

    /**
     * 启动流程实例
     */
    protected Optional<Instance> startProcess(Process process, FlowCreator flowCreator, Map<String, Object> args) {
        Execution execution = this.execute(process, flowCreator, args);
        // 执行启动模型
        process.executeStartModel(flowLongContext, execution);
        return Optional.ofNullable(execution.getInstance());
    }


    /**
     * 创建流程实例，并返回执行对象
     *
     * @param process     流程定义
     * @param flowCreator 流程实例任务创建者
     * @param args        参数列表
     * @return Execution
     */
    protected Execution execute(Process process, FlowCreator flowCreator, Map<String, Object> args) {
        Instance instance = runtimeService().createInstance(process, flowCreator, args);
        if (log.isDebugEnabled()) {
            log.debug("创建流程实例对象:" + instance);
        }
        Execution current = new Execution(this, process, instance, args);
        current.setCreateId(flowCreator.getCreateId());
        current.setCreateBy(flowCreator.getCreateBy());
        return current;
    }

    /**
     * 根据任务ID，创建人ID，参数列表执行任务
     */
    @Override
    public Optional<List<Task>> executeTask(Long taskId, FlowCreator flowCreator, Map<String, Object> args) {
        //完成任务，并且构造执行对象
        Execution execution = this.execute(taskId, flowCreator, args);
        if (execution == null) {
            return Optional.empty();
        }
        execution.getProcess().executeNodeModel(flowLongContext, execution, execution.getTask().getTaskName());
        return Optional.of(execution.getTasks());
    }

    /**
     * 执行任务并跳转到指定节点
     */
    public Optional<List<Task>> executeAndJumpTask(Long taskId, String nodeName, FlowCreator flowCreator, Map<String, Object> args) {
        // 执行当前任务
        Execution execution = this.execute(taskId, flowCreator, args);
        if (null == execution) {
            return Optional.empty();
        }
        ProcessModel processModel = execution.getProcess().getProcessModel();
        Assert.notNull(processModel, "当前任务未找到流程定义模型");

        // 查找模型节点
        NodeModel nodeModel = processModel.getNode(nodeName);
        Assert.notNull(nodeModel, "根据节点名称[" + nodeName + "]无法找到节点模型");

        // 创建当前节点任务
        nodeModel.createTask(flowLongContext, execution);
        return Optional.of(execution.getTasks());
    }

    /**
     * 根据任务ID，创建人ID，参数列表完成任务，并且构造执行对象
     */
    protected Execution execute(Long taskId, FlowCreator flowCreator, Map<String, Object> args) {
        if (args == null) {
            args = new HashMap<>();
        }
        Task task = taskService().complete(taskId, flowCreator, args);
        if (log.isDebugEnabled()) {
            log.debug("任务[taskId=" + taskId + "]已完成");
        }
        Instance instance = queryService().getInstance(task.getInstanceId());
        Assert.notNull(instance, "指定的流程实例[id=" + task.getInstanceId() + "]已完成或不存在");
        instance.setLastUpdateBy(flowCreator.getCreateId());
        instance.setLastUpdateTime(DateUtils.getCurrentDate());
        runtimeService().updateInstance(instance);
        //协办任务完成不产生执行对象
//        if (!task.major()) {
//            return null;
//        }
        Map<String, Object> instanceMaps = instance.getVariableMap();
        if (instanceMaps != null) {
            for (Map.Entry<String, Object> entry : instanceMaps.entrySet()) {
                if (args.containsKey(entry.getKey())) {
                    continue;
                }
                args.put(entry.getKey(), entry.getValue());
            }
        }
        Process process = processService().getProcessById(instance.getProcessId());
        Execution execution = new Execution(this, process, instance, args);
        execution.setCreateId(flowCreator.getCreateId());
        execution.setCreateBy(flowCreator.getCreateBy());
        execution.setTask(task);
        return execution;
    }
}
