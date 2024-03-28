/*
 * Copyright 2023-2025 Licensed under the AGPL License
 */
package com.flowlong.bpm.engine;

/**
 * JSON BPM 常量类
 *
 * <p>
 * 尊重知识产权，不允许非法使用，后果自负
 * </p>
 *
 * @author hubin
 * @since 1.0
 */
public interface FlowConstants {

    /**
     * 流程定义缓存KEY
     */
    String processCacheKey = "flwProcessModel#";

    /**
     * 流程实例缓存KEY
     */
    String processInstanceCacheKey = "flwProcessInstanceModel#";

    /**
     * 流程节点动态分配节点处理人或角色
     */
    String processDynamicAssignee = "flwProcessDynamicAssignee";

}
