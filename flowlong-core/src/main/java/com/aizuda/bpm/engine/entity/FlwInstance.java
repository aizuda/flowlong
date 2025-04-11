/*
 * Copyright 2023-2025 Licensed under the Dual Licensing
 * website: https://aizuda.com
 */
package com.aizuda.bpm.engine.entity;

import com.aizuda.bpm.engine.core.FlowLongContext;
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
public class FlwInstance extends FlowEntity {
    /**
     * 流程定义ID
     */
    protected Long processId;
    /**
     * 父流程实例ID
     */
    protected Long parentInstanceId;
    /**
     * 流程实例优先级
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

    public static FlwInstance of(String businessKey) {
        FlwInstance flwInstance = new FlwInstance();
        flwInstance.setBusinessKey(businessKey);
        return flwInstance;
    }

    @SuppressWarnings({"all"})
    public Map<String, Object> variableToMap() {
        Map<String, Object> map = FlowLongContext.fromJson(this.variable, Map.class);
        if (map == null) return new HashMap<>();
        return map;
    }

    public void setMapVariable(Map<String, Object> args) {
        this.variable = FlowLongContext.toJson(args);
    }
}
