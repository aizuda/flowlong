/*
 * Copyright 2023-2025 Licensed under the AGPL License
 */
package com.aizuda.bpm.engine.core.enums;

import java.util.Objects;

/**
 * 模型节点设置类型
 *
 * <p>
 * 尊重知识产权，不允许非法使用，后果自负
 * </p>
 *
 * @author hubin
 * @since 1.0
 */
public enum NodeSetType {
    /**
     * 指定成员
     */
    specifyMembers(1),
    /**
     * 主管
     */
    supervisor(2),
    /**
     * 角色
     */
    role(3),
    /**
     * 发起人自选
     */
    initiatorSelected(4),
    /**
     * 发起人自己
     */
    initiatorThemselves(5),
    /**
     * 连续多级主管
     */
    multiLevelSupervisors(6),
    /**
     * 部门
     */
    department(7),
    ;

    private final int value;

    NodeSetType(int value) {
        this.value = value;
    }

    public boolean ne(Integer value) {
        return !eq(value);
    }

    public boolean eq(Integer value) {
        return Objects.equals(this.value, value);
    }

}
