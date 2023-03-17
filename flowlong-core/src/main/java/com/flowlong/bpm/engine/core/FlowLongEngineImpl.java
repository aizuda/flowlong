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
import com.flowlong.bpm.engine.assist.StringUtils;
import com.flowlong.bpm.engine.core.enums.JumpMode;
import com.flowlong.bpm.engine.entity.HisTask;
import com.flowlong.bpm.engine.entity.Instance;
import com.flowlong.bpm.engine.entity.Process;
import com.flowlong.bpm.engine.entity.Task;
import com.flowlong.bpm.engine.model.NodeModel;
import com.flowlong.bpm.engine.model.ProcessModel;
import com.flowlong.bpm.engine.model.TaskModel;
import com.flowlong.bpm.engine.model.TransitionModel;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 基本的流程引擎实现类
 *
 * <p>
 * 尊重知识产权，CV 请保留版权，爱组搭 http://aizuda.com 出品
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

    @Override
    public FlowLongContext getContext() {
        return this.flowLongContext;
    }

    /**
     * 根据流程定义ID启动流程实例
     */
    @Override
    public Instance startInstanceById(Long id) {
        return startInstanceById(id, null, null);
    }

    /**
     * 根据流程定义ID，创建人ID启动流程实例
     */
    @Override
    public Instance startInstanceById(Long id, String createBy) {
        return startInstanceById(id, createBy, null);
    }

    /**
     * 根据流程定义ID，创建人ID，参数列表启动流程实例
     */
    @Override
    public Instance startInstanceById(Long id, String createBy, Map<String, Object> args) {
        if (args == null) {
            args = new HashMap<>(0);
        }
        Process process = processService().getProcessById(id);
        processService().check(process, id);
        return startProcess(process, createBy, args);
    }

    @Override
    public Instance startInstanceByIdAndParentId(Long id, String createBy, Map<String, Object> args, Long parentId, String parentName) {
        if (args == null) {
            args = new HashMap<>(0);
        }
        Process process = processService().getProcessById(id);
        processService().check(process, id);
        return startProcess(process, createBy, args, parentId, parentName);
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
     * 根据流程名称、版本号、创建人启动流程实例
     *
     * @since 1.0
     */
    @Override
    public Instance startInstanceByName(String name, Integer version,
                                        String createBy) {
        return startInstanceByName(name, version, createBy, null);
    }

    /**
     * 根据流程名称、版本号、创建人、参数列表启动流程实例
     *
     * @since 1.0
     */
    @Override
    public Instance startInstanceByName(String name, Integer version, String createBy, Map<String, Object> args) {
        Process process = processService().getProcessByVersion(name, version);
        return this.startProcess(process, createBy, args);
    }

    protected Instance startProcess(Process process, String createBy, Map<String, Object> args) {
        return startProcess(process, createBy, args, null, null);
    }

    protected Instance startProcess(Process process, String createBy, Map<String, Object> args, Long parentId, String parentNodeName) {
        Execution execution = this.execute(process, createBy, args, parentId, parentNodeName);
        // 执行启动模型
        process.executeStartModel(flowLongContext, execution);
        return execution.getInstance();
    }

    /**
     * 根据父执行对象启动子流程实例（用于启动子流程）
     */
    @Override
    public Instance startInstanceByExecution(Execution execution) {
        Process process = execution.getProcess();
        Execution current = execute(process, execution.getCreateBy(), execution.getArgs(),
                execution.getParentInstance().getId(), execution.getParentNodeName());
        process.executeStartModel(flowLongContext, current);
        return current.getInstance();
    }

    /**
     * 创建流程实例，并返回执行对象
     *
     * @param process        流程定义
     * @param createBy       创建人
     * @param args           参数列表
     * @param parentId       父流程实例ID
     * @param parentNodeName 启动子流程的父流程节点名称
     * @return Execution
     */
    protected Execution execute(Process process, String createBy, Map<String, Object> args,
                                Long parentId, String parentNodeName) {
        Instance instance = runtimeService().createInstance(process, createBy, args, parentId, parentNodeName);
        if (log.isDebugEnabled()) {
            log.debug("创建流程实例对象:" + instance);
        }
        Execution current = new Execution(this, process, instance, args);
        current.setCreateBy(createBy);
        return current;
    }

    /**
     * 根据任务ID执行任务
     */
    @Override
    public List<Task> executeTask(Long taskId) {
        return executeTask(taskId, null);
    }

    /**
     * 根据任务ID，创建人ID执行任务
     */
    @Override
    public List<Task> executeTask(Long taskId, String createBy) {
        return executeTask(taskId, createBy, null);
    }

    /**
     * 根据任务ID，创建人ID，参数列表执行任务
     */
    @Override
    public List<Task> executeTask(Long taskId, String createBy, Map<String, Object> args) {
        //完成任务，并且构造执行对象
        Execution execution = execute(taskId, createBy, args);
        if (execution == null) {
            return Collections.emptyList();
        }
        execution.getProcess().executeNodeModel(flowLongContext, execution, execution.getTask().getTaskName());
        return execution.getTasks();
    }

    /**
     * 根据任务ID，创建人ID，参数列表执行任务，并且根据nodeName跳转到任意节点
     * 1、nodeName为null时，则驳回至上一步处理
     * 2、nodeName不为null时，则任意跳转，即动态创建转移
     */
    @Override
    public List<Task> executeAndJumpTask(Long taskId, String createBy, Map<String, Object> args, String nodeName) {
        return this.executeAndJumpTask(taskId, createBy, args, nodeName, JumpMode.all);
    }

    @Override
    public List<Task> retreatTask(Long taskId, String createBy, Map<String, Object> args, String nodeName) {
        return this.executeAndJumpTask(taskId, createBy, args, nodeName, JumpMode.retreat);
    }

    /**
     * 根据任务ID，创建人ID，参数列表执行任务，并且根据节点名称与跳转模式跳转到指定节点
     *
     * @param taskId   任务ID
     * @param createBy 创建人
     * @param args     参数
     * @param nodeName 节点名称
     * @param jumpMode 跳转模式
     * @return {@link Task} 任务列表
     */
    public List<Task> executeAndJumpTask(Long taskId, String createBy, Map<String, Object> args, String nodeName, JumpMode jumpMode) {
        Execution execution = this.execute(taskId, createBy, args);
        if (execution == null) {
            return Collections.emptyList();
        }
        ProcessModel processModel = execution.getProcess().getProcessModel();
        Assert.notNull(processModel, "当前任务未找到流程定义模型");
        if (StringUtils.isEmpty(nodeName)) {
            // 驳回当前任务
            execution.addTask(taskService().rejectTask(processModel, execution.getTask()));
        } else {
            // 委派获取历史任务信息
            switch (jumpMode) {
                case advance:
                    // todo: 暂未扩展节点前进逻辑
                    break;
                case retreat:
                    HisTask appointTask = queryService().getHistoryTaskByName(execution.getInstance().getId(), nodeName);
                    Assert.notNull(appointTask, "未在当前实例中找到对应任务历史记录");
                    break;
            }
            NodeModel nodeModel = processModel.getNode(nodeName);
            Assert.notNull(nodeModel, "根据节点名称[" + nodeName + "]无法找到节点模型");
            // 动态创建转移对象，由转移对象执行execution实例
            TransitionModel tm = new TransitionModel();
            tm.setTarget(nodeModel);
            tm.setEnabled(true);
            tm.execute(flowLongContext, execution);
        }
        return execution.getTasks();
    }

    /**
     * 根据流程实例ID，创建人ID，参数列表按照节点模型model创建新的自由任务
     */
    @Override
    public List<Task> createFreeTask(Long instanceId, String createBy, Map<String, Object> args, TaskModel model) {
        Instance instance = queryService().getInstance(instanceId);
        Assert.notNull(instance, "指定的流程实例[id=" + instanceId + "]已完成或不存在");
        instance.setLastUpdateBy(createBy);
        instance.setLastUpdateTime(DateUtils.getCurrentDate());
        Process process = processService().getProcessById(instance.getProcessId());
        Execution execution = new Execution(this, process, instance, args);
        execution.setCreateBy(createBy);
        return taskService().createTask(model, execution);
    }

    /**
     * 根据任务ID，创建人ID，参数列表完成任务，并且构造执行对象
     *
     * @param taskId   任务ID
     * @param createBy 创建人
     * @param args     参数列表
     * @return Execution
     */
    protected Execution execute(Long taskId, String createBy, Map<String, Object> args) {
        if (args == null) {
            args = new HashMap<>();
        }
        Task task = taskService().complete(taskId, createBy, args);
        if (log.isDebugEnabled()) {
            log.debug("任务[taskId=" + taskId + "]已完成");
        }
        Instance instance = queryService().getInstance(task.getInstanceId());
        Assert.notNull(instance, "指定的流程实例[id=" + task.getInstanceId() + "]已完成或不存在");
        instance.setLastUpdateBy(createBy);
        instance.setLastUpdateTime(DateUtils.getCurrentDate());
        runtimeService().updateInstance(instance);
        //协办任务完成不产生执行对象
        if (!task.major()) {
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
        execution.setCreateBy(createBy);
        execution.setTask(task);
        return execution;
    }
}
