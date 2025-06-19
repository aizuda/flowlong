/*
 * Copyright 2023-2025 Licensed under the Dual Licensing
 * website: https://aizuda.com
 */
package com.aizuda.bpm.engine;

import com.aizuda.bpm.engine.core.Execution;
import com.aizuda.bpm.engine.core.FlowCreator;
import com.aizuda.bpm.engine.core.enums.InstanceState;
import com.aizuda.bpm.engine.entity.FlwInstance;
import com.aizuda.bpm.engine.entity.FlwProcess;
import com.aizuda.bpm.engine.entity.FlwTask;
import com.aizuda.bpm.engine.model.NodeModel;
import com.aizuda.bpm.engine.model.ProcessModel;

import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * 流程实例运行业务类
 *
 * <p>
 * <a href="https://aizuda.com">官网</a>尊重知识产权，不允许非法使用，后果自负
 * </p>
 *
 * @author hubin
 * @since 1.0
 */
public interface RuntimeService {

    /**
     * 根据流程、创建人员、父流程实例ID创建流程实例
     *
     * @param flwProcess  流程定义对象
     * @param flowCreator 流程实例任务创建者
     * @param args        参数列表
     * @param nodeModel   当前所在节点
     * @param saveAsDraft 暂存草稿
     * @param supplier    初始化流程实例提供者
     * @return 活动流程实例对象
     */
    FlwInstance createInstance(FlwProcess flwProcess, FlowCreator flowCreator, Map<String, Object> args, NodeModel nodeModel, boolean saveAsDraft, Supplier<FlwInstance> supplier);

    /**
     * 根据流程实例ID获取流程实例模型
     *
     * @param instanceId 流程实例ID
     * @return {@link ProcessModel}
     */
    ProcessModel getProcessModelByInstanceId(Long instanceId);

    /**
     * 通过流程实例ID节点KEY获取节点模型
     *
     * @param instanceId 流程实例ID
     * @param nodeKey 节点KEY
     */
    default NodeModel getNodeModel(Long instanceId, String nodeKey) {
        ProcessModel processModel = this.getProcessModelByInstanceId(instanceId);
        return null == processModel ? null : processModel.getNode(nodeKey);
    }

    /**
     * 根据 流程实例ID 更新流程实例全局变量
     *
     * @param instanceId 流程实例ID
     * @param args       流程实例参数
     * @param function   待更新实例回调处理函数
     */
    boolean addVariable(Long instanceId, Map<String, Object> args, Function<FlwInstance, FlwInstance> function);

    /**
     * 根据 流程实例ID 更新流程实例全局变量
     *
     * @param instanceId 流程实例ID
     * @param args       流程实例参数
     */
    default boolean addVariable(Long instanceId, Map<String, Object> args) {
        return this.addVariable(instanceId, args, t -> new FlwInstance());
    }

    /**
     * 结束流程实例（审批通过）
     *
     * @param execution     {@link Execution}
     * @param instanceId    流程实例ID
     * @param endNode       结束节点
     * @param instanceState 实例状态
     * @return true 成功 false 失败
     */
    boolean endInstance(Execution execution, Long instanceId, NodeModel endNode, InstanceState instanceState);

    /**
     * 保存流程实例
     *
     * @param flwInstance 流程实例对象
     * @param flwProcess  流程定义对象
     * @param saveAsDraft 暂存草稿
     * @param flowCreator 处理人员
     */
    void saveInstance(FlwInstance flwInstance, FlwProcess flwProcess, boolean saveAsDraft, FlowCreator flowCreator);

    /**
     * 暂停流程实例 {@link InstanceState#suspend}
     *
     * @param instanceId  流程实例ID
     * @param flowCreator 处理人员
     * @return true 成功 false 失败
     */
    boolean suspendInstanceById(Long instanceId, FlowCreator flowCreator);

    /**
     * 流程实例拒绝审批强制终止（用于后续审核人员认为该审批不再需要继续，拒绝审批强行终止）
     *
     * @param instanceId     流程实例ID
     * @param currentFlwTask 当前任务
     * @param flowCreator    处理人员
     */
    boolean reject(Long instanceId, FlwTask currentFlwTask, FlowCreator flowCreator);

    default boolean reject(Long instanceId, FlowCreator flowCreator) {
        return this.reject(instanceId, null, flowCreator);
    }

    /**
     * 流程实例撤销（用于错误发起审批申请，发起人主动撤销）
     *
     * @param instanceId     流程实例ID
     * @param currentFlwTask 当前任务
     * @param flowCreator    处理人员
     */
    boolean revoke(Long instanceId, FlwTask currentFlwTask, FlowCreator flowCreator);

    default boolean revoke(Long instanceId, FlowCreator flowCreator) {
        return this.revoke(instanceId, null, flowCreator);
    }

    /**
     * 流程实例超时（设定审批时间超时，自动结束）
     *
     * @param instanceId     流程实例ID
     * @param currentFlwTask 当前任务
     * @param flowCreator    处理人员
     */
    boolean timeout(Long instanceId, FlwTask currentFlwTask, FlowCreator flowCreator);

    default boolean timeout(Long instanceId, FlowCreator flowCreator) {
        return this.timeout(instanceId, null, flowCreator);
    }

    /**
     * 流程实例超时（忽略操作权限）
     *
     * @param instanceId 流程实例ID
     */
    default boolean timeout(Long instanceId) {
        return this.timeout(instanceId, FlowCreator.ADMIN);
    }

    /**
     * 流程实例强制终止
     *
     * @param instanceId     流程实例ID
     * @param currentFlwTask 当前任务
     * @param flowCreator    处理人员
     */
    boolean terminate(Long instanceId, FlwTask currentFlwTask, FlowCreator flowCreator);

    default boolean terminate(Long instanceId, FlowCreator flowCreator) {
        return this.terminate(instanceId, null, flowCreator);
    }

    /**
     * 更新流程实例
     *
     * @param flwInstance 流程实例对象
     */
    void updateInstance(FlwInstance flwInstance);

    /**
     * 根据 流程实例ID 更新流程实例模型内容
     *
     * @param instanceId   流程实例ID
     * @param processModel 流程模型
     * @return true 成功 false 失败
     */
    boolean updateInstanceModelById(Long instanceId, ProcessModel processModel);

    /**
     * 级联删除指定流程实例的所有数据
     *
     * @param processId 流程ID
     */
    void cascadeRemoveByProcessId(Long processId);

    /**
     * 级联删除表 flw_his_task_actor, flw_his_task, flw_task_actor, flw_task, flw_instance, flw_his_instance
     *
     * @param instanceId 流程实例ID
     */
    void cascadeRemoveByInstanceId(Long instanceId);

    /**
     * 根据 流程实例ID 作废流程
     *
     * @param instanceId 流程实例ID
     * @param args 流程实例参数
     * @return true 成功 false 失败
     */
    boolean destroyByInstanceId(Long instanceId, Map<String, Object> args);

    /**
     * 追加节点模型（不执行任务跳转）
     * <p>
     * 执行追加节点模型调用 {@link FlowLongEngine#executeAppendNodeModel(Long, NodeModel, FlowCreator, boolean)}
     * </p>
     *
     * @param taskId      任务ID
     * @param nodeModel   节点模型
     * @param beforeAfter true 前置 false 后置
     */
    void appendNodeModel(Long taskId, NodeModel nodeModel, boolean beforeAfter);
}
