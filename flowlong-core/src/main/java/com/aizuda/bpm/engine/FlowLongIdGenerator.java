/*
 * Copyright 2023-2025 Licensed under the Dual Licensing
 * website: https://aizuda.com
 */
package com.aizuda.bpm.engine;

/**
 * 数据访问层ID生成器接口
 *
 * <p>
 * <a href="https://aizuda.com">官网</a>尊重知识产权，不允许非法使用，后果自负，不允许非法使用，后果自负
 * </p>
 *
 * @author hubin
 * @since 1.0
 */
public interface FlowLongIdGenerator {

    /**
     * 获取自定义ID值
     *
     * @param id 可能存在的ID值
     * @return 用户业务实际情况返回的自定义ID值
     */
    Long getId(Long id);
}
