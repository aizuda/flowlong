/*
 * Copyright 2023-2025 Licensed under the apache-2.0 License
 * website: https://aizuda.com
 */
package com.aizuda.bpm.engine.core.enums;

import lombok.Getter;

import java.util.Objects;

/**
 * 参与者类型
 *
 * <p>
 * <a href="https://aizuda.com">官网</a>尊重知识产权，不允许非法使用，后果自负
 * </p>
 *
 * @author insist
 * @since 1.0
 */
@Getter
public enum ActorType {
    /**
     * 用户
     */
    user(0),
    /**
     * 角色
     */
    role(1),
    /**
     * 部门
     */
    department(2);

    private final int value;

    ActorType(int value) {
        this.value = value;
    }

    public boolean ne(Integer value) {
        return !eq(value);
    }

    public boolean eq(Integer value) {
        return Objects.equals(this.value, value);
    }
}
