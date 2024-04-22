/*
 * Copyright 2023-2025 Licensed under the AGPL License
 */
package com.aizuda.bpm.engine;

import com.aizuda.bpm.engine.core.Execution;
import com.aizuda.bpm.engine.core.FlowCreator;
import com.aizuda.bpm.engine.core.FlowLongContext;
import com.aizuda.bpm.engine.entity.FlwInstance;
import com.aizuda.bpm.engine.model.NodeModel;

import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * FlowLong流程引擎接口
 *
 * <p>
 * 尊重知识产权，不允许非法使用，后果自负
 * </p>
 *
 * @author hubin
 * @since 1.0
 */
public interface FlowLongEngine {
    /**
     * 根据Configuration对象配置实现类
     *
     * @param config 全局配置对象
     * @return FlowLongEngine 流程引擎
     */
    FlowLongEngine configure(FlowLongContext config);

    FlowLongContext getContext();

    /**
     * 获取process服务
     *
     * @return ProcessService 流程定义服务
     */
    default ProcessService processService() {
        return getContext().getProcessService();
    }

    /**
     * 获取查询服务
     *
     * @return QueryService 常用查询服务
     */
    default QueryService queryService() {
        return getContext().getQueryService();
    }

    /**
     * 获取实例服务
     *
     * @return RuntimeService 流程实例运行业务服务
     */
    default RuntimeService runtimeService() {
        return getContext().getRuntimeService();
    }

    /**
     * 获取任务服务
     *
     * @return TaskService 任务服务
     */
    default TaskService taskService() {
        return getContext().getTaskService();
    }

    /**
     * 根据流程定义ID，创建人ID，参数列表启动流程实例
     *
     * @param id          流程定义ID
     * @param flowCreator 流程实例任务创建者
     * @param args        参数列表
     * @param supplier    初始化流程实例提供者
     * @return {@link FlwInstance} 流程实例
     */
    Optional<FlwInstance> startInstanceById(Long id, FlowCreator flowCreator, Map<String, Object> args, Supplier<FlwInstance> supplier);

    default Optional<FlwInstance> startInstanceById(Long id, FlowCreator flowCreator, Map<String, Object> args) {
        return this.startInstanceById(id, flowCreator, args, null);
    }

    default Optional<FlwInstance> startInstanceById(Long id, FlowCreator flowCreator, String businessKey) {
        return this.startInstanceById(id, flowCreator, null, () -> FlwInstance.of(businessKey));
    }

    default Optional<FlwInstance> startInstanceById(Long id, FlowCreator flowCreator) {
        return this.startInstanceById(id, flowCreator, null, null);
    }

    /**
     * 根据流程名称、版本号、创建人、参数列表启动流程实例
     *
     * @param processKey  流程定义key
     * @param version     版本号
     * @param flowCreator 流程实例任务创建者
     * @param args        参数列表
     * @param supplier    初始化流程实例提供者
     * @return {@link FlwInstance} 流程实例
     */
    Optional<FlwInstance> startInstanceByProcessKey(String processKey, Integer version, FlowCreator flowCreator, Map<String, Object> args, Supplier<FlwInstance> supplier);

    default Optional<FlwInstance> startInstanceByProcessKey(String processKey, Integer version, FlowCreator flowCreator, Map<String, Object> args) {
        return this.startInstanceByProcessKey(processKey, version, flowCreator, args, null);
    }

    default Optional<FlwInstance> startInstanceByProcessKey(String processKey, Integer version, FlowCreator flowCreator, String businessKey) {
        return this.startInstanceByProcessKey(processKey, version, flowCreator, null, () -> FlwInstance.of(businessKey));
    }

    default Optional<FlwInstance> startInstanceByProcessKey(String processKey, Integer version, FlowCreator flowCreator) {
        return this.startInstanceByProcessKey(processKey, version, flowCreator, null, null);
    }

    default Optional<FlwInstance> startInstanceByProcessKey(String processKey, FlowCreator flowCreator) {
        return this.startInstanceByProcessKey(processKey, null, flowCreator);
    }

    /**
     * 重启流程实例（从当前所在节点currentNode位置开始）
     *
     * @param id          流程定义ID
     * @param currentNode 当前所在节点
     * @param execution   {@link Execution}
     */
    void restartProcessInstance(Long id, String currentNode, Execution execution);

    /**
     * 根据任务ID，创建人ID，参数列表执行任务
     *
     * @param taskId      任务ID
     * @param flowCreator 任务创建者
     * @param args        参数列表
     * @return true 成功 false 失败
     */
    boolean executeTask(Long taskId, FlowCreator flowCreator, Map<String, Object> args);

    default boolean executeTask(Long taskId, FlowCreator flowCreator) {
        return this.executeTask(taskId, flowCreator, null);
    }

    /**
     * 根据任务ID，创建人ID，参数列表执行任务，并且根据nodeName跳转到任意节点
     * <p>
     * 1、nodeName为null时，则跳转至上一步处理
     * 2、nodeName不为null时，则任意跳转，即动态创建转移
     * </p>
     *
     * @param taskId      任务ID
     * @param nodeName    跳转的节点名称
     * @param flowCreator 任务创建者
     * @param args        任务参数
     * @return true 成功 false 失败
     */
    boolean executeJumpTask(Long taskId, String nodeName, FlowCreator flowCreator, Map<String, Object> args);

    default boolean executeJumpTask(Long taskId, String nodeName, FlowCreator flowCreator) {
        return executeJumpTask(taskId, nodeName, flowCreator, null);
    }

    /**
     * 执行追加节点模型
     *
     * @param taskId      当前任务ID
     * @param nodeModel   加签节点模型
     * @param flowCreator 任务创建者
     * @param args        任务参数
     * @param beforeAfter true 前置 false 后置
     * @return true 成功 false 失败
     */
    boolean executeAppendNodeModel(Long taskId, NodeModel nodeModel, FlowCreator flowCreator, Map<String, Object> args, boolean beforeAfter);

    default boolean executeAppendNodeModel(Long taskId, NodeModel nodeModel, FlowCreator flowCreator, boolean beforeAfter) {
        return executeAppendNodeModel(taskId, nodeModel, flowCreator, null, beforeAfter);
    }
}
