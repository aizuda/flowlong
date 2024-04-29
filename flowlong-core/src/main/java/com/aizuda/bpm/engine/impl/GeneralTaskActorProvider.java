/*
 * Copyright 2023-2025 Licensed under the AGPL License
 */
package com.aizuda.bpm.engine.impl;

import com.aizuda.bpm.engine.TaskActorProvider;
import com.aizuda.bpm.engine.assist.ObjectUtils;
import com.aizuda.bpm.engine.core.Execution;
import com.aizuda.bpm.engine.core.enums.NodeSetType;
import com.aizuda.bpm.engine.entity.FlwTaskActor;
import com.aizuda.bpm.engine.model.NodeAssignee;
import com.aizuda.bpm.engine.model.NodeModel;

import java.util.ArrayList;
import java.util.List;

/**
 * 普遍的任务参与者提供处理类
 *
 * <p>
 * 尊重知识产权，不允许非法使用，后果自负
 * </p>
 *
 * @author hubin
 * @since 1.0
 */
public class GeneralTaskActorProvider implements TaskActorProvider {

    public List<FlwTaskActor> getTaskActors(NodeModel nodeModel, Execution execution) {
        List<FlwTaskActor> flwTaskActors = new ArrayList<>();
        if (ObjectUtils.isNotEmpty(nodeModel.getNodeAssigneeList())) {
            // 0，用户 1，角色 2，部门
            Integer actorType = null;
            if (NodeSetType.specifyMembers.eq(nodeModel.getSetType())) {
                actorType = 0;
            } else if (NodeSetType.role.eq(nodeModel.getSetType())) {
                actorType = 1;
            } else if (NodeSetType.department.eq(nodeModel.getSetType())) {
                actorType = 2;
            }
            if (null != actorType) {
                for (NodeAssignee nodeAssignee : nodeModel.getNodeAssigneeList()) {
                    flwTaskActors.add(FlwTaskActor.of(nodeAssignee, actorType));
                }
            }
        }
        return ObjectUtils.isEmpty(flwTaskActors) ? null : flwTaskActors;
    }
}
