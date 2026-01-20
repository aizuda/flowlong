/*
 * Copyright 2023-2025 Licensed under the Dual Licensing
 * website: https://aizuda.com
 */
package com.aizuda.bpm.spring.adaptive;

import com.aizuda.bpm.engine.FlowLongExpression;
import com.aizuda.bpm.engine.model.NodeExpression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Spring el表达式解析器
 *
 * <p>
 * <a href="https://aizuda.com">官网</a>尊重知识产权，不允许非法使用，后果自负
 * </p>
 *
 * @author ximu
 * @since 1.0
 */
public class SpelFlowLongExpression implements FlowLongExpression {
    private final ExpressionParser parser;

    public SpelFlowLongExpression() {
        parser = new SpelExpressionParser();
    }

    @Override
    public boolean eval(List<List<NodeExpression>> conditionList, Map<String, Object> args) {
        return this.eval(conditionList, () -> args, expr -> {
            StandardEvaluationContext context = new StandardEvaluationContext();
            context.setVariables(args);
            return parser.parseExpression(expr).getValue(context, Boolean.class);
        });
    }

    @Override
    public String exprOfArgs(NodeExpression nodeExpression, Map<String, Object> args) {
        String value = nodeExpression.getValue();
        String operator = nodeExpression.getOperator();
        String field = nodeExpression.getField();
        Object fieldValue = args.get(nodeExpression.getField());
        if (fieldValue instanceof String) {
            // 字符串
            if (this.include(operator)) {
                return "#" + field + ".contains('" + value + "')";
            } else if (this.notInclude(operator)) {
                return "not #" + field + ".contains('" + value + "')";
            }
            value = "'" + value + "'";
        } else if (fieldValue instanceof Long) {
            value += "L";
        } else if (fieldValue instanceof Collection) {
            // 集合情况
            if (this.notInclude(operator)) {
                return "not T(java.util.Arrays).asList(#" + field + ").contains('" + value + "')";
            } else {
                // 其它情况视为包含
                return "T(java.util.Arrays).asList(#" + field + ").contains('" + value + "')";
            }
        }
        // 其它
        return "#" + field + " " + operator + " " + value;
    }
}
