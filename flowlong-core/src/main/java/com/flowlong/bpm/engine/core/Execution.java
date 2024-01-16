/*
 * Copyright 2023-2025 Licensed under the AGPL License
 */
package com.flowlong.bpm.engine.core;

import com.flowlong.bpm.engine.FlowLongEngine;
import com.flowlong.bpm.engine.TaskActorProvider;
import com.flowlong.bpm.engine.assist.Assert;
import com.flowlong.bpm.engine.entity.FlwInstance;
import com.flowlong.bpm.engine.entity.FlwProcess;
import com.flowlong.bpm.engine.entity.FlwTask;
import com.flowlong.bpm.engine.entity.FlwTaskActor;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 流程执行过程中所传递的执行对象，其中包含流程定义、流程模型、流程实例对象、执行参数、返回的任务列表
 *
 * <p>
 * 尊重知识产权，不允许非法使用，后果自负
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
     * 流程定义对象
     */
    private FlwProcess process;
    /**
     * 流程实例对象
     */
    private FlwInstance flwInstance;
    /**
     * 父流程实例
     */
    private FlwInstance parentFlwInstance;
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
     * 用于产生子流程执行对象使用
     *
     * @param execution      {@link Execution}
     * @param process        {@link FlwProcess}
     * @param parentNodeName 上一节点名称
     */
    Execution(Execution execution, FlwProcess process, String parentNodeName) {
        if (execution == null || process == null || parentNodeName == null) {
            throw Assert.throwable("构造Execution对象失败，请检查execution、process、parentNodeName是否为空");
        }
        this.engine = execution.getEngine();
        this.process = process;
        this.args = execution.getArgs();
        this.parentFlwInstance = execution.getFlwInstance();
        this.parentNodeName = parentNodeName;
        this.flowCreator = execution.getFlowCreator();
    }

    /**
     * 构造函数，接收流程定义、流程实例对象、执行参数
     *
     * @param engine      {@link FlowLongEngine}
     * @param process     {@link FlwProcess}
     * @param flowCreator {@link FlowCreator}
     * @param flwInstance {@link FlwInstance}
     * @param args        执行参数
     */
    public Execution(FlowLongEngine engine, FlwProcess process, FlowCreator flowCreator,
                     FlwInstance flwInstance, Map<String, Object> args) {
        if (process == null || flwInstance == null) {
            throw Assert.throwable("构造Execution对象失败，请检查process、order是否为空");
        }
        this.engine = engine;
        this.process = process;
        this.flowCreator = flowCreator;
        this.flwInstance = flwInstance;
        this.args = args;
    }

    /**
     * 根据当前执行对象execution、子流程定义process、当前节点名称产生子流程的执行对象
     *
     * @param execution      {@link Execution}
     * @param process        {@link FlwProcess}
     * @param parentNodeName 上一节点名称
     * @return {@link Execution}
     */
    public Execution createSubExecution(Execution execution, FlwProcess process, String parentNodeName) {
        return new Execution(execution, process, parentNodeName);
    }

    /**
     * 执行结束当前流程实例
     *
     * @return true 执行成功  false 执行失败
     */
    public boolean endInstance() {
        List<FlwTask> flwTasks = engine.queryService().getTasksByInstanceId(flwInstance.getId());
        for (FlwTask flwTask : flwTasks) {
            Assert.illegal(flwTask.major(), "存在未完成的主办任务");
            engine.taskService().complete(flwTask.getId(), this.flowCreator);
        }

        /*
         * 结束当前流程实例
         */
        return engine.runtimeService().complete(this, flwInstance.getId());
    }

    /**
     * 重启流程实例（从当前所在节点currentNode位置开始）
     *
     * @param id          流程定义ID
     * @param currentNode 当前所在节点
     */
    public void restartProcessInstance(Long id, String currentNode) {
        engine.restartProcessInstance(id, currentNode, this);
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

    public TaskActorProvider getTaskActorProvider() {
        return engine.getContext().getTaskActorProvider();
    }
}
