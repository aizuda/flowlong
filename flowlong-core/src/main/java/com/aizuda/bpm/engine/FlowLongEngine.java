/*
 * Copyright 2023-2025 Licensed under the Dual Licensing
 * website: https://aizuda.com
 */
package com.aizuda.bpm.engine;

import com.aizuda.bpm.engine.core.Execution;
import com.aizuda.bpm.engine.core.FlowCreator;
import com.aizuda.bpm.engine.core.FlowLongContext;
import com.aizuda.bpm.engine.core.enums.PerformType;
import com.aizuda.bpm.engine.core.enums.TaskType;
import com.aizuda.bpm.engine.entity.FlwInstance;
import com.aizuda.bpm.engine.entity.FlwProcess;
import com.aizuda.bpm.engine.entity.FlwTask;
import com.aizuda.bpm.engine.entity.FlwTaskActor;
import com.aizuda.bpm.engine.model.NodeAssignee;
import com.aizuda.bpm.engine.model.NodeModel;
import com.aizuda.bpm.engine.model.ProcessModel;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * FlowLong流程引擎接口
 *
 * <p>
 * <a href="https://aizuda.com">官网</a>尊重知识产权，不允许非法使用，后果自负
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
     * @param saveAsDraft 暂存草稿
     * @param supplier    初始化流程实例提供者
     * @return {@link FlwInstance} 流程实例
     */
    Optional<FlwInstance> startInstanceById(Long id, FlowCreator flowCreator, Map<String, Object> args, boolean saveAsDraft, Supplier<FlwInstance> supplier);

    default Optional<FlwInstance> startInstanceById(Long id, FlowCreator flowCreator, Map<String, Object> args, boolean saveAsDraft) {
        return this.startInstanceById(id, flowCreator, args, saveAsDraft, null);
    }

    default Optional<FlwInstance> startInstanceById(Long id, FlowCreator flowCreator, Map<String, Object> args) {
        return this.startInstanceById(id, flowCreator, args, false);
    }

    default Optional<FlwInstance> startInstanceById(Long id, FlowCreator flowCreator, String businessKey, boolean saveAsDraft) {
        return this.startInstanceById(id, flowCreator, null, saveAsDraft, () -> FlwInstance.of(businessKey));
    }

    default Optional<FlwInstance> startInstanceById(Long id, FlowCreator flowCreator, String businessKey) {
        return this.startInstanceById(id, flowCreator, businessKey, false);
    }

    default Optional<FlwInstance> startInstanceById(Long id, FlowCreator flowCreator, boolean saveAsDraft) {
        return this.startInstanceById(id, flowCreator, null, saveAsDraft, null);
    }

    default Optional<FlwInstance> startInstanceById(Long id, FlowCreator flowCreator) {
        return this.startInstanceById(id, flowCreator, false);
    }

    /**
     * 根据流程名称、版本号、创建人、参数列表启动流程实例
     *
     * @param processKey  流程定义key
     * @param version     版本号
     * @param flowCreator 流程实例任务创建者
     * @param args        参数列表
     * @param saveAsDraft 暂存草稿
     * @param supplier    初始化流程实例提供者
     * @return {@link FlwInstance} 流程实例
     */
    Optional<FlwInstance> startInstanceByProcessKey(String processKey, Integer version, FlowCreator flowCreator, Map<String, Object> args, boolean saveAsDraft, Supplier<FlwInstance> supplier);

    default Optional<FlwInstance> startInstanceByProcessKey(String processKey, Integer version, FlowCreator flowCreator, Map<String, Object> args, boolean saveAsDraft) {
        return this.startInstanceByProcessKey(processKey, version, flowCreator, args, saveAsDraft, null);
    }

    default Optional<FlwInstance> startInstanceByProcessKey(String processKey, Integer version, FlowCreator flowCreator, Map<String, Object> args) {
        return this.startInstanceByProcessKey(processKey, version, flowCreator, args, false);
    }

    default Optional<FlwInstance> startInstanceByProcessKey(String processKey, Integer version, FlowCreator flowCreator, String businessKey, boolean saveAsDraft) {
        return this.startInstanceByProcessKey(processKey, version, flowCreator, null, saveAsDraft, () -> FlwInstance.of(businessKey));
    }

    default Optional<FlwInstance> startInstanceByProcessKey(String processKey, Integer version, FlowCreator flowCreator, String businessKey) {
        return this.startInstanceByProcessKey(processKey, version, flowCreator, businessKey, false);
    }

    default Optional<FlwInstance> startInstanceByProcessKey(String processKey, Integer version, FlowCreator flowCreator, boolean saveAsDraft) {
        return this.startInstanceByProcessKey(processKey, version, flowCreator, null, saveAsDraft, null);
    }

    default Optional<FlwInstance> startInstanceByProcessKey(String processKey, Integer version, FlowCreator flowCreator) {
        return this.startInstanceByProcessKey(processKey, version, flowCreator, false);
    }

    default Optional<FlwInstance> startInstanceByProcessKey(String processKey, FlowCreator flowCreator, boolean saveAsDraft) {
        return this.startInstanceByProcessKey(processKey, null, flowCreator, saveAsDraft);
    }

    default Optional<FlwInstance> startInstanceByProcessKey(String processKey, FlowCreator flowCreator) {
        return this.startInstanceByProcessKey(processKey, flowCreator, false);
    }

    /**
     * 根据流程对象启动流程实例
     *
     * @param process     {@link FlwProcess}
     * @param flowCreator 流程实例任务创建者
     * @param args        参数列表
     * @param saveAsDraft 暂存草稿
     * @param supplier    初始化流程实例提供者
     * @return {@link FlwInstance} 流程实例
     */
    Optional<FlwInstance> startProcessInstance(FlwProcess process, FlowCreator flowCreator, Map<String, Object> args, boolean saveAsDraft, Supplier<FlwInstance> supplier);

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
     * 自动跳转任务
     *
     * @param taskId      任务ID
     * @param args        任务参数
     * @param flowCreator 任务创建者
     * @return true 成功 false 失败
     */
    boolean autoJumpTask(Long taskId, Map<String, Object> args, FlowCreator flowCreator);

    default boolean autoJumpTask(Long taskId, FlowCreator flowCreator) {
        return this.autoJumpTask(taskId, null, flowCreator);
    }

    /**
     * 自动完成任务
     *
     * @param taskId      任务ID
     * @param args        任务参数
     * @param flowCreator 任务创建者
     * @return true 成功 false 失败
     */
    boolean autoCompleteTask(Long taskId, Map<String, Object> args, FlowCreator flowCreator);

    default boolean autoCompleteTask(Long taskId, FlowCreator flowCreator) {
        return this.autoCompleteTask(taskId, null, flowCreator);
    }

    default boolean autoCompleteTask(Long taskId) {
        return this.autoCompleteTask(taskId, FlowCreator.ADMIN);
    }

    /**
     * 自动拒绝任务
     *
     * @param flwTask     任务对象
     * @param args        任务参数
     * @param flowCreator 任务创建者
     * @return true 成功 false 失败
     */
    boolean autoRejectTask(FlwTask flwTask, Map<String, Object> args, FlowCreator flowCreator);

    default boolean autoRejectTask(FlwTask flwTask, FlowCreator flowCreator) {
        return this.autoRejectTask(flwTask, null, flowCreator);
    }

    default boolean autoRejectTask(FlwTask flwTask) {
        return this.autoRejectTask(flwTask, FlowCreator.ADMIN);
    }


    /**
     * 根据任务ID，创建人ID，参数列表执行任务，并且根据nodeName跳转到任意节点
     * <p>
     * 1、nodeName为null时，则跳转至上一步处理
     * 2、nodeName不为null时，则任意跳转，即动态创建转移
     * </p>
     *
     * @param taskId      任务ID
     * @param nodeKey     跳转的节点key
     * @param flowCreator 任务创建者
     * @param args        任务参数
     * @return true 成功 false 失败
     */
    default boolean executeJumpTask(Long taskId, String nodeKey, FlowCreator flowCreator, Map<String, Object> args) {
        // 执行任务跳转归档
        return this.executeJumpTask(taskId, nodeKey, flowCreator, args, TaskType.jump).isPresent();
    }

    default boolean executeJumpTask(Long taskId, String nodeKey, FlowCreator flowCreator) {
        return executeJumpTask(taskId, nodeKey, flowCreator, null);
    }

    Optional<FlwTask> executeJumpTask(Long taskId, String nodeKey, FlowCreator flowCreator, Map<String, Object> args, TaskType taskTye);

    /**
     * 根据当前任务对象驳回至指定 nodeKey 节点，如果 nodeKey 为空默认为上一步处理
     *
     * @param currentFlwTask 当前任务对象
     * @param nodeKey        跳转的节点key
     * @param flowCreator    任务创建者
     * @param args           任务参数
     * @param termination    是否终止流程，该参数为 true 时，其它驳回策略无效
     * @return Task 任务对象
     */
    Optional<FlwTask> executeRejectTask(FlwTask currentFlwTask, String nodeKey, FlowCreator flowCreator, Map<String, Object> args, boolean termination);

    default Optional<FlwTask> executeRejectTask(FlwTask currentFlwTask, String nodeKey, FlowCreator flowCreator, Map<String, Object> args) {
        return executeRejectTask(currentFlwTask, nodeKey, flowCreator, args, false);
    }

    default Optional<FlwTask> executeRejectTask(FlwTask currentFlwTask, FlowCreator flowCreator, Map<String, Object> args) {
        return executeRejectTask(currentFlwTask, null, flowCreator, args, false);
    }

    /**
     * 根据已有任务、参与者创建新的任务
     * <p>
     * 适用于动态转派，动态协办等处理且流程图中不体现节点情况
     * </p>
     *
     * @param taskId      主办任务ID
     * @param taskActors  参与者集合
     * @param taskType    任务类型
     * @param performType 参与类型
     * @param flowCreator 任务创建者
     * @param args        任务参数
     * @return 创建任务集合
     */
    List<FlwTask> createNewTask(Long taskId, TaskType taskType, PerformType performType, List<FlwTaskActor> taskActors,
                                FlowCreator flowCreator, Map<String, Object> args);

    /**
     * 创建抄送任务
     * <p>默认不校验是否重复抄送</p>
     *
     * @param taskModel   任务模型
     * @param ccUserList  抄送任务分配到任务的人或角色列表
     * @param flwTask     当前任务
     * @param flowCreator 任务创建者
     */
    boolean createCcTask(NodeModel taskModel, FlwTask flwTask, List<NodeAssignee> ccUserList, FlowCreator flowCreator);

    /**
     * 创建抄送任务
     */
    default boolean createCcTask(FlwTask flwTask, List<NodeAssignee> ccUserList, FlowCreator flowCreator) {
        ProcessModel processModel = runtimeService().getProcessModelByInstanceId(flwTask.getInstanceId());
        return this.createCcTask(processModel.getNode(flwTask.getTaskKey()), flwTask, ccUserList, flowCreator);
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

    /**
     * 执行追加节点模型
     */
    default boolean executeAppendNodeModel(Long taskId, NodeModel nodeModel, FlowCreator flowCreator, boolean beforeAfter) {
        return executeAppendNodeModel(taskId, nodeModel, flowCreator, null, beforeAfter);
    }
}
