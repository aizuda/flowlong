/*
 * Copyright 2023-2025 Licensed under the AGPL License
 */
package com.flowlong.bpm.engine.impl;

import com.flowlong.bpm.engine.TaskActorProvider;
import com.flowlong.bpm.engine.assist.ObjectUtils;
import com.flowlong.bpm.engine.core.Execution;
import com.flowlong.bpm.engine.entity.FlwTaskActor;
import com.flowlong.bpm.engine.model.NodeModel;

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
        if (ObjectUtils.isNotEmpty(nodeModel.getNodeUserList())) {
            // 指定用户审批
            nodeModel.getNodeUserList().forEach(t -> flwTaskActors.add(FlwTaskActor.of(t, 0)));
        } else if (ObjectUtils.isNotEmpty(nodeModel.getNodeRoleList())) {
            // 指定角色审批
            nodeModel.getNodeRoleList().forEach(t -> flwTaskActors.add(FlwTaskActor.of(t, 1)));
        }
        return ObjectUtils.isEmpty(flwTaskActors) ? null : flwTaskActors;
    }
}
