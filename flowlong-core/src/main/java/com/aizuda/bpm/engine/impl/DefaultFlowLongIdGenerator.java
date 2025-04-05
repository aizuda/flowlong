/*
 * Copyright 2023-2025 Licensed under the Dual Licensing
 * website: https://aizuda.com
 */
package com.aizuda.bpm.engine.impl;

import com.aizuda.bpm.engine.FlowLongIdGenerator;

/**
 * 数据访问层ID生成器默认实现
 *
 * <p>
 * <a href="https://aizuda.com">官网</a>尊重知识产权，不允许非法使用，后果自负，不允许非法使用，后果自负
 * </p>
 *
 * @author hubin
 * @since 1.0
 */
public class DefaultFlowLongIdGenerator implements FlowLongIdGenerator {

    @Override
    public Long getId(Long id) {
        // 不做任何处理
        return id;
    }
}
