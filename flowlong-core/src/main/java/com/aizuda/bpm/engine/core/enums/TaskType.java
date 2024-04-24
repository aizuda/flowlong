/* 
 * Copyright 2023-2025 Licensed under the AGPL License
 */
package com.aizuda.bpm.engine.core.enums;

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
     * 结束节点
     */
    end(-1),
    /**
     * 主办
     */
    major(0),
    /**
     * 审批
     */
    approval(1),
    /**
     * 抄送
     */
    cc(2),
    /**
     * 条件审批
     */
    conditionNode(3),
    /**
     * 条件分支
     */
    conditionBranch(4),
    /**
     * 调用外部流程任务【办理子流程】
     */
    callProcess(5),
    /**
     * 定时器任务
     */
    timer(6),
    /**
     * 触发器任务
     */
    trigger(7),
    /**
     * 转办、代理人办理完任务直接进入下一个节点
     */
    transfer(10),
    /**
     * 委派、代理人办理完任务该任务重新归还给原处理人
     */
    delegate(11),
    /**
     * 委派归还任务
     */
    delegateReturn(12),
    /**
     * 代理人任务
     */
    agent(13),
    /**
     * 代理人归还任务
     */
    agentReturn(14);

    private final int value;

    TaskType(int value) {
        this.value = value;
    }

    public boolean eq(int value) {
        return this.value == value;
    }

    public static TaskType get(int value) {
        return Arrays.stream(TaskType.values()).filter(s -> s.getValue() == value).findFirst().orElse(null);
    }
}
