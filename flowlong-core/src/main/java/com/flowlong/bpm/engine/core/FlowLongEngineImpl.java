/* Copyright 2023-2025 www.flowlong.com
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

import com.flowlong.bpm.engine.*;
import com.flowlong.bpm.engine.assist.Assert;
import com.flowlong.bpm.engine.assist.DateUtils;
import com.flowlong.bpm.engine.assist.StringUtils;
import com.flowlong.bpm.engine.entity.Instance;
import com.flowlong.bpm.engine.entity.Process;
import com.flowlong.bpm.engine.entity.Task;
import com.flowlong.bpm.engine.model.*;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 基本的流程引擎实现类
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
//        CacheManager cacheManager = ServiceContext.find(CacheManager.class);
//        if (cacheManager == null) {
//            //默认使用内存缓存管理器
//            cacheManager = new MemoryCacheManager();
//        }
//        List<CacheManagerAware> cacheServices = ServiceContext.findList(CacheManagerAware.class);
//        for (CacheManagerAware cacheService : cacheServices) {
//            cacheService.setCacheManager(cacheManager);
//        }
        return this;
    }

    /**
     * 根据流程定义ID启动流程实例
     */
    @Override
    public Instance startInstanceById(String id) {
        return startInstanceById(id, null, null);
    }

    /**
     * 根据流程定义ID，操作人ID启动流程实例
     */
    @Override
    public Instance startInstanceById(String id, String operator) {
        return startInstanceById(id, operator, null);
    }

    /**
     * 根据流程定义ID，操作人ID，参数列表启动流程实例
     */
    @Override
    public Instance startInstanceById(String id, String operator, Map<String, Object> args) {
        if (args == null) {
            args = new HashMap<>();
        }
        Process process = processService().getProcessById(id);
        processService().check(process, id);
        return startProcess(process, operator, args);
    }

    /**
     * 根据流程名称启动流程实例
     *
     * @since 1.0
     */
    @Override
    public Instance startInstanceByName(String name) {
        return startInstanceByName(name, null, null, null);
    }

    /**
     * 根据流程名称、版本号启动流程实例
     *
     * @since 1.0
     */
    @Override
    public Instance startInstanceByName(String name, Integer version) {
        return startInstanceByName(name, version, null, null);
    }

    /**
     * 根据流程名称、版本号、操作人启动流程实例
     *
     * @since 1.0
     */
    @Override
    public Instance startInstanceByName(String name, Integer version,
                                        String operator) {
        return startInstanceByName(name, version, operator, null);
    }

    /**
     * 根据流程名称、版本号、操作人、参数列表启动流程实例
     *
     * @since 1.0
     */
    @Override
    public Instance startInstanceByName(String name, Integer version, String operator, Map<String, Object> args) {
        if (args == null) {
            args = new HashMap<>();
        }
        Process process = processService().getProcessByVersion(name, version);
        processService().check(process, name);
        return startProcess(process, operator, args);
    }

    protected Instance startProcess(Process process, String operator, Map<String, Object> args) {
        Execution execution = execute(process, operator, args, null, null);
        if (process.getModel() != null) {
            StartModel start = process.getModel().getStart();
            Assert.notNull(start, "流程定义[name=" + process.getName() + ", version=" + process.getVersion() + "]没有开始节点");
            start.execute(flowLongContext, execution);
        }

        return execution.getInstance();
    }

    /**
     * 根据父执行对象启动子流程实例（用于启动子流程）
     */
    @Override
    public Instance startInstanceByExecution(Execution execution) {
        Process process = execution.getProcess();
        StartModel start = process.getModel().getStart();
        Assert.notNull(start, "流程定义[id=" + process.getId() + "]没有开始节点");

        Execution current = execute(process, execution.getOperator(), execution.getArgs(),
                execution.getParentInstance().getId(), execution.getParentNodeName());
        start.execute(flowLongContext, current);
        return current.getInstance();
    }

    /**
     * 创建流程实例，并返回执行对象
     *
     * @param process        流程定义
     * @param operator       操作人
     * @param args           参数列表
     * @param parentId       父流程实例id
     * @param parentNodeName 启动子流程的父流程节点名称
     * @return Execution
     */
    protected Execution execute(Process process, String operator, Map<String, Object> args,
                                String parentId, String parentNodeName) {
        Instance instance = runtimeService().createInstance(process, operator, args, parentId, parentNodeName);
        if (log.isDebugEnabled()) {
            log.debug("创建流程实例对象:" + instance);
        }
        Execution current = new Execution(this, process, instance, args);
        current.setOperator(operator);
        return current;
    }

    /**
     * 根据任务主键ID执行任务
     */
    @Override
    public List<Task> executeTask(String taskId) {
        return executeTask(taskId, null);
    }

    /**
     * 根据任务主键ID，操作人ID执行任务
     */
    @Override
    public List<Task> executeTask(String taskId, String operator) {
        return executeTask(taskId, operator, null);
    }

    /**
     * 根据任务主键ID，操作人ID，参数列表执行任务
     */
    @Override
    public List<Task> executeTask(String taskId, String operator, Map<String, Object> args) {
        //完成任务，并且构造执行对象
        Execution execution = execute(taskId, operator, args);
        if (execution == null) {
            return Collections.emptyList();
        }
        ProcessModel model = execution.getProcess().getModel();
        if (model != null) {
            NodeModel nodeModel = model.getNode(execution.getTask().getTaskName());
            //将执行对象交给该任务对应的节点模型执行
            nodeModel.execute(flowLongContext, execution);
        }
        return execution.getTasks();
    }

    /**
     * 根据任务主键ID，操作人ID，参数列表执行任务，并且根据nodeName跳转到任意节点
     * 1、nodeName为null时，则驳回至上一步处理
     * 2、nodeName不为null时，则任意跳转，即动态创建转移
     */
    @Override
    public List<Task> executeAndJumpTask(String taskId, String operator, Map<String, Object> args, String nodeName) {
        Execution execution = execute(taskId, operator, args);
        if (execution == null) {
            return Collections.emptyList();
        }
        ProcessModel model = execution.getProcess().getModel();
        Assert.notNull(model, "当前任务未找到流程定义模型");
        if (StringUtils.isEmpty(nodeName)) {
            Task newTask = taskService().rejectTask(model, execution.getTask());
            execution.addTask(newTask);
        } else {
            NodeModel nodeModel = model.getNode(nodeName);
            Assert.notNull(nodeModel, "根据节点名称[" + nodeName + "]无法找到节点模型");
            //动态创建转移对象，由转移对象执行execution实例
            TransitionModel tm = new TransitionModel();
            tm.setTarget(nodeModel);
            tm.setEnabled(true);
            tm.execute(flowLongContext, execution);
        }

        return execution.getTasks();
    }

    /**
     * 根据流程实例ID，操作人ID，参数列表按照节点模型model创建新的自由任务
     */
    @Override
    public List<Task> createFreeTask(String instanceId, String operator, Map<String, Object> args, TaskModel model) {
        Instance instance = queryService().getInstance(instanceId);
        Assert.notNull(instance, "指定的流程实例[id=" + instanceId + "]已完成或不存在");
        instance.setLastUpdator(operator);
        instance.setLastUpdateTime(DateUtils.getTime());
        Process process = processService().getProcessById(instance.getProcessId());
        Execution execution = new Execution(this, process, instance, args);
        execution.setOperator(operator);
        return taskService().createTask(model, execution);
    }

    /**
     * 根据任务主键ID，操作人ID，参数列表完成任务，并且构造执行对象
     *
     * @param taskId   任务id
     * @param operator 操作人
     * @param args     参数列表
     * @return Execution
     */
    protected Execution execute(String taskId, String operator, Map<String, Object> args) {
        if (args == null) args = new HashMap<>();
        Task task = taskService().complete(taskId, operator, args);
        if (log.isDebugEnabled()) {
            log.debug("任务[taskId=" + taskId + "]已完成");
        }
        Instance instance = queryService().getInstance(task.getInstanceId());
        Assert.notNull(instance, "指定的流程实例[id=" + task.getInstanceId() + "]已完成或不存在");
        instance.setLastUpdator(operator);
        instance.setLastUpdateTime(DateUtils.getTime());
        runtimeService().updateInstance(instance);
        //协办任务完成不产生执行对象
        if (!task.isMajor()) {
            return null;
        }
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
        execution.setOperator(operator);
        execution.setTask(task);
        return execution;
    }

    @Override
    public ProcessService processService() {
        return this.flowLongContext.getProcessService();
    }

    @Override
    public QueryService queryService() {
        return this.flowLongContext.getQueryService();
    }

    @Override
    public RuntimeService runtimeService() {
        return this.flowLongContext.getRuntimeService();
    }

    @Override
    public TaskService taskService() {
        return this.flowLongContext.getTaskService();
    }

    @Override
    public ManagerService managerService() {
        return this.flowLongContext.getManagerService();
    }
}
