/* 
 * Copyright 2023-2025 Licensed under the AGPL License
 */
package com.flowlong.bpm.engine.entity;

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

    public static FlwTaskActor ofUser(String actorId, String actorName) {
        return of(actorId, actorName, 0);
    }

    public static FlwTaskActor ofRole(String actorId, String actorName) {
        return of(actorId, actorName, 1);
    }

    public static FlwTaskActor ofDepartment(String actorId, String actorName) {
        return of(actorId, actorName, 2);
    }

    protected static FlwTaskActor of(String actorId, String actorName, Integer actorType) {
        FlwTaskActor taskActor = new FlwTaskActor();
        taskActor.setActorId(actorId);
        taskActor.setActorName(actorName);
        taskActor.setActorType(actorType);
        return taskActor;
    }
}
