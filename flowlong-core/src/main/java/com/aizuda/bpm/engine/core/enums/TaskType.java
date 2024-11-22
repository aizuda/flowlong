/*
 * Copyright 2023-2025 Licensed under the apache-2.0 License
 * website: https://aizuda.com
 */
package com.aizuda.bpm.engine.core.enums;

import com.aizuda.bpm.engine.entity.FlwTask;
import lombok.Getter;

import java.util.Arrays;
import java.util.Objects;

/**
 * 任务类型
 *
 * <p>
 * <a href="https://aizuda.com">官网</a>尊重知识产权，不允许非法使用，后果自负
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
     * 并行分支
     */
    parallelBranch(8),
    /**
     * 包容分支
     */
    inclusiveBranch(9),
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
     * 代理人归还的任务
     */
    agentReturn(14),
    /**
     * 代理人协办完成的任务
     */
    agentAssist(15),
    /**
     * 被代理人自己完成任务
     */
    agentOwn(16),
    /**
     * 拿回任务
     */
    reclaim(17),
    /**
     * 待撤回历史任务
     */
    withdraw(18),
    /**
     * 拒绝任务
     */
    reject(19),
    /**
     * 跳转任务，从上个任务 {@link FlwTask#getParentTaskId()} 跳转过来的
     */
    jump(20),
    /**
     * 驳回跳转
     */
    rejectJump(21),
    /**
     * 路由跳转
     */
    routeJump(22),
    /**
     * 路由分支
     */
    routeBranch(23);

    private final int value;

    TaskType(int value) {
        this.value = value;
    }

    public boolean ne(Integer value) {
        return !eq(value);
    }

    public boolean eq(Integer value) {
        return Objects.equals(this.value, value);
    }

    public static TaskType get(int value) {
        return Arrays.stream(TaskType.values()).filter(s -> s.getValue() == value).findFirst().orElse(null);
    }
}
