package com.flowlong.bpm.solon.adaptive;


import com.flowlong.bpm.engine.Expression;

import java.util.Map;

/**
 * Spring el表达式解析器
 *
 * <p>
 * 尊重知识产权，CV 请保留版权，爱组搭 http://aizuda.com 出品，不允许非法使用，后果自负
 * </p>
 *
 * @author ximu
 * @since 1.0
 */
public class SolonSpelExpression implements Expression {
    //private final ExpressionParser parser;

    public SolonSpelExpression() {
        //parser = new SpelExpressionParser();
    }

    @Override
    public <T> T eval(Class<T> T, String expr, Map<String, Object> args) {
        return null;
//        EvaluationContext context = new StandardEvaluationContext();
//        for (Map.Entry<String, Object> entry : args.entrySet()) {
//            context.setVariable(entry.getKey(), entry.getValue());
//        }
//        return parser.parseExpression(expr).getValue(context, T);
    }

}
