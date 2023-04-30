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
import com.flowlong.bpm.engine.exception.FlowLongException;
import com.flowlong.bpm.engine.handler.JsonHandler;
import com.flowlong.bpm.engine.handler.impl.JacksonHandler;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * FlowLong流程引擎上下文
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
public class FlowLongContext {
    private ProcessService processService;
    private QueryService queryService;
    private RuntimeService runtimeService;
    private TaskService taskService;
    private ManagerService managerService;
    private Expression expression;

    private List<FlowLongInterceptor> interceptors;
    private TaskAccessStrategy taskAccessStrategy;

    /**
     * JSON 处理器，默认 jackson 实现
     * 使用其它json框架可以初始化的赋值该静态属性
     */
    public static JsonHandler JSON_HANDLER = new JacksonHandler();
    public static long REMIND_SCHEDULED_FIXED_DELAY = 5000;

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
