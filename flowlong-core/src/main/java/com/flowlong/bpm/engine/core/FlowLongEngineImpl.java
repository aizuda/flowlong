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
import com.flowlong.bpm.engine.assist.ObjectUtils;
import com.flowlong.bpm.engine.core.enums.PerformType;
import com.flowlong.bpm.engine.entity.*;
import com.flowlong.bpm.engine.handler.impl.CreateTaskHandler;
import com.flowlong.bpm.engine.model.NodeAssignee;
import com.flowlong.bpm.engine.model.NodeModel;
import com.flowlong.bpm.engine.model.ProcessModel;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.function.Consumer;

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
    public Optional<FlwInstance> startInstanceById(Long id, FlowCreator flowCreator, Map<String, Object> args) {
        FlwProcess process = processService().getProcessById(id);
        if (null == process) {
            return Optional.empty();
        }
        return this.startProcess(process.checkState(), flowCreator, args);
    }

    /**
     * 根据流程名称、版本号、创建人、参数列表启动流程实例
     */
    @Override
    public Optional<FlwInstance> startInstanceByName(String name, Integer version, FlowCreator flowCreator, Map<String, Object> args) {
        FlwProcess process = processService().getProcessByVersion(name, version);
        return this.startProcess(process, flowCreator, args);
    }

    /**
     * 启动流程实例
     */
    protected Optional<FlwInstance> startProcess(FlwProcess process, FlowCreator flowCreator, Map<String, Object> args) {
        Execution execution = this.execute(process, flowCreator, args);
        // 执行启动模型
        process.executeStartModel(flowLongContext, execution);
        return Optional.ofNullable(execution.getFlwInstance());
    }


    /**
     * 创建流程实例，并返回执行对象
     *
     * @param process     流程定义
     * @param flowCreator 流程实例任务创建者
     * @param args        参数列表
     * @return Execution
     */
    protected Execution execute(FlwProcess process, FlowCreator flowCreator, Map<String, Object> args) {
        FlwInstance flwInstance = runtimeService().createInstance(process, flowCreator, args);
        if (log.isDebugEnabled()) {
            log.debug("创建流程实例对象:" + flwInstance);
        }
        Execution current = new Execution(this, process, flwInstance, args);
        current.setCreateId(flowCreator.getCreateId());
        current.setCreateBy(flowCreator.getCreateBy());
        return current;
    }

    /**
     * 根据任务ID，创建人ID，参数列表执行任务
     */
    @Override
    public void executeTask(Long taskId, FlowCreator flowCreator, Map<String, Object> args) {
        // 完成任务，并且构造执行对象
        this.execute(taskId, flowCreator, args, execution -> {
            // 执行节点模型
            execution.getProcess().executeNodeModel(flowLongContext, execution, execution.getFlwTask().getTaskName());
        });
    }

    /**
     * 执行任务并跳转到指定节点
     */
    public void executeAndJumpTask(Long taskId, String nodeName, FlowCreator flowCreator, Map<String, Object> args) {
        // 执行当前任务
        this.execute(taskId, flowCreator, args, execution -> {
            ProcessModel processModel = execution.getProcess().getProcessModel();
            Assert.notNull(processModel, "当前任务未找到流程定义模型");

            // 查找模型节点
            NodeModel nodeModel = processModel.getNode(nodeName);
            Assert.notNull(nodeModel, "根据节点名称[" + nodeName + "]无法找到节点模型");

            // 创建当前节点任务
            nodeModel.createTask(flowLongContext, execution);
        });
    }

    /**
     * 根据任务ID，创建人ID，参数列表完成任务，并且构造执行对象
     */
    protected void execute(Long taskId, FlowCreator flowCreator, Map<String, Object> args, Consumer<Execution> executeNextStep) {
        if (args == null) {
            args = new HashMap<>();
        }
        FlwTask flwTask = taskService().complete(taskId, flowCreator, args);
        if (log.isDebugEnabled()) {
            log.debug("任务[taskId=" + taskId + "]已完成");
        }
        FlwInstance flwInstance = queryService().getInstance(flwTask.getInstanceId());
        Assert.notNull(flwInstance, "指定的流程实例[id=" + flwTask.getInstanceId() + "]已完成或不存在");
        flwInstance.setLastUpdateBy(flowCreator.getCreateId());
        flwInstance.setLastUpdateTime(DateUtils.getCurrentDate());
        runtimeService().updateInstance(flwInstance);

        PerformType performType = PerformType.get(flwTask.getPerformType());
        if (performType == PerformType.countersign) {
            /**
             * 会签未全部完成，不继续执行节点模型
             */
            List<FlwTask> flwTaskList = queryService().getTasksByInstanceIdAndTaskName(flwInstance.getId(), flwTask.getTaskName());
            if (ObjectUtils.isNotEmpty(flwTaskList)) {
                return;
            }
        }

        // 流程模型
        FlwProcess process = processService().getProcessById(flwInstance.getProcessId());
        Map<String, Object> instanceMaps = flwInstance.getVariableMap();
        if (instanceMaps != null) {
            for (Map.Entry<String, Object> entry : instanceMaps.entrySet()) {
                if (args.containsKey(entry.getKey())) {
                    continue;
                }
                args.put(entry.getKey(), entry.getValue());
            }
        }
        Execution execution = new Execution(this, process, flwInstance, args);
        execution.setCreateId(flowCreator.getCreateId());
        execution.setCreateBy(flowCreator.getCreateBy());
        execution.setFlwTask(flwTask);

        /**
         * 按顺序依次审批，一个任务按顺序多个参与者依次添加
         */
        if (performType == PerformType.sort) {
            NodeModel nodeModel = process.getProcessModel().getNode(flwTask.getTaskName());
            List<NodeAssignee> nodeUserList = nodeModel.getNodeUserList();
            NodeAssignee nextNodeAssignee = null;
            boolean findTaskActor = false;
            for (NodeAssignee nodeAssignee : nodeUserList) {
                if (findTaskActor) {
                    // 找到下一个执行人
                    nextNodeAssignee = nodeAssignee;
                    break;
                }
                if (Objects.equals(nodeAssignee.getId(), flowCreator.getCreateId())) {
                    findTaskActor = true;
                }
            }
            // 如果下一个顺序执行人存在，创建顺序审批任务
            if (null != nextNodeAssignee) {
                execution.setNextFlwTaskActor(FlwTaskActor.ofUser(nextNodeAssignee.getId(), nextNodeAssignee.getName()));
                new CreateTaskHandler(nodeModel).handle(flowLongContext, execution);
                return;
            }
        }

        // 执行回调逻辑
        executeNextStep.accept(execution);
    }
}
