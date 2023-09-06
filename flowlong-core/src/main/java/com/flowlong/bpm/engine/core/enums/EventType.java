package com.flowlong.bpm.engine.core.enums;

/**
 * 流程引擎监听类型
 *
 * <p>
 * 尊重知识产权，CV 请保留版权，爱组搭 http://aizuda.com 出品，不允许非法使用，后果自负
 * </p>
 *
 * @author lizhongyuan
 * @since 1.0
 */
public enum EventType {
    /**
     * 创建
     */
    EVENT_CREATE,
    /**
     * 分配
     */
    EVENT_ASSIGNMENT,
    /**
     * 完成
     */
    EVENT_COMPLETE,
    /**
     * 终止
     */
    EVENT_TERMINATE,
    /**
     * 更新
     */
    EVENT_UPDATE,
    /**
     * 删除
     */
    EVENT_DELETE,
    /**
     * 驳回
     */
    EVENT_REJECT,
    /**
     * 超时
     */
    EVENT_TIMEOUT
}
