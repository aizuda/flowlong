/*
 * Copyright 2023-2025 Licensed under the Dual Licensing
 * website: https://aizuda.com
 */
package com.aizuda.bpm.engine.exception;

/**
 * FlowLong流程引擎异常类
 *
 * <p>
 * <a href="https://aizuda.com">官网</a>尊重知识产权，不允许非法使用，后果自负
 * </p>
 *
 * @author hubin
 * @since 1.0
 */
public class FlowLongException extends RuntimeException {

    public FlowLongException() {
        super();
    }

    public FlowLongException(String msg, Throwable cause) {
        super(msg);
        super.initCause(cause);
    }

    public FlowLongException(String msg) {
        super(msg);
    }

    public FlowLongException(Throwable cause) {
        super();
        super.initCause(cause);
    }
}
