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

import com.flowlong.bpm.engine.*;
import com.flowlong.bpm.engine.assist.Assert;
import com.flowlong.bpm.engine.cache.FlowCache;
import com.flowlong.bpm.engine.cache.FlowSimpleCache;
import com.flowlong.bpm.engine.exception.FlowLongException;
import com.flowlong.bpm.engine.handler.FlowJsonHandler;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * FlowLong流程引擎上下文
 *
 * <p>
 * 尊重知识产权，CV 请保留版权，爱组搭 http://aizuda.com 出品，不允许非法使用，后果自负
 * </p>
 *
 * @author hubin
 * @since 1.0
 */
@Slf4j
@Getter
@Setter
public class FlowLongContext {
    private ProcessService processService;
    private QueryService queryService;
    private RuntimeService runtimeService;
    private TaskService taskService;
    private Expression expression;

    private List<FlowLongInterceptor> interceptors;
    private TaskAccessStrategy taskAccessStrategy;

    /**
     * 流程 JSON 处理器，默认 jackson 实现
     * 使用其它json框架可在初始化时赋值该静态属性
     */
    @Setter
    private static FlowJsonHandler flowJsonHandler;

    /**
     * 流程缓存处理类，默认 ConcurrentHashMap 实现
     * 使用其它缓存框架可在初始化时赋值该静态属性
     */
    public static FlowCache FLOW_CACHE = new FlowSimpleCache();

    public static <T> T fromJson(String jsonString, Class<T> clazz) {
        return getFlowJsonHandler().fromJson(jsonString, clazz);
    }
    public static String toJson(Object object) {
        return getFlowJsonHandler().toJson(object);
    }

    private static FlowJsonHandler getFlowJsonHandler() {
        Assert.isNull(flowJsonHandler, "Please implement the FlowJsonHandler interface class");
        return flowJsonHandler;
    }

    /**
     * 默认初始化流程引擎上下文
     *
     * @return {@link FlowLongEngine}
     * @throws FlowLongException
     */
    public FlowLongEngine build() throws FlowLongException {
        if (log.isInfoEnabled()) {
            log.info("FlowLongEngine start......");
        }
        /**
         * 由服务上下文返回流程引擎
         */
        FlowLongEngine configEngine = new FlowLongEngineImpl();
        if (configEngine == null) {
            throw new FlowLongException("配置无法发现LongEngine的实现类");
        }
        if (log.isInfoEnabled()) {
            log.info("FlowLongEngine be found:" + configEngine.getClass());
        }
        return configEngine.configure(this);
    }

}
