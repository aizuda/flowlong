/*
 * Copyright 2023-2025 Licensed under the AGPL License
 */
package com.aizuda.bpm.engine;

import com.aizuda.bpm.engine.core.Execution;
import com.aizuda.bpm.engine.core.FlowCreator;
import com.aizuda.bpm.engine.entity.FlwInstance;
import com.aizuda.bpm.engine.entity.FlwProcess;
import com.aizuda.bpm.engine.model.NodeModel;
import com.aizuda.bpm.engine.model.ProcessModel;

import java.util.Map;
import java.util.function.Supplier;

/**
 * 流程实例运行业务类
 *
 * <p>
 * 尊重知识产权，不允许非法使用，后果自负
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
     * @param supplier    初始化流程实例提供者
     * @return 活动流程实例对象
     */
    FlwInstance createInstance(FlwProcess flwProcess, FlowCreator flowCreator, Map<String, Object> args, NodeModel nodeModel, Supplier<FlwInstance> supplier);

    /**
     * 根据流程实例ID获取流程实例模型
     *
     * @param instanceId 流程实例ID
     * @return {@link ProcessModel}
     */
    ProcessModel getProcessModelByInstanceId(Long instanceId);

    /**
     * 向指定实例id添加全局变量数据
     *
     * @param instanceId 实例id
     * @param args       变量数据
     */
    void addVariable(Long instanceId, Map<String, Object> args);

    /**
     * 结束流程实例（审批通过）
     *
     * @param execution  {@link Execution}
     * @param instanceId 流程实例ID
     * @param endNode    结束节点
     * @return true 成功 false 失败
     */
    boolean endInstance(Execution execution, Long instanceId, NodeModel endNode);

    /**
     * 保存流程实例
     *
     * @param flwInstance 流程实例对象
     * @param flwProcess  流程定义对象
     * @param flowCreator 处理人员
     */
    void saveInstance(FlwInstance flwInstance, FlwProcess flwProcess, FlowCreator flowCreator);

    /**
     * 流程实例拒绝审批强制终止（用于后续审核人员认为该审批不再需要继续，拒绝审批强行终止）
     *
     * @param instanceId  流程实例ID
     * @param flowCreator 处理人员
     */
    void reject(Long instanceId, FlowCreator flowCreator);

    /**
     * 流程实例撤销（用于错误发起审批申请，发起人主动撤销）
     *
     * @param instanceId  流程实例ID
     * @param flowCreator 处理人员
     */
    void revoke(Long instanceId, FlowCreator flowCreator);

    /**
     * 流程实例超时（设定审批时间超时，自动结束）
     *
     * @param instanceId  流程实例ID
     * @param flowCreator 处理人员
     */
    void timeout(Long instanceId, FlowCreator flowCreator);

    /**
     * 流程实例超时（忽略操作权限）
     *
     * @param instanceId 流程实例ID
     */
    default void timeout(Long instanceId) {
        this.timeout(instanceId, FlowCreator.ADMIN);
    }

    /**
     * 流程实例强制终止
     *
     * @param instanceId  流程实例ID
     * @param flowCreator 处理人员
     */
    void terminate(Long instanceId, FlowCreator flowCreator);

    /**
     * 更新流程实例
     *
     * @param flwInstance 流程实例对象
     */
    void updateInstance(FlwInstance flwInstance);

    /**
     * 级联删除指定流程实例的所有数据
     *
     * @param processId 流程ID
     */
    void cascadeRemoveByProcessId(Long processId);

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
