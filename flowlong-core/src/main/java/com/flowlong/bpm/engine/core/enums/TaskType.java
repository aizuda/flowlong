/* 
 * Copyright 2023-2025 Licensed under the AGPL License
 */
package com.flowlong.bpm.engine.core.enums;

import lombok.Getter;

import java.util.Arrays;

/**
 * 任务类型
 *
 * <p>
 * 尊重知识产权，不允许非法使用，后果自负
 * </p>
 *
 * @author hubin
 * @since 1.0
 */
@Getter
public enum TaskType {
    /**
     * 主办
     */
    major(0),
    /**
     * 审批
     */
    approval(1),
    /**
     * 转办、代理人办理完任务直接进入下一个节点
     */
    transfer(2),
    /**
     * 委派、代理人办理完任务该任务重新归还给原处理人
     */
    delegate(3),
    /**
     * 会签
     */
    countersign(4),
    /**
     * 抄送
     */
    cc(5),
    /**
     * 调用外部流程任务
     */
    callProcess(6),
    /**
     * 定时器任务
     */
    timer(6),
    /**
     * 触发器任务
     */
    trigger(8);

    private final int value;

    TaskType(int value) {
        this.value = value;
    }

    public static TaskType get(int value) {
        return Arrays.stream(TaskType.values()).filter(s -> s.getValue() == value).findFirst().orElseGet(null);
    }
}
