/*
 * Copyright 2023-2025 Licensed under the Dual Licensing
 * website: https://aizuda.com
 */
package com.aizuda.bpm.engine.handler.impl;

import com.aizuda.bpm.engine.assist.DateUtils;
import com.aizuda.bpm.engine.handler.FlowCreateTimeHandler;

import java.util.Date;

/**
 * 默认流程创建时间处理器
 *
 * <p>
 * <a href="https://aizuda.com">官网</a>尊重知识产权，不允许非法使用，后果自负
 * </p>
 *
 * @author hubin
 * @since 1.0
 */
public class SimpleFlowCreateTimeHandler implements FlowCreateTimeHandler {

    @Override
    public Date getCurrentTime(Long instanceId, Long taskId) {
        return DateUtils.getCurrentDate();
    }

    @Override
    public Date getFinishTime(Long instanceId, Long taskId) {
        return DateUtils.getCurrentDate();
    }
}
