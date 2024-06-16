/*
 * Copyright 2023-2025 Licensed under the AGPL License
 */
package com.aizuda.bpm.engine.core.enums;

import lombok.Getter;

import java.util.Arrays;
import java.util.Objects;

/**
 * 参与类型
 *
 * <p>
 * 尊重知识产权，不允许非法使用，后果自负
 * </p>
 *
 * @author hubin
 * @since 1.0
 */
@Getter
public enum PerformType {
    /**
     * 发起
     */
    start(0),
    /**
     * 按顺序依次审批
     */
    sort(1),
    /**
     * 会签 (可同时审批，每个人必须审批通过)
     */
    countersign(2),
    /**
     * 或签 (有一人审批通过即可)
     */
    orSign(3),
    /**
     * 票签 (总权重大于节点 passWeight 属性)
     */
    voteSign(4),
    /**
     * 定时器
     */
    timer(6),
    /**
     * 触发器
     */
    trigger(7),
    /**
     * 抄送
     */
    copy(9);

    private final int value;

    PerformType(int value) {
        this.value = value;
    }

    public boolean ne(Integer value) {
        return !eq(value);
    }

    public boolean eq(Integer value) {
        return Objects.equals(this.value, value);
    }

    public static PerformType get(Integer value) {
        if (null == value) {
            // 默认，按顺序依次审批
            return sort;
        }
        return Arrays.stream(PerformType.values()).filter(s -> s.getValue() == value).findFirst().orElse(sort);
    }

}
