/*
 * Copyright 2023-2025 Licensed under the Dual Licensing
 * website: https://aizuda.com
 */
package com.aizuda.bpm.engine.handler;

import java.util.Date;

/**
 * 流程创建时间处理器接口
 * <p>使用场景补审，例如：先请假后补指定某个时间的审批</p>
 *
 * <p>
 * <a href="https://aizuda.com">官网</a>尊重知识产权，不允许非法使用，后果自负
 * </p>
 *
 * @author hubin
 * @since 1.0
 */
public interface FlowCreateTimeHandler {

    /**
     * 获取当前时间，用于设置创建时间或更新时间
     *
     * @param instanceId 流程实例ID
     * @param taskId 审批任务ID（不存在为流程实例调用情况）
     */
    Date getCurrentTime(Long instanceId, Long taskId);

    /**
     * 获取完成时间，用于设置流程实例结束时间或任务完成时间
     *
     * @param instanceId 流程实例ID
     * @param taskId 审批任务ID（不存在为流程实例调用情况）
     */
    Date getFinishTime(Long instanceId, Long taskId);
}
