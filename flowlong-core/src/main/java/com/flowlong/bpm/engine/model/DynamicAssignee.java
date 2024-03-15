/*
 * Copyright 2023-2025 Licensed under the AGPL License
 */
package com.flowlong.bpm.engine.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * JSON BPM 节点处理人或角色动态分配对象
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
public class DynamicAssignee {
    /**
     * 分配到任务的人或角色列表
     */
    private List<NodeAssignee> assigneeList;
    /**
     * 分配类型 1，用户 2，角色 该属性决定 assigneeList 属性是分配到人还是角色
     */
    private Integer type;

}
