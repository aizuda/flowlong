/*
 * Copyright 2023-2025 Licensed under the AGPL License
 */
package com.aizuda.bpm.engine.entity;

import com.aizuda.bpm.engine.core.enums.ActorType;
import com.aizuda.bpm.engine.model.NodeAssignee;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 历史任务参与者实体类
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
@ToString
public class FlwHisTaskActor extends FlwTaskActor {

    public static FlwHisTaskActor ofNodeAssignee(NodeAssignee nodeAssignee, Long instanceId, Long taskId) {
        FlwHisTaskActor his = new FlwHisTaskActor();
        his.setTenantId(nodeAssignee.getTenantId());
        his.setInstanceId(instanceId);
        his.setTaskId(taskId);
        his.setActorId(nodeAssignee.getId());
        his.setActorName(nodeAssignee.getName());
        his.setWeight(nodeAssignee.getWeight());
        his.setActorType(ActorType.user.getValue());
        return his;
    }

    public static FlwHisTaskActor ofFlwHisTask(FlwHisTask flwHisTask) {
        FlwHisTaskActor his = new FlwHisTaskActor();
        his.setTenantId(flwHisTask.getTenantId());
        his.setInstanceId(flwHisTask.getInstanceId());
        his.setTaskId(flwHisTask.getId());
        his.setActorId(flwHisTask.getCreateId());
        his.setActorName(flwHisTask.getCreateBy());
        his.setActorType(ActorType.user.getValue());
        return his;
    }

    public static FlwHisTaskActor of(FlwTaskActor taskActor) {
        FlwHisTaskActor his = new FlwHisTaskActor();
        his.setTenantId(taskActor.getTenantId());
        his.setInstanceId(taskActor.getInstanceId());
        his.setTaskId(taskActor.getTaskId());
        his.setActorId(taskActor.getActorId());
        his.setActorName(taskActor.getActorName());
        his.setWeight(taskActor.getWeight());
        his.setActorType(taskActor.getActorType());
        his.setAgentId(taskActor.getAgentId());
        his.setAgentType(taskActor.getAgentType());
        his.setExtend(taskActor.getExtend());
        return his;
    }
}
