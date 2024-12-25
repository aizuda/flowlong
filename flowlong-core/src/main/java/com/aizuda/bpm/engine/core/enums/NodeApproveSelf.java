/*
 * Copyright 2023-2025 Licensed under the Dual Licensing
 * website: https://aizuda.com
 */
package com.aizuda.bpm.engine.core.enums;

import java.util.Objects;

/**
 * 审批人与提交人为同一人时
 *
 * <p>
 * <a href="https://aizuda.com">官网</a>尊重知识产权，不允许非法使用，后果自负
 * </p>
 *
 * @author hubin
 * @since 1.0
 */
public enum NodeApproveSelf {
    /**
     * 由发起人对自己审批
     */
    initiatorThemselves(0),
    /**
     * 自动跳过
     */
    AutoSkip(1),
    /**
     * 转交给直接上级审批
     */
    TransferDirectSuperior(2),
    /**
     * 转交给部门负责人审批
     */
    TransferDepartmentHead(3);

    private final int value;

    NodeApproveSelf(int value) {
        this.value = value;
    }

    public boolean ne(Integer value) {
        return !eq(value);
    }

    public boolean eq(Integer value) {
        return Objects.equals(this.value, value);
    }

}

