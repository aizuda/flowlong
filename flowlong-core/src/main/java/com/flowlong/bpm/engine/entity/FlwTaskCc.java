/* 
 * Copyright 2023-2025 Licensed under the AGPL License
 */
package com.flowlong.bpm.engine.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;

/**
 * 抄送任务实体类
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
public class FlwTaskCc extends FlowEntity {
    /**
     * 流程实例ID
     */
    protected Long instanceId;
    /**
     * 父任务ID
     */
    protected Long parentTaskId;
    /**
     * 任务名称
     */
    protected String taskName;
    /**
     * 任务显示名称
     */
    protected String displayName;
    /**
     * 关联的参与者ID（参与者可以为用户、部门、角色）
     */
    protected String actorId;
    /**
     * 关联的参与者名称
     */
    protected String actorName;
    /**
     * 任务类型 0，用户 1，角色 2，部门
     */
    protected Integer taskType;
    /**
     * 任务状态 0，结束 1，活动
     */
    protected Integer taskState;
    /**
     * 完成时间
     */
    protected Date finishTime;

}
