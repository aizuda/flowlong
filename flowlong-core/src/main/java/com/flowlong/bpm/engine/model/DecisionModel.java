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
package com.flowlong.bpm.engine.model;

import com.flowlong.bpm.engine.Expression;
import com.flowlong.bpm.engine.assist.ClassUtils;
import com.flowlong.bpm.engine.assist.ObjectUtils;
import com.flowlong.bpm.engine.core.Execution;
import com.flowlong.bpm.engine.core.FlowLongContext;
import com.flowlong.bpm.engine.exception.FlowLongException;
import com.flowlong.bpm.engine.handler.DecisionHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * 决策定义decision元素
 *
 * <p>
 * 尊重知识产权，CV 请保留版权，爱组搭 http://aizuda.com 出品
 * </p>
 *
 * @author hubin
 * @since 1.0
 */
@Slf4j
public class DecisionModel extends NodeModel {
    /**
     * 决策选择表达式串（需要表达式引擎解析）
     */
    private String expr;
    /**
     * 决策处理类，对于复杂的分支条件，可通过handleClass来处理
     */
    private String handleClass;
    /**
     * 决策处理类实例
     */
    private DecisionHandler decide;
    /**
     * 表达式解析器
     */
    private transient Expression expression;

    @Override
    public void run(FlowLongContext flowLongContext, Execution execution) {
        log.info(execution.getInstance().getId() + "->decision execution.getArgs():" + execution.getArgs());
        if (expression == null) {
            expression = flowLongContext.getExpression();
        }
        log.info("expression is " + expression);
        if (expression == null) {
            throw new FlowLongException("表达式解析器为空，请检查配置.");
        }
        String next = null;
        if (ObjectUtils.isNotEmpty(expr)) {
            next = expression.eval(String.class, expr, execution.getArgs());
        } else if (decide != null) {
            next = decide.decide(flowLongContext, execution);
        }
        log.info(execution.getInstance().getId() + "->decision expression[expr=" + expr + "] return result:" + next);
        boolean isFound = false;
        for (TransitionModel tm : getOutputs()) {
            if (ObjectUtils.isEmpty(next)) {
                String expr = tm.getExpr();
                if (ObjectUtils.isNotEmpty(expr) && expression.eval(Boolean.class, expr, execution.getArgs())) {
                    tm.setEnabled(true);
                    tm.execute(flowLongContext, execution);
                    isFound = true;
                }
            } else {
                if (tm.getName().equals(next)) {
                    tm.setEnabled(true);
                    tm.execute(flowLongContext, execution);
                    isFound = true;
                }
            }
        }
        if (!isFound) {
            throw new FlowLongException(execution.getInstance().getId() + "->decision节点无法确定下一步执行路线");
        }
    }

    public String getExpr() {
        return expr;
    }

    public void setExpr(String expr) {
        this.expr = expr;
    }

    public String getHandleClass() {
        return handleClass;
    }

    public void setHandleClass(String handleClass) {
        this.handleClass = handleClass;
        if (ObjectUtils.isNotEmpty(handleClass)) {
            decide = (DecisionHandler) ClassUtils.newInstance(handleClass);
        }
    }
}
