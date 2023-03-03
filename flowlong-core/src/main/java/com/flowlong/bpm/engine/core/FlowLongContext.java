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
import com.flowlong.bpm.engine.parser.NodeParser;
import com.flowlong.bpm.engine.parser.impl.*;
import com.flowlong.bpm.engine.scheduling.FlowLongScheduler;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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

    /**
     * 调度器接口
     */
    private FlowLongScheduler scheduler;
    private List<FlowLongInterceptor> interceptors;
    private TaskAccessStrategy taskAccessStrategy;

    /**
     * JSON 处理器，默认 jackson 实现
     * 使用其它json框架可以初始化的赋值该静态属性
     */
    public static JsonHandler JSON_HANDLER = new JacksonHandler();

    /**
     * 初始化内置节点解析类 MAP
     */
    private static Map<String, NodeParser> NODE_PARSER_MAP = new ConcurrentHashMap<String, NodeParser>() {{
        put("start", new StartParser());
        put("task", new TaskParser());
        put("custom", new CustomParser());
        put("decision", new DecisionParser());
        put("subprocess", new SubProcessParser());
        put("fork", new ForkParser());
        put("join", new JoinParser());
        put("end", new EndParser());
    }};

    public static NodeParser getNodeParser(String key) {
        return NODE_PARSER_MAP.get(key);
    }

    public FlowLongEngine build() throws FlowLongException {
        // 默认初始化流程引擎上下文
        return this.build(null);
    }

    public FlowLongEngine build(Map<String, NodeParser> nodeParserMap) throws FlowLongException {
        if (log.isInfoEnabled()) {
            log.info("FlowLongEngine start......");
        }
        /**
         * 预留注入自定义节点处理类
         */
        if (null != nodeParserMap) {
            NODE_PARSER_MAP.putAll(nodeParserMap);
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
