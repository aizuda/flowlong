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
    custom,
    /**
     * 表单条件字段
     */
    form;

    public boolean ne(String type) {
        return !eq(type);
    }

    public boolean eq(String type) {
        return Objects.equals(this.name(), type);
    }
}
