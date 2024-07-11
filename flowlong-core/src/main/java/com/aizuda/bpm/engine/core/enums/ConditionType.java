package com.aizuda.bpm.engine.core.enums;

import java.util.Objects;

/**
 * JSON BPM 节点表达式 条件类型
 *
 * <p>
 * 尊重知识产权，不允许非法使用，后果自负
 * </p>
 *
 * @author insist
 * @since 1.0
 */
public enum ConditionType {
    /**
     * 自定义条件字段
     */
    custom("custom"),
    /**
     * 表单条件字段
     */
    form("form");

    private final String value;

    ConditionType(String value) {
        this.value = value;
    }

    public boolean ne(String value) {
        return !this.eq(value);
    }

    public boolean eq(String value) {
        return Objects.equals(this.value, value);
    }
}
