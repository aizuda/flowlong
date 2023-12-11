/*
 * Copyright 2023-2025 Licensed under the AGPL License
 */
package com.flowlong.bpm.engine.entity;

import com.flowlong.bpm.engine.core.FlowCreator;
import com.flowlong.bpm.engine.model.NodeAssignee;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

/**
 * 任务参与者实体类
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
public class FlwTaskActor implements Serializable {
    /**
     * 主键ID
     */
    protected Long id;
    /**
     * 租户ID
     */
    protected String tenantId;
    /**
     * 流程实例ID
     */
    protected Long instanceId;
    /**
     * 关联的任务ID
     */
    protected Long taskId;
    /**
     * 关联的参与者ID（参与者可以为用户、部门、角色）
     */
    protected String actorId;
    /**
     * 关联的参与者名称
     */
    protected String actorName;
    /**
     * 参与者类型 0，用户 1，角色 2，部门
     */
    protected Integer actorType;
    /**
     * 票签权重
     */
    protected Integer weight;

    public static FlwTaskActor ofFlwTask(FlwTask flwTask) {
        FlwTaskActor flwTaskActor = FlwTaskActor.ofUser(flwTask.getTenantId(),
                flwTask.getCreateId(), flwTask.getCreateBy());
        flwTaskActor.setInstanceId(flwTask.getInstanceId());
        flwTaskActor.setTaskId(flwTask.getId());
        return flwTaskActor;
    }

    public static FlwTaskActor ofFlowCreator(FlowCreator flowCreator) {
        return ofUser(flowCreator.getTenantId(), flowCreator.getCreateId(), flowCreator.getCreateBy());
    }

    public static FlwTaskActor ofNodeAssignee(NodeAssignee nodeAssignee) {
        return ofUser(nodeAssignee.getTenantId(), nodeAssignee.getId(), nodeAssignee.getName());
    }

    public static FlwTaskActor ofUser(String tenantId, String actorId, String actorName) {
        return of(tenantId, actorId, actorName, 0, null);
    }

    public static FlwTaskActor ofRole(String tenantId, String actorId, String actorName) {
        return of(tenantId, actorId, actorName, 1, null);
    }

    public static FlwTaskActor ofDepartment(String tenantId, String actorId, String actorName) {
        return of(tenantId, actorId, actorName, 2, null);
    }

    public static FlwTaskActor of(NodeAssignee nodeAssignee, Integer actorType) {
        return of(nodeAssignee.getTenantId(), nodeAssignee.getId(), nodeAssignee.getName(), actorType, nodeAssignee.getWeight());
    }

    protected static FlwTaskActor of(String tenantId, String actorId, String actorName, Integer actorType, Integer weight) {
        FlwTaskActor taskActor = new FlwTaskActor();
        taskActor.setTenantId(tenantId);
        taskActor.setActorId(actorId);
        taskActor.setActorName(actorName);
        taskActor.setActorType(actorType);
        taskActor.setWeight(weight);
        return taskActor;
    }

}
