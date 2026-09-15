/*
 * Copyright 2023-2025 Licensed under the Dual Licensing
 * website: https://aizuda.com
 */
package com.aizuda.bpm.engine.entity;

import com.aizuda.bpm.engine.core.FlowLongContext;
import com.aizuda.bpm.engine.core.enums.InstancePriority;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 流程实例实体类
 *
 * <p>
 * <a href="https://aizuda.com">官网</a>尊重知识产权，不允许非法使用，后果自负
 * </p>
 *
 * @author hubin
 * @since 1.0
 */
@Getter
@Setter
@ToString
public class FlwInstance extends FlowEntity implements Cloneable {
    /**
     * 流程定义ID
     */
    protected Long processId;
    /**
     * 父流程实例ID
     */
    protected Long parentInstanceId;
    /**
     * 流程实例优先级 0，同步 1，异步
     */
    protected Integer priority;
    /**
     * 流程实例编号
     */
    protected String instanceNo;
    /**
     * 业务KEY（用于关联业务逻辑实现预留）
     *
     * <p>
     * 子流程情况，该字段用于存放父流程所在节点KEY
     * </p>
     */
    protected String businessKey;
    /**
     * 变量json
     */
    protected String variable;
    /**
     * 当前所在节点名称
     */
    protected String currentNodeName;
    /**
     * 当前所在节点key
     */
    protected String currentNodeKey;
    /**
     * 流程实例期望完成时间
     */
    protected Date expireTime;
    /**
     * 流程实例上一次更新人
     */
    protected String lastUpdateBy;
    /**
     * 流程实例上一次更新时间
     */
    protected Date lastUpdateTime;
    /**
     * 流程实例紧急程度 0，常规 1，紧急且重要 2，重要不紧急 3，紧急不重要
     * <p>
     * 0（常规）：日常例行工作，按排期走。
     * </p>
     * <p>
     * 1（紧急且重要）：系统故障、线上事故、阻塞他人的任务，必须立即处理。
     * </p>
     * <p>
     * 2（重要不紧急）：核心业务需求、关键节点任务，当天或当班内完成。
     * </p>
     * <p>
     * 3（紧急不重要）：临时插进来的琐事、他人催办但影响面小的事，可批量集中处理。
     * </p>
     */
    protected Integer urgent;

    public FlwInstance() {
        // 默认优先级 0
        this.priority = 0;
        // 日常例行工作 0
        this.urgent = 0;
    }

    public static FlwInstance of(String businessKey) {
        FlwInstance flwInstance = new FlwInstance();
        flwInstance.setBusinessKey(businessKey);
        return flwInstance;
    }

    public void priority(InstancePriority instancePriority) {
        this.priority = instancePriority.getValue();
    }

    @SuppressWarnings({"all"})
    public Map<String, Object> variableToMap() {
        Map<String, Object> map = FlowLongContext.fromJson(this.variable, Map.class);
        return null != map ? map : new HashMap<>();
    }

    public void putAllVariable(Map<String, Object> args) {
        this.variable = FlowLongContext.putAllVariable(this.variable, args);
    }

    @Override
    public FlwInstance clone() {
        try {
            return (FlwInstance) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
