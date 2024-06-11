/*
 * Copyright 2023-2025 Licensed under the AGPL License
 */
package com.aizuda.bpm.engine.core.enums;

/**
 * 流程引擎监听类型
 *
 * <p>
 * 尊重知识产权，不允许非法使用，后果自负
 * </p>
 *
 * @author lizhongyuan
 * @since 1.0
 */
public enum EventType {
    /**
     * 发起
     */
    start,
    /**
     * 创建
     */
    create,
    /**
     * 再创建，仅用于流程回退
     */
    recreate,
    /**
     * 抄送
     */
    cc,
    /**
     * 分配
     */
    assignment,
    /**
     * 任务加签
     */
    addTaskActor,
    /**
     * 任务减签
     */
    removeTaskActor,
    /**
     * 驳回至上一步处理
     */
    reject,
    /**
     * 认领
     */
    claim,
    /**
     * 拿回未执行任务
     */
    reclaim,
    /**
     * 撤回指定任务
     */
    withdraw,
    /**
     * 唤醒历史任务
     */
    resume,
    /**
     * 完成
     */
    complete,
    /**
     * 撤销
     */
    revoke,
    /**
     * 终止
     */
    terminate,
    /**
     * 更新
     */
    update,
    /**
     * 删除
     */
    delete,
    /**
     * 超时
     */
    timeout,
    /**
     * 跳转
     */
    jump,
    /**
     * 自动跳转
     */
    autoJump,
    /**
     * 自动审批完成
     */
    autoComplete,
    /**
     * 自动审批拒绝
     */
    autoReject;

    public boolean eq(EventType eventType) {
        return this == eventType;
    }

    public boolean ne(EventType eventType) {
        return this != eventType;
    }
}
