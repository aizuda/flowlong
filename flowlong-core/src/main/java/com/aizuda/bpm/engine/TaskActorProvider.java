/*
 * Copyright 2023-2025 Licensed under the Dual Licensing
 * website: https://aizuda.com
 */
package com.aizuda.bpm.engine;

import com.aizuda.bpm.engine.assist.Assert;
import com.aizuda.bpm.engine.assist.ObjectUtils;
import com.aizuda.bpm.engine.core.Execution;
import com.aizuda.bpm.engine.core.FlowCreator;
import com.aizuda.bpm.engine.core.enums.NodeSetType;
import com.aizuda.bpm.engine.core.enums.PerformType;
import com.aizuda.bpm.engine.core.enums.TaskType;
import com.aizuda.bpm.engine.entity.FlwTask;
import com.aizuda.bpm.engine.entity.FlwTaskActor;
import com.aizuda.bpm.engine.model.NodeAssignee;
import com.aizuda.bpm.engine.model.NodeModel;

import java.util.List;
import java.util.Objects;

/**
 * 任务参与者提供处理接口
 *
 * <p>
 * <a href="https://aizuda.com">官网</a>尊重知识产权，不允许非法使用，后果自负
 * </p>
 *
 * @author hubin
 * @since 1.0
 */
public interface TaskActorProvider {

    /**
     * 流程创建者是否允许操作执行当前节点
     *
     * @param nodeModel   当前执行节点
     * @param flowCreator 流程创建者
     * @return true 允许 false 不被允许
     */
    default boolean isAllowed(NodeModel nodeModel, FlowCreator flowCreator) {
        List<NodeAssignee> nodeAssigneeList = nodeModel.getNodeAssigneeList();
        if (NodeSetType.specifyMembers.eq(nodeModel.getSetType()) && ObjectUtils.isNotEmpty(nodeAssigneeList)) {
            return nodeAssigneeList.stream().anyMatch(t -> Objects.equals(t.getId(), flowCreator.getCreateId()));
        }

        if (TaskType.major.eq(nodeModel.getType()) && !NodeSetType.initiatorSelected.eq(nodeModel.getSetType())) {
            // 发起人且非自选情况
            return true;
        }

        // 角色判断必须要求子类实现
        Assert.isEmpty(nodeAssigneeList, "Please implement the interface TaskActorProvider method isAllow");
        return true;
    }

    /**
     * 根据Task模型的assignee、assignmentHandler属性以及运行时数据，确定参与者
     *
     * @param nodeModel 节点模型
     * @param execution 执行对象
     * @return 参与者列表
     */
    List<FlwTaskActor> getTaskActors(NodeModel nodeModel, Execution execution);

    /**
     * 动态获取分配到任务的处理者列表
     *
     * @param nodeModel 节点模型
     * @param execution 执行对象
     * @return 参与者列表
     */
    default List<NodeAssignee> getNodeAssigneeList(NodeModel nodeModel, Execution execution) {
        return nodeModel.getNodeAssigneeList();
    }

    /**
     * 非正常创建任务处理逻辑，默认抛出异常
     *
     * @param flwTask     当前任务
     * @param performType 任务参与类型 {@link PerformType}
     * @param taskActors  任务参与者
     * @param execution   执行对象 {@link Execution}
     * @param nodeModel   模型节点 {@link NodeModel}
     * @return 返回 true 不再创建任务，返回 false 解决异常补充回写 taskActors 信息
     */
    default boolean abnormal(FlwTask flwTask, PerformType performType, List<FlwTaskActor> taskActors, Execution execution, NodeModel nodeModel) {
        Assert.illegal("taskActors cannot be empty. taskName = " + flwTask.getTaskName() + ", taskKey = " +
                flwTask.getTaskKey() + ", performType = " + performType.getValue());
        return true;
    }

    /**
     * 参与者类型转换处理方法
     *
     * @param nodeModel 当前审批节点 {@link NodeModel}
     * @return 返回值对应 flw_task_actor flw_his_task_actor 表字段参与者类型 actorType
     */
    Integer getActorType(NodeModel nodeModel);
}
