/*
 * Copyright 2023-2025 Licensed under the Dual Licensing
 * website: https://aizuda.com
 */
package com.aizuda.bpm.engine.core;

import com.aizuda.bpm.engine.*;
import com.aizuda.bpm.engine.assist.Assert;
import com.aizuda.bpm.engine.cache.FlowCache;
import com.aizuda.bpm.engine.handler.ConditionNodeHandler;
import com.aizuda.bpm.engine.handler.CreateTaskHandler;
import com.aizuda.bpm.engine.handler.FlowJsonHandler;
import com.aizuda.bpm.engine.handler.impl.SimpleConditionNodeHandler;
import com.aizuda.bpm.engine.handler.impl.SimpleCreateTaskHandler;
import com.aizuda.bpm.engine.impl.DefaultProcessModelParser;
import com.aizuda.bpm.engine.model.NodeModel;
import com.aizuda.bpm.engine.model.ProcessModel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * FlowLong流程引擎上下文
 *
 * <p>
 * <a href="https://aizuda.com">官网</a>尊重知识产权，不允许非法使用，后果自负
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
    private FlowLongExpression flowLongExpression;

    /**
     * 流程任务创建处理器
     */
    private CreateTaskHandler createTaskHandler;

    /**
     * 流程执行条件处理器
     */
    private ConditionNodeHandler conditionNodeHandler;

    /**
     * 流程任务创建拦截器
     */
    private TaskCreateInterceptor taskCreateInterceptor;

    /**
     * 任务访问策略类
     */
    private TaskAccessStrategy taskAccessStrategy;

    /**
     * 审批参与者提供者
     */
    private TaskActorProvider taskActorProvider;

    /**
     * 任务提醒接口
     */
    private TaskReminder taskReminder;

    /**
     * 审批参与者提供者
     */
    private TaskTrigger taskTrigger;

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
     *
     * @param flowCache          流程缓存
     * @param processModelParser 流程模型解析器
     */
    public FlowLongContext(FlowCache flowCache, ProcessModelParser processModelParser) {
        if (null == processModelParser) {
            PROCESS_MODEL_PARSER = new DefaultProcessModelParser(flowCache);
        } else {
            PROCESS_MODEL_PARSER = processModelParser;
        }
    }

    /**
     * 检查并返回条件表达式
     */
    public FlowLongExpression checkFlowLongExpression() {
        Assert.isNull(flowLongExpression, "Interface FlowLongExpression not implemented");
        return this.flowLongExpression;
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

    @SuppressWarnings({"all"})
    public static String putAllVariable(String variable, Map<String, Object> args) {
        if (null != args && !args.isEmpty()) {
            if (null != variable) {
                Map<String, Object> varMap = fromJson(variable, Map.class);
                if (null != varMap) {
                    // 合并变量
                    return toJson(Stream.concat(varMap.entrySet().stream(), args.entrySet().stream())
                            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, ((e1, e2) -> e2))));
                }
            }
            return toJson(args);
        }
        return variable;
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
        return null != createTaskHandler ? createTaskHandler : SimpleCreateTaskHandler.getInstance();
    }

    /**
     * 获取创建流程任务处理器实现类
     *
     * @return {@link CreateTaskHandler}
     */
    public ConditionNodeHandler getFlowConditionHandler() {
        return null != conditionNodeHandler ? conditionNodeHandler : SimpleConditionNodeHandler.getInstance();
    }

    /**
     * 创建流程任务
     *
     * @param execution 执行对象
     * @param nodeModel 节点模型
     * @return true 执行成功  false 执行失败
     */
    public boolean createTask(Execution execution, NodeModel nodeModel) {
        // 拦截器前置处理
        if (null != taskCreateInterceptor) {
            taskCreateInterceptor.before(this, execution);
        }

        // 执行创建任务
        boolean result = this.getCreateTaskHandler().handle(this, execution, nodeModel);

        // 拦截器后置处理
        if (null != taskCreateInterceptor) {
            taskCreateInterceptor.after(this, execution);
        }
        return result;
    }

    /**
     * 默认初始化流程引擎上下文
     *
     * @param configEngine 流程配置引擎
     * @return {@link FlowLongEngine}
     */
    public FlowLongContext build(FlowLongEngine configEngine, boolean banner) {
        if (log.isInfoEnabled()) {
            log.info("FlowLongEngine start......");
        }
        /*
         * 由服务上下文返回流程引擎
         */
        Assert.isNull(configEngine, "Unable to discover implementation class for LongEngine");
        if (log.isInfoEnabled()) {
            log.info("FlowLongEngine be found {}", configEngine.getClass());
        }
        configEngine.configure(this);

        if (banner) {
            System.out.println("┌─┐┬  ┌─┐┬ ┬┬  ┌─┐┌┐┌┌─┐");
            System.out.println("├┤ │  │ │││││  │ │││││ ┬");
            System.out.println("└  ┴─┘└─┘└┴┘┴─┘└─┘┘└┘└─┘  1.1.15");
        }

        return this;
    }

}
