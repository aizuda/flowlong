/*
 * Copyright 2023-2025 Licensed under the Dual Licensing
 * website: https://aizuda.com
 */
package com.aizuda.bpm.engine.core;

import com.aizuda.bpm.engine.FlowConstants;
import com.aizuda.bpm.engine.FlowLongEngine;
import com.aizuda.bpm.engine.TaskActorProvider;
import com.aizuda.bpm.engine.assist.Assert;
import com.aizuda.bpm.engine.core.enums.InstanceState;
import com.aizuda.bpm.engine.core.enums.PerformType;
import com.aizuda.bpm.engine.core.enums.TaskEventType;
import com.aizuda.bpm.engine.core.enums.TaskState;
import com.aizuda.bpm.engine.entity.FlwInstance;
import com.aizuda.bpm.engine.entity.FlwTask;
import com.aizuda.bpm.engine.entity.FlwTaskActor;
import com.aizuda.bpm.engine.model.ModelHelper;
import com.aizuda.bpm.engine.model.NodeModel;
import com.aizuda.bpm.engine.model.ProcessModel;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 流程执行过程中所传递的执行对象，其中包含流程定义、流程模型、流程实例对象、执行参数、返回的任务列表
 *
 * <p>
 * <a href="https://aizuda.com">官网</a>尊重知识产权，不允许非法使用，后果自负
 * </p>
 *
 * @author hubin
 * @since 1.0
 */
@Getter
@Setter
public class Execution implements Serializable {
    /**
     * FlowLongEngine holder
     */
    private FlowLongEngine engine;
    /**
     * JSON BPM 模型
     */
    private ProcessModel processModel;
    /**
     * 流程实例对象
     */
    private FlwInstance flwInstance;
    /**
     * 父流程实例
     */
    private FlwInstance parentFlwInstance;
    /**
     * 动态提供审批参与者类型
     * <p>如果已经存在，避免多次获取</p>
     */
    private Integer providerTaskActorType;
    /**
     * 动态提供审批参与者列表
     * <p>如果已经存在，避免多次获取</p>
     */
    private List<FlwTaskActor> providerTaskActors;
    /**
     * 下一个审批参与者
     */
    private FlwTaskActor nextFlwTaskActor;
    /**
     * 父流程实例节点名称
     */
    private String parentNodeName;
    /**
     * 子流程实例节点名称
     */
    private Long childInstanceId;
    /**
     * 执行参数
     */
    private Map<String, Object> args;
    /**
     * 创建人
     */
    private FlowCreator flowCreator;
    /**
     * 当前执行任务
     */
    private FlwTask flwTask;
    /**
     * 返回的任务列表
     */
    private List<FlwTask> flwTasks = new ArrayList<>();
    /**
     * 是否已合并
     * 针对join节点的处理
     */
    private boolean isMerged = false;
    /**
     * 暂存草稿
     */
    private boolean saveAsDraft = false;
    /**
     * 指定任务事件类型
     */
    private TaskEventType taskEventType;

    public TaskEventType getTaskEventType() {
        return null == taskEventType ? TaskEventType.create : taskEventType;
    }

    /**
     * 用于产生子流程执行对象使用
     *
     * @param execution      {@link Execution}
     * @param processModel   {@link ProcessModel}
     * @param parentNodeName 上一节点名称
     */
    Execution(Execution execution, ProcessModel processModel, String parentNodeName) {
        if (execution == null || processModel == null || parentNodeName == null) {
            throw Assert.throwable("Failed to construct Execution object. Please check if Execution, Process, and parentNodeName are empty");
        }
        this.engine = execution.getEngine();
        this.processModel = processModel;
        this.args = execution.getArgs();
        this.parentFlwInstance = execution.getFlwInstance();
        this.parentNodeName = parentNodeName;
        this.flowCreator = execution.getFlowCreator();
    }

