/* 
 * Copyright 2023-2025 Licensed under the AGPL License
 */
package com.flowlong.bpm.engine.entity;

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

    public static FlwHisTaskActor of(FlwTaskActor taskActor) {
        FlwHisTaskActor hisTaskActor = new FlwHisTaskActor();
        hisTaskActor.tenantId = taskActor.getTenantId();
        hisTaskActor.instanceId = taskActor.getInstanceId();
        hisTaskActor.taskId = taskActor.getTaskId();
        hisTaskActor.actorType = taskActor.getActorType();
        hisTaskActor.actorId = taskActor.getActorId();
        hisTaskActor.actorName = taskActor.getActorName();
        return hisTaskActor;
    }
}
