/*
 * Copyright 2023-2025 Licensed under the AGPL License
 */
package com.aizuda.bpm.engine.entity;

import com.aizuda.bpm.engine.core.FlowCreator;
import com.aizuda.bpm.engine.core.FlowLongContext;
import com.aizuda.bpm.engine.model.NodeAssignee;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

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
     * 权重
     * <p>
     * 票签任务时，该值为不同处理人员的分量比例
     * </p>
     */
    protected Integer weight;
    /**
     * 代理人ID
     */
    protected String agentId;
    /**
     * 代理人类型 0，代理 1，被代理
     */
    protected Integer agentType;
    /**
     * 扩展json
     */
    protected String extend;

    /**
     * 是否为代理人
     *
     * @return true 是 false 否
     */
    public boolean agentActor() {
        return Objects.equals(0, this.actorType);
    }

    public boolean eqActorId(String actorId) {
        return Objects.equals(this.actorId, actorId);
    }

    public static FlwTaskActor of(FlowCreator flowCreator, FlwTask flwTask) {
        FlwTaskActor flwTaskActor = ofUser(flowCreator.getTenantId(), flowCreator.getCreateId(), flowCreator.getCreateBy());
        flwTaskActor.setInstanceId(flwTask.getInstanceId());
        flwTaskActor.setTaskId(flwTask.getId());
        return flwTaskActor;
    }

    public static FlwTaskActor ofAgentIt(FlowCreator flowCreator) {
        FlwTaskActor flwTaskActor = new FlwTaskActor();
        flwTaskActor.setAgentId(flowCreator.getCreateId());
        flwTaskActor.setAgentType(1);
        Map<String, Object> map = new HashMap<>();
        map.put("createBy", flowCreator.getCreateBy());
        flwTaskActor.setExtendOf(map);
        return flwTaskActor;
    }

    public static FlwTaskActor ofAgent(FlowCreator flowCreator, FlwTask flwTask, FlwTaskActor agentTaskActor) {
        FlwTaskActor flwTaskActor = of(flowCreator, flwTask);
        flwTaskActor.setAgentId(agentTaskActor.getActorId());
        flwTaskActor.setAgentType(0);
        Map<String, Object> map = new HashMap<>();
        map.put("actorType", agentTaskActor.getActorType());
        map.put("actorName", agentTaskActor.getActorName());
        flwTaskActor.setExtendOf(map);
        return flwTaskActor;
    }

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

    public static FlwTaskActor ofFlwInstance(FlwInstance flwInstance, Long taskId) {
        FlwTaskActor flwTaskActor = ofUser(flwInstance.getTenantId(), flwInstance.getCreateId(), flwInstance.getCreateBy());
        flwTaskActor.setInstanceId(flwInstance.getId());
        flwTaskActor.setTaskId(taskId);
        return flwTaskActor;
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

    public static FlwTaskActor of(Long taskId, FlwHisTaskActor t) {
        FlwTaskActor flwTaskActor = of(t.getTenantId(), t.getActorId(), t.getActorName(), t.getActorType(), t.getWeight());
        flwTaskActor.setTaskId(taskId);
        flwTaskActor.setInstanceId(t.getInstanceId());
        return flwTaskActor;
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

    public void setExtendOf(Object object) {
        this.extend = FlowLongContext.toJson(object);
    }
}
