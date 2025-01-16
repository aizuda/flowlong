/*
 * Copyright 2023-2025 Licensed under the Dual Licensing
 * website: https://aizuda.com
 */
package com.aizuda.bpm.engine.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * JSON BPM 节点处理人或角色动态分配对象
 *
 * <p>
 * <a href="https://aizuda.com">官网</a>尊重知识产权，不允许非法使用，后果自负
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
     * 分配类型  0，用户 1，角色 2，部门 该属性决定 assigneeList 属性是分配到人还是角色
     * <p>
     * 需要与参数 {@link com.aizuda.bpm.engine.entity.FlwTaskActor#actorType} 值保持一致
     * </p>
     */
    private Integer type;

    public DynamicAssignee type(int type) {
        this.type = type;
        return this;
    }

    public DynamicAssignee assigneeList(List<NodeAssignee> assigneeList) {
        this.assigneeList = assigneeList;
        return this;
    }

    public static DynamicAssignee builder() {
        return new DynamicAssignee();
    }

    public static DynamicAssignee assigneeUserList(List<NodeAssignee> assigneeList) {
        return of(0, assigneeList);
    }

    public static DynamicAssignee assigneeRoleList(List<NodeAssignee> assigneeList) {
        return of(1, assigneeList);
    }

    public static DynamicAssignee assigneeDeptList(List<NodeAssignee> assigneeList) {
        return of(2, assigneeList);
    }

    public static DynamicAssignee of(Integer type, List<NodeAssignee> assigneeList) {
        return builder().type(type).assigneeList(assigneeList);
    }
}
