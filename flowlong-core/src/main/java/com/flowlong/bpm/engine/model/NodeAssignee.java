/*
 * Copyright 2023-2025 Licensed under the AGPL License
 */
package com.flowlong.bpm.engine.model;

import com.flowlong.bpm.engine.entity.FlwTaskActor;
import lombok.Getter;
import lombok.Setter;

/**
 * JSON BPM 分配到任务的人
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
public class NodeAssignee {
    /**
     * 主键ID
     */
    private String id;
    /**
     * 名称
     */
    private String name;
    /**
     * 权重（ 用于票签，多个参与者合计权重 100% ）
     */
    private Integer weight;

    public static NodeAssignee of(FlwTaskActor flwTaskActor) {
        NodeAssignee nodeAssignee = new NodeAssignee();
        nodeAssignee.setId(flwTaskActor.getActorId());
        nodeAssignee.setName(flwTaskActor.getActorName());
        return nodeAssignee;
    }
}
