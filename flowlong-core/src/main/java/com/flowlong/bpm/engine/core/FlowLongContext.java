/*
 * Copyright 2023-2025 Licensed under the AGPL License
 */
package com.flowlong.bpm.engine.core;

import com.flowlong.bpm.engine.*;
import com.flowlong.bpm.engine.assist.Assert;
import com.flowlong.bpm.engine.handler.CreateTaskHandler;
import com.flowlong.bpm.engine.handler.FlowJsonHandler;
import com.flowlong.bpm.engine.handler.impl.DefaultCreateTaskHandler;
import com.flowlong.bpm.engine.impl.DefaultProcessModelParser;
import com.flowlong.bpm.engine.model.NodeModel;
import com.flowlong.bpm.engine.model.ProcessModel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * FlowLong流程引擎上下文
 *
 * <p>
 * 尊重知识产权，不允许非法使用，后果自负
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
    /**
     * 流程任务创建处理器
     */
    private CreateTaskHandler createTaskHandler;
    /**
     * 流程引擎拦截器
     */
    private List<FlowLongInterceptor> interceptors;
    private TaskAccessStrategy taskAccessStrategy;

    /**
     * 审批参与者提供者
     */
    private TaskActorProvider taskActorProvider;

    /**
     * 静态注入流程模型解析器
     */
    @Setter
    private static ProcessModelParser PROCESS_MODEL_PARSER;

    public static ProcessModel parseProcessModel(String content, String cacheKey, boolean redeploy) {
        return PROCESS_MODEL_PARSER.parse(content, cacheKey, redeploy);
    }

    public static void invalidateProcessModel(String cacheKey) {
        PROCESS_MODEL_PARSER.invalidate(cacheKey);
    }

    /**
     * 注入默认流程模型解析器
     */
    public FlowLongContext() {
        this(new DefaultProcessModelParser());
    }

    public FlowLongContext(ProcessModelParser processModelParser) {
        Assert.isNull(processModelParser, "Please implement the ProcessModelParser interface class");
        PROCESS_MODEL_PARSER = processModelParser;
    }


    /**
     * 流程 JSON 处理器，默认 jackson 实现
     * 使用其它json框架可在初始化时赋值该静态属性
     */
    @Setter
    private static FlowJsonHandler flowJsonHandler;

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
     * 获取创建流程任务处理器实现类
     *
     * @return {@link CreateTaskHandler}
     */
    public CreateTaskHandler getCreateTaskHandler() {
        return null != createTaskHandler ? createTaskHandler : DefaultCreateTaskHandler.getInstance();
    }

    /**
     * 创建流程任务
     *
     * @param execution 执行对象
     * @param nodeModel 节点模型
     * @return true 执行成功  false 执行失败
     */
    public boolean createTask(Execution execution, NodeModel nodeModel) {
        return this.getCreateTaskHandler().handle(this, execution, nodeModel);
    }

    /**
     * 默认初始化流程引擎上下文
     *
     * @return {@link FlowLongEngine}
     */
    public FlowLongContext build(FlowLongEngine configEngine) {
        if (log.isInfoEnabled()) {
            log.info("FlowLongEngine start......");
        }
        /*
         * 由服务上下文返回流程引擎
         */
        Assert.isNull(configEngine, "Unable to discover implementation class for LongEngine");
        if (log.isInfoEnabled()) {
            log.info("FlowLongEngine be found:" + configEngine.getClass());
        }
        configEngine.configure(this);
        return this;
    }

}
