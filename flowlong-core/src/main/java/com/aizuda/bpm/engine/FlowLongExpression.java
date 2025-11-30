/*
 * Copyright 2023-2025 Licensed under the Dual Licensing
 * website: https://aizuda.com
 */
package com.aizuda.bpm.engine;

import com.aizuda.bpm.engine.assist.ObjectUtils;
import com.aizuda.bpm.engine.model.NodeExpression;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * 飞龙条件表达式
 *
 * <p>
 * <a href="https://aizuda.com">官网</a>尊重知识产权，不允许非法使用，后果自负
 * </p>
 *
 * @author hubin
 * @since 1.0
 */
public interface FlowLongExpression {

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
     * @param argsSupplier  参数列表提供者
     * @param evalFunc      执行表单式函数
     * @return true 成功 false 失败
     */
    default boolean eval(List<List<NodeExpression>> conditionList, Supplier<Map<String, Object>> argsSupplier, Function<String, Boolean> evalFunc) {
        if (ObjectUtils.isEmpty(conditionList)) {
            return false;
        }

        Map<String, Object> args = argsSupplier.get();
        for (List<NodeExpression> nodeExpressions : conditionList) {
            // 判断参数是否包含参数，不包含则返回 false
            if (nodeExpressions.stream().anyMatch(t -> !args.containsKey(t.getField()))) {
                return false;
            }
        }

        // 执行存在参数的表达式
        String expr = conditionList.stream().map(cl -> cl.stream().map(t -> exprOfArgs(t, args))
                .collect(Collectors.joining(" && "))).collect(Collectors.joining(" || "));
        return evalFunc.apply(expr);
    }

    default String exprOfArgs(NodeExpression nodeExpression, Map<String, Object> args) {
        String value = nodeExpression.getValue();
        Object fieldValue = args.get(nodeExpression.getField());
        if (fieldValue instanceof String) {
            value = "'" + nodeExpression.getValue() + "'";
        }
        return "#" + nodeExpression.getField() + " " + nodeExpression.getOperator() + " " + value;
    }

    default boolean include(String value) {
        return "include".equalsIgnoreCase(value);
    }

    default boolean notInclude(String value) {
        return "notinclude".equalsIgnoreCase(value);
    }
}
