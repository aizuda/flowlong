/* 
 * Copyright 2023-2025 Licensed under the AGPL License
 */
package com.flowlong.bpm.engine.core.enums;

import java.util.Arrays;

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
public enum PerformType {
    /**
     * 发起、其它
     */
    unknown(0),
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
     * 抄送
     */
    copy(9);

    private final int value;

    PerformType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static PerformType get(Integer value) {
        if (null == value) {
            return unknown;
        }
        return Arrays.stream(PerformType.values()).filter(s -> s.getValue() == value).findFirst().orElse(unknown);
    }

}