    /**
     * 构造函数，接收流程定义、流程实例对象、执行参数
     *
     * @param engine       {@link FlowLongEngine}
     * @param processModel {@link ProcessModel}
     * @param flowCreator  {@link FlowCreator}
     * @param flwInstance  {@link FlwInstance}
     * @param args         执行参数
     */
    public Execution(FlowLongEngine engine, ProcessModel processModel, FlowCreator flowCreator,
                     FlwInstance flwInstance, Map<String, Object> args) {
        if (processModel == null || flwInstance == null) {
            throw Assert.throwable("Failed to construct Execution object, please check if process and order are empty");
        }
        this.engine = engine;
        this.processModel = processModel;
        this.flowCreator = flowCreator;
        this.flwInstance = flwInstance;
        this.args = args;
    }

    /**
     * 构造函数，仅适用于模型条件节点查找
     */
    public Execution(FlowCreator flowCreator, Map<String, Object> args) {
        this.flowCreator = flowCreator;
        this.args = args;
    }

    /**
     * 根据当前执行对象execution、子流程定义process、当前节点名称产生子流程的执行对象
     *
     * @param execution      {@link Execution}
     * @param processModel   {@link ProcessModel}
     * @param parentNodeName 上一节点名称
     * @return {@link Execution}
     */
    public Execution createSubExecution(Execution execution, ProcessModel processModel, String parentNodeName) {
        return new Execution(execution, processModel, parentNodeName);
    }

    /**
     * 执行节点模型
     *
     * @param flowLongContext 流程引擎上下文
     * @param nodeKey         节点key
     * @return 执行节点模型结果 true 成功 false 失败
     */
    public boolean executeNodeModel(FlowLongContext flowLongContext, String nodeKey) {
        ProcessModel processModel = this.getProcessModel();
        Assert.isNull(processModel, "Process model content cannot be empty");

        // 重新加载流程模型内容
        ModelHelper.reloadProcessModel(flowLongContext, flwInstance.getId(), processModel);

        // 获取节点模型
        NodeModel nodeModel = processModel.getNode(nodeKey);
        Assert.isNull(nodeModel, "Not found in the process model, process nodeKey=" + nodeKey);

        // 获取当前任务列表，检查并行分支执行情况
        List<String> nodeKeys = new LinkedList<>();
        flowLongContext.getQueryService().getActiveTasksByInstanceId(flwTask.getInstanceId()).ifPresent(flwTasks -> {
            for (FlwTask ft : flwTasks) {
                nodeKeys.add(ft.getTaskKey());
            }
        });

        // 查找流程关联的子流程
        List<String> otherProcessKeys = new LinkedList<>();
        Optional<List<FlwInstance>> subProcessList = flowLongContext.getQueryService().getSubProcessByInstanceId(flwTask.getInstanceId());
        subProcessList.ifPresent(subProcesses -> subProcesses.forEach(process -> {
            ProcessModel otherModel = flowLongContext.getRuntimeService().getProcessModelByInstanceId(process.getId());
            otherProcessKeys.addAll(ModelHelper.getRootNodeAllChildNodes(otherModel.getNodeConfig()).stream().map(NodeModel::getNodeKey).collect(Collectors.toList()));
            flowLongContext.getQueryService().getActiveTasksByInstanceId(process.getId()).ifPresent(flwTasks -> {
                for (FlwTask ft : flwTasks) {
                    nodeKeys.add(ft.getTaskKey());
                }
            });
        }));

        Optional<NodeModel> executeNodeOptional = Optional.empty();

        // 如果有额外的流程，先判断当前的task是否在流程里面，如果不在直找下一个节点
        if (!(!otherProcessKeys.isEmpty() && !nodeKeys.isEmpty() && !Collections.disjoint(nodeKeys, otherProcessKeys))) {
            executeNodeOptional = nodeModel.nextNode(nodeKeys);
        }

        if (executeNodeOptional.isPresent()) {
            // 执行流程节点
            NodeModel executeNode = executeNodeOptional.get();
            return executeNode.execute(flowLongContext, this);
        }

        /*
         * 无执行节点流程结束，并且任务列表为空
         */
        if (nodeKeys.isEmpty()) {
            return this.endInstance(nodeModel);
        }
        return true;
    }

