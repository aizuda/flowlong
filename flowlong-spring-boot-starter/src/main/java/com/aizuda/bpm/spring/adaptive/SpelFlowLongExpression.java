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
        if ("include".equalsIgnoreCase(operator)) {
            return "#" + field + ".contains('" + value + "')";
        }
        if ("notinclude".equalsIgnoreCase(operator)) {
            return "not #" + field + ".contains('" + value + "')";
        }
        Object fieldValue = args.get(nodeExpression.getField());
        if (fieldValue instanceof String) {
            value = "'" + value + "'";
        }
        return "#" + field + " " + operator + " " + value;
    }
}
