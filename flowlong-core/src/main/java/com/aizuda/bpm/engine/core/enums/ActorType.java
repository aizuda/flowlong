package com.aizuda.bpm.engine.core.enums;

import lombok.Getter;

/**
 * 参与者类型
 *
 * <p>
 * 尊重知识产权，不允许非法使用，后果自负
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

}
