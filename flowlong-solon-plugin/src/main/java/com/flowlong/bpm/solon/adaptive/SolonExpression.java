package com.flowlong.bpm.solon.adaptive;


import com.flowlong.bpm.engine.Expression;
import com.googlecode.aviator.AviatorEvaluator;
import org.noear.solon.annotation.Component;

import java.util.Map;

/**
 * Solon 表达式解析器适配
 *
 * <p>
 * 尊重知识产权，CV 请保留版权，爱组搭 http://aizuda.com 出品，不允许非法使用，后果自负
 * </p>
 *
 * @author ximu
 * @author noear
 * @since 1.0
 */
@Component
public class SolonExpression implements Expression {

    @Override
    public <T> T eval(Class<T> T, String expr, Map<String, Object> args) {
        if (expr.startsWith("#{")) {
            expr = expr.substring(2, expr.length() - 2);
        } else if (expr.startsWith("#")) {
            expr = expr.substring(1, expr.length() - 2);
        }

        return (T) AviatorEvaluator.execute(expr, args);
    }
}
