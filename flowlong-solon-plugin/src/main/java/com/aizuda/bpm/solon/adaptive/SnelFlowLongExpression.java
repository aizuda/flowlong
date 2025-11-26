/*
 * Copyright 2023-2025 Licensed under the Dual Licensing
 * website: https://aizuda.com
 */
package com.aizuda.bpm.solon.adaptive;

import com.aizuda.bpm.engine.FlowLongExpression;
import com.aizuda.bpm.engine.model.NodeExpression;
import org.noear.solon.annotation.Component;
import org.noear.solon.expression.context.EnhanceContext;
import org.noear.solon.expression.snel.SnelEvaluateParser;

import java.util.List;
import java.util.Map;

/**
 * Solon SnEL 表达式解析器适配
 *
 * <p>
 * <a href="https://aizuda.com">官网</a>尊重知识产权，不允许非法使用，后果自负
 * </p>
 *
 * @author noear
 * @since 1.0
 */
@Component
public class SnelFlowLongExpression implements FlowLongExpression {
    private final SnelEvaluateParser parser;

    public SnelFlowLongExpression() {
        parser = new SnelEvaluateParser(2000);
    }

    @Override
    public boolean eval(List<List<NodeExpression>> conditionList, Map<String, Object> args) {
        return this.eval(conditionList, () -> args, expr -> {
            return (Boolean) parser.parse(expr).eval(new EnhanceContext(args));
        });
    }

    @Override
    public String exprOfArgs(NodeExpression nodeExpression, Map<String, Object> args) {
        String value = nodeExpression.getValue();
        String operator = nodeExpression.getOperator();
        String field = nodeExpression.getField();

        if ("include".equalsIgnoreCase(operator)) {
            return field + ".contains('" + value + "')";
        }

        if ("notinclude".equalsIgnoreCase(operator)) {
            return "NOT " + field + ".contains('" + value + "')";
        }

        Object fieldValue = args.get(nodeExpression.getField());

        if (fieldValue instanceof String) {
            value = "'" + value + "'";
        } else if (fieldValue instanceof Long) {
            value += "L";
        }

        return field + " " + operator + " " + value;
    }
}