    /**
     * 执行结束当前流程实例
     */
    public boolean endInstance(NodeModel endNode) {
        return this.endInstance(endNode, InstanceState.complete);
    }

    /**
     * 执行结束当前流程实例
     *
     * @param endNode       结束节点
     * @param instanceState 实例状态
     * @return true 执行成功  false 执行失败
     */
    public boolean endInstance(NodeModel endNode, InstanceState instanceState) {
        if (engine.queryService().existActiveSubProcess(flwInstance.getId())) {
            /*
             * 存在执行中的子流程，不允许结束
             */
            return true;
        }

        /*
         * 执行自动完成逻辑
         */
        if (instanceState == InstanceState.autoPass || instanceState == InstanceState.autoReject) {
            List<FlwTask> flwTasks = engine.queryService().getTasksByInstanceId(flwInstance.getId());
            for (FlwTask flwTask : flwTasks) {
                Assert.illegal(flwTask.major(), "There are unfinished major tasks");
                TaskState taskState = TaskState.autoComplete;
                TaskEventType taskEventType = TaskEventType.autoComplete;
                if (instanceState == InstanceState.autoReject) {
                    taskState = TaskState.autoReject;
                    taskEventType = TaskEventType.autoReject;
                }
                engine.taskService().executeTask(flwTask.getId(), flowCreator, null, taskState, taskEventType);
            }
        }

        /*
         * 销毁流程实例模型缓存
         */
        FlowLongContext.invalidateProcessModel(FlowConstants.processInstanceCacheKey + flwInstance.getId());

        /*
         * 结束当前流程实例
         */
        return engine.runtimeService().endInstance(this, flwInstance.getId(), endNode, instanceState);
    }

    /**
     * 重启流程实例（从当前所在节点currentNode位置开始）
     *
     * @param id             流程定义ID
     * @param currentNodeKey 当前所在节点key
     */
    public void restartProcessInstance(Long id, String currentNodeKey) {
        engine.restartProcessInstance(id, currentNodeKey, this);
    }

    /**
     * 添加任务集合
     *
     * @param flwTasks 流程任务列表
     */
    public void addTasks(List<FlwTask> flwTasks) {
        this.flwTasks.addAll(flwTasks);
    }

    /**
     * 添加任务
     *
     * @param flwTask 流程任务
     */
    public void addTask(FlwTask flwTask) {
        this.flwTasks.add(flwTask);
    }

    /**
     * 非正常创建任务处理逻辑，默认抛出异常
     *
     * @param flwTask     当前任务
     * @param performType 任务参与类型 {@link PerformType}
     * @param taskActors  任务参与者
     * @param nodeModel   模型节点 {@link NodeModel}
     * @return 返回 true 不再创建任务，返回 false 解决异常补充回写 taskActors 信息
     */
    public boolean abnormal(FlwTask flwTask, PerformType performType, List<FlwTaskActor> taskActors, NodeModel nodeModel) {
        return getTaskActorProvider().abnormal(flwTask, performType, taskActors, this, nodeModel);
    }

    /**
     * 动态获取指定节点模型任务参与者类型
     *
     * @param nodeModel 节点模型
     */
    public Integer getProviderTaskActorType(NodeModel nodeModel) {
        if (null == providerTaskActorType) {
            providerTaskActorType = getTaskActorProvider().getActorType(nodeModel);
        }
        return providerTaskActorType;
    }

    /**
     * 动态获取指定节点模型任务参与者列表
     *
     * @param nodeModel 节点模型
     */
    public List<FlwTaskActor> getProviderTaskActors(NodeModel nodeModel) {
        if (null == providerTaskActors) {
            providerTaskActors = getTaskActorProvider().getTaskActors(nodeModel, this);
        }
        return providerTaskActors;
    }

    protected TaskActorProvider getTaskActorProvider() {
        return engine.getContext().getTaskActorProvider();
    }
}
