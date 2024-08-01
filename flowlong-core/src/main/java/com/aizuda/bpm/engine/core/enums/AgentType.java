/*
 * Copyright 2023-2025 Licensed under the AGPL License
 */
package com.aizuda.bpm.engine.core.enums;

import lombok.Getter;

import java.util.Objects;

/**
 * 代理人类型
 *
 * <p>
 * 尊重知识产权，不允许非法使用，后果自负
 * </p>
 *
 * @author hubin
 * @since 1.0
 */
@Getter
public enum AgentType {
    /**
     * 代理人
     */
    agent(0),
    /**
     * 被代理人
     */
    principal(1),
    /**
     * 认领角色
     */
    claimRole(2),
    /**
     * 认领部门
     */
    claimDepartment(3);

    private final int value;

    AgentType(int value) {
        this.value = value;
    }

    public boolean ne(Integer value) {
        return !eq(value);
    }

    public boolean eq(Integer value) {
        return Objects.equals(this.value, value);
    }
}
