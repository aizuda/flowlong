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

import com.flowlong.bpm.engine.FlowLongInterceptor;
import com.flowlong.bpm.engine.ModelInstance;
import com.flowlong.bpm.engine.assist.ClassUtils;
import com.flowlong.bpm.engine.assist.ObjectUtils;
import com.flowlong.bpm.engine.core.Execution;
import com.flowlong.bpm.engine.core.FlowLongContext;
import com.flowlong.bpm.engine.exception.FlowLongException;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * 节点元素（存在输入输出的变迁）
 *
 * <p>
 * 尊重知识产权，CV 请保留版权，爱组搭 http://aizuda.com 出品
 * </p>
 *
 * @author hubin
 * @since 1.0
 */
@Slf4j
@Getter
@Setter
public abstract class NodeModel extends BaseElement implements ModelInstance {
    /**
     * 输入变迁集合
     */
    private List<TransitionModel> inputs = new ArrayList<>();
    /**
     * 输出变迁集合
     */
    private List<TransitionModel> outputs = new ArrayList<>();
    /**
     * layout
     */
    private String layout;
    /**
     * 前置局部拦截器实例集合
     */
    protected List<FlowLongInterceptor> preInterceptorList = new ArrayList<>();
    /**
     * 后置局部拦截器实例集合
     */
    protected List<FlowLongInterceptor> postInterceptorList = new ArrayList<>();

    /**
     * 根据父节点模型、当前节点模型判断是否可退回。可退回条件：
     * 1、满足中间无fork、join、subprocess模型
     * 2、满足父节点模型如果为任务模型时，参与类型为any
     *
     * @param parent 父节点模型
     * @return 是否可以退回
     */
    public static boolean canRejected(NodeModel current, NodeModel parent) {
        if (parent instanceof TaskModel && !((TaskModel) parent).isPerformAny()) {
            return false;
        }
        boolean result = false;
        for (TransitionModel tm : current.getInputs()) {
            NodeModel source = tm.getSource();
            if (source == parent) {
                return true;
            }
            if (source instanceof ForkModel
                    || source instanceof JoinModel
                    || source instanceof SubProcessModel
                    || source instanceof StartModel) {
                continue;
            }
            result = result || canRejected(source, parent);
        }
        return result;
    }

    /**
     * 具体节点逻辑运行处理
     *
     * @param execution 执行对象
     */
    protected abstract void run(FlowLongContext flowLongContext, Execution execution);

    /**
     * 对执行逻辑增加前置、后置拦截处理
     *
     * @param execution 执行对象
     */
    @Override
    public void execute(FlowLongContext flowLongContext, Execution execution) {
        this.intercept(flowLongContext, preInterceptorList, execution);
        this.run(flowLongContext, execution);
        this.intercept(flowLongContext, postInterceptorList, execution);
    }

    /**
     * 运行变迁继续执行
     *
     * @param execution 执行对象
     */
    protected void runOutTransition(FlowLongContext flowLongContext, Execution execution) {
        this.getOutputs().forEach(t -> t.enable().execute(flowLongContext, execution));
    }

    /**
     * 拦截方法
     *
     * @param flowLongContext 流程引擎上下文
     * @param interceptorList 拦截器列表
     * @param execution       执行对象
     */
    private void intercept(FlowLongContext flowLongContext, List<FlowLongInterceptor> interceptorList, Execution execution) {
        try {
            interceptorList.forEach(i -> i.handle(flowLongContext, execution));
        } catch (Exception e) {
            log.error("拦截器执行失败: {}", e.getMessage());
            throw new FlowLongException(e);
        }
    }

    public <T> List<T> getNextModels(Class<T> clazz) {
        List<T> models = new ArrayList<>();
        for (TransitionModel tm : this.getOutputs()) {
            addNextModels(models, tm, clazz);
        }
        return models;
    }

    protected <T> void addNextModels(List<T> models, TransitionModel tm, Class<T> clazz) {
        if (clazz.isInstance(tm.getTarget())) {
            models.add((T) tm.getTarget());
        } else {
            for (TransitionModel tm2 : tm.getTarget().getOutputs()) {
                addNextModels(models, tm2, clazz);
            }
        }
    }

    public void setPreInterceptors(String preInterceptors) {
        this.parseInterceptor(preInterceptors, i -> this.preInterceptorList.add(i));
    }

    public void parseInterceptor(String interceptors, Consumer<FlowLongInterceptor> consumer) {
        if (ObjectUtils.isNotEmpty(interceptors)) {
            for (String interceptor : interceptors.split(",")) {
                FlowLongInterceptor instance = (FlowLongInterceptor) ClassUtils.newInstance(interceptor);
                if (null != instance) {
                    consumer.accept(instance);
                }
            }
        }
    }

    public void setPostInterceptors(String postInterceptors) {
        this.parseInterceptor(postInterceptors, i -> this.postInterceptorList.add(i));
    }
}
