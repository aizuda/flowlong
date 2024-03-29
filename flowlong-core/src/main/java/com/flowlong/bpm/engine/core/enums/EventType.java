package com.flowlong.bpm.engine.core.enums;

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
     * 创建
     */
    create,
    /**
     * 抄送
     */
    cc,
    /**
     * 分配
     */
    assignment,
    /**
     * 认领
     */
    claim,
    /**
     * 拿回
     */
    reclaim,
    /**
     * 唤醒
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
     * 驳回
     */
    reject,
    /**
     * 超时
     */
    timeout,
    /**
     * 跳转
     */
    jump
}
