/* Copyright 2023-2025 www.flowlong.com
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
import com.flowlong.bpm.engine.access.jdbc.JdbcAccess;
import com.flowlong.bpm.engine.impl.GeneralAccessStrategy;
import com.flowlong.bpm.engine.impl.GeneralCompletion;
import com.flowlong.bpm.engine.parser.NodeParser;
import com.flowlong.bpm.engine.parser.impl.*;
import com.flowlong.bpm.engine.scheduling.IScheduler;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * FlowLong流程引擎配置处理类
 *
 * @author hubin
 * @since 1.0
 */
@Slf4j
@Getter
@Setter
public class FlowLongContext {
    private IProcessService processService;
    private IQueryService queryService;
    private IRuntimeService runtimeService;
    private ITaskService taskService;
    private IManagerService managerService;
    private JdbcAccess jdbcAccess;
    private Expression expression;

    /**
     * 调度器接口
     */
    private IScheduler scheduler;
    private List<FlowLongInterceptor> interceptors;
    private TaskAccessStrategy taskAccessStrategy;
    /**
     * 完成触发接口
     */
    private Completion completion;
    private Map<String, NodeParser> nodeParserMap = new ConcurrentHashMap<>();

    /**
     * 构造LongEngine对象，用于api集成
     * 通过SpringHelper调用
     *
     * @return FlowLongEngine
     * @throws FlowLongException
     */
    public FlowLongEngine build(DataSource dataSource) throws FlowLongException {
        if (log.isInfoEnabled()) {
            log.info("FlowLongEngine start......");
        }
        /**
         * 初始化内置节点解析类
         */
        nodeParserMap.put("start", new StartParser());
        nodeParserMap.put("task", new TaskParser());
        nodeParserMap.put("custom", new CustomParser());
        nodeParserMap.put("decision", new DecisionParser());
        nodeParserMap.put("subprocess", new SubProcessParser());
        nodeParserMap.put("fork", new ForkParser());
        nodeParserMap.put("join", new JoinParser());
        nodeParserMap.put("end", new EndParser());
        jdbcAccess = new JdbcAccess(dataSource);
        processService = new ProcessService(this, jdbcAccess);
        queryService = new QueryService(jdbcAccess);
        runtimeService = new RuntimeService(this, jdbcAccess);
        taskService = new TaskService(this, jdbcAccess);
        managerService = new ManagerService(jdbcAccess);
        taskAccessStrategy = new GeneralAccessStrategy();
        completion = new GeneralCompletion();
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

    public boolean isCMB() {
        return false;
    }

}
