/*
 * Copyright 2023-2025 Licensed under the apache-2.0 License
 * website: https://aizuda.com
 */
package com.aizuda.bpm.engine;

/**
 * JSON BPM 常量类
 *
 * <p>
 * <a href="https://aizuda.com">官网</a>尊重知识产权，不允许非法使用，后果自负
 * </p>
 *
 * @author hubin
 * @since 1.0
 */
public interface FlowConstants {

    /**
     * 流程定义缓存 KEY
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

    /**
     * 流程指定条件节点 KEY
     */
    String processSpecifyConditionNodeKey = "flwProcessSpecifyConditionNodeKey";

}
