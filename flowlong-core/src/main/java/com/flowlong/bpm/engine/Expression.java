/*
 * Copyright 2023-2025 Licensed under the AGPL License
 */
package com.flowlong.bpm.engine;

import com.flowlong.bpm.engine.assist.ObjectUtils;
import com.flowlong.bpm.engine.model.NodeExpression;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

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
     * @param conditionList 条件组列表
     * @param args          参数列表
     * @return T 返回对象
     */
    boolean eval(List<List<NodeExpression>> conditionList, Map<String, Object> args);

    /**
     * 根据表达式串、参数解析表达式并返回指定类型
     *
     * @param conditionList 条件组列表
     * @param evalFunc      执行表单式函数
     * @return true 成功 false 失败
     */
    default boolean eval(List<List<NodeExpression>> conditionList, Function<String, Boolean> evalFunc) {
        if (ObjectUtils.isEmpty(conditionList)) {
            return false;
        }
        String expr = conditionList.stream().map(cl -> cl.stream().map(t -> "#" + t.getField() + t.getOperator() + t.getValue())
                .collect(Collectors.joining(" && "))).collect(Collectors.joining(" || "));
        return evalFunc.apply(expr);
    }
}
