/* Copyright 2023-2025 jobob@qq.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.flowlong.bpm.engine.core;

import com.flowlong.bpm.engine.Expression;
import de.odysseus.el.ExpressionFactoryImpl;
import de.odysseus.el.util.SimpleContext;

import javax.el.ExpressionFactory;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Juel 表达式引擎
 *
 * <p>
 * 尊重知识产权，CV 请保留版权，爱组搭 http://aizuda.com 出品
 * </p>
 *
 * @author hubin
 * @since 1.0
 */
public class JuelExpression implements Expression {
    private ExpressionFactory expressionFactory;

    public JuelExpression() {
        expressionFactory = new ExpressionFactoryImpl();
    }

    @Override
    public <T> T eval(Class<T> T, String expr, Map<String, Object> args) {
        SimpleContext context = new SimpleContext();
        for (Entry<String, Object> entry : args.entrySet()) {
            context.setVariable(entry.getKey(), expressionFactory.createValueExpression(entry.getValue(), Object.class));
        }
        return (T) expressionFactory.createValueExpression(context, expr, T).getValue(context);
    }
}
