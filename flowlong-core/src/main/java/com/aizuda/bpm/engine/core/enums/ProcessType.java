/*
 * Copyright 2023-2025 Licensed under the apache-2.0 License
 * website: https://aizuda.com
 */
package com.aizuda.bpm.engine.core.enums;

import java.util.Objects;

/**
 * 流程类型
 *
 * <p>
 * <a href="https://aizuda.com">官网</a>尊重知识产权，不允许非法使用，后果自负
 * </p>
 *
 * @author hubin
 * @since 1.0
 */
public enum ProcessType {
    /**
     * 业务流程
     */
    business,
    /**
     * 子流程
     */
    child,
    /**
     * 主流程
     */
    main;

    public boolean ne(String type) {
        return !eq(type);
    }

    public boolean eq(String type) {
        return Objects.equals(this.name(), type);
    }
}
