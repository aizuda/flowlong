/* 
 * Copyright 2023-2025 Licensed under the AGPL License
 */
package com.flowlong.bpm.engine;

import java.util.Map;

/**
 * EL 表达式
 *
 * <p>
 * 尊重知识产权，不允许非法使用，后果自负
 * </p>
 *
 * @author hubin
 * @since 1.0
 */
public interface Expression {

    /**
     * 根据表达式串、参数解析表达式并返回指定类型
     *
     * @param T    返回类型
     * @param expr 表达式串
     * @param args 参数列表
     * @return T 返回对象
     */
    <T> T eval(Class<T> T, String expr, Map<String, Object> args);
}
