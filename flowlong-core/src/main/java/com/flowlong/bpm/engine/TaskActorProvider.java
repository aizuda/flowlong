/*
 * Copyright 2023-2025 Licensed under the AGPL License
 */
package com.flowlong.bpm.engine;

import com.flowlong.bpm.engine.assist.Assert;
import com.flowlong.bpm.engine.core.Execution;
import com.flowlong.bpm.engine.core.FlowCreator;
import com.flowlong.bpm.engine.entity.FlwTaskActor;
import com.flowlong.bpm.engine.model.NodeAssignee;
import com.flowlong.bpm.engine.model.NodeModel;

import java.util.List;
import java.util.Objects;

/**
 * 任务参与者提供处理接口
 *
 * <p>
 * 尊重知识产权，不允许非法使用，后果自负
 * </p>
 *
 * @author hubin
 * @since 1.0
 */
public interface TaskActorProvider {

    /**
     * 根据Task模型的assignee、assignmentHandler属性以及运行时数据，确定参与者
     *
     * @param nodeModel 节点模型
     * @param execution 执行对象
     * @return 参与者数组
     */
    List<FlwTaskActor> getTaskActors(NodeModel nodeModel, Execution execution);

    /**
     * 流程创建者是否允许操作执行当前节点
     *
     * @param nodeModel   当前执行节点
     * @param flowCreator 流程创建者
     * @return true 允许 false 不被允许
     */
    default boolean isAllowed(NodeModel nodeModel, FlowCreator flowCreator) {
        List<NodeAssignee> nodeUserList = nodeModel.getNodeUserList();
        if (null != nodeUserList) {
            return nodeUserList.stream().anyMatch(t -> Objects.equals(t.getId(), flowCreator.getCreateId()));
        }
        // 角色判断必须要求子类实现
        Assert.isTrue(null != nodeModel.getNodeRoleList(), "Please implement the interface TaskActorProvider method isAllow");
        return true;
    }
}
