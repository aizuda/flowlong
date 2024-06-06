/*
 * Copyright 2023-2025 Licensed under the AGPL License
 */
package test;

import com.aizuda.bpm.engine.FlowLongEngine;
import com.aizuda.bpm.engine.core.FlowCreator;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 测试流程引擎抽象类
 */
public abstract class TestFlowLong {
    /**
     * 流程定义ID
     */
    protected Long processId;
    /**
     * 测试用户1
     */
    protected String testUser1 = "test001";
    /**
     * 测试用户2
     */
    protected String testUser2 = "test002";

    /**
     * 测试用户3
     */
    protected String testUser3 = "test003";

    @Autowired
    protected FlowLongEngine flowLongEngine;

    protected Long deployByResource(String resourceName, FlowCreator flowCreator) {
        return flowLongEngine.processService().deployByResource(resourceName, flowCreator, false);
    }
}
