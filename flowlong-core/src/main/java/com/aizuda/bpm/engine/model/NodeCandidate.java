/*
 * Copyright 2023-2025 Licensed under the AGPL License
 */
package com.aizuda.bpm.engine.model;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

/**
 * JSON BPM 分配到任务的候选人或角色
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
public class NodeCandidate implements Serializable {

    /**
     * 候选类型
     * <p>
     * 1，用户 2，角色 3，部门
     * </p>
     */
    private Integer type;

    /**
     * 候选处理者，过 type 区分个人角色或部门
     */
    private List<NodeAssignee> assignees;

}
