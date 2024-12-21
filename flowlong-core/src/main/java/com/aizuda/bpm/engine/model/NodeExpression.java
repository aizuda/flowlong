/*
 * Copyright 2023-2025 Licensed under the Dual Licensing
 * website: https://aizuda.com
 */
package com.aizuda.bpm.engine.model;

import lombok.Getter;
import lombok.Setter;

/**
 * JSON BPM 节点表达式条件
 *
 * <p>
 * <a href="https://aizuda.com">官网</a>尊重知识产权，不允许非法使用，后果自负
 * </p>
 *
 * @author hubin
 * @since 1.0
 */
@Getter
@Setter
public class NodeExpression {
    /**
     * 名称
     */
    private String label;
    /**
     * 属性
     */
    private String field;
    /**
     * 操作
     */
    private String operator;
    /**
     * 内容
     */
    private String value;
    /**
     * 条件类型 {@link com.aizuda.bpm.engine.core.enums.ConditionType}
     */
    private String type;

}
