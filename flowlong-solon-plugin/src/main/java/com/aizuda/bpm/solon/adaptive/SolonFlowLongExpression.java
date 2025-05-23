/*
 * Copyright 2023-2025 Licensed under the Dual Licensing
 * website: https://aizuda.com
 */
package com.aizuda.bpm.solon.adaptive;

import com.aizuda.bpm.engine.FlowLongExpression;
import com.aizuda.bpm.engine.model.NodeExpression;
import com.googlecode.aviator.AviatorEvaluator;
import org.noear.solon.annotation.Component;

import java.util.List;
import java.util.Map;

/**
 * Solon 表达式解析器适配
 *
 * <p>
 * <a href="https://aizuda.com">官网</a>尊重知识产权，不允许非法使用，后果自负
 * </p>
 *
 * @author noear
 * @since 1.0
 */
@Component
public class SolonFlowLongExpression implements FlowLongExpression {

    @Override
    public boolean eval(List<List<NodeExpression>> conditionList, Map<String, Object> args) {
        return this.eval(conditionList, () -> args, expr -> {
            if (expr.startsWith("#{")) {
                expr = expr.substring(2, expr.length() - 2);
            } else if (expr.startsWith("#")) {
                expr = expr.substring(1);
            }
            return (Boolean) AviatorEvaluator.execute(expr, args);
        });
    }
}
