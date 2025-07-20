/*
 * Copyright 2023-2025 Licensed under the Dual Licensing
 * website: https://aizuda.com
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

    /**
     * 测试用户4
     */
    protected String testUser4 = "test004";

    /**
     * 测试用户5
     */
    protected String testUser5 = "test005";

    /**
     * 测试用户6
     */
    protected String testUser6 = "test006";

    @Autowired
    protected FlowLongEngine flowLongEngine;

    protected Long deployByResource(String resourceName, FlowCreator flowCreator) {
        return flowLongEngine.processService().deployByResource(resourceName, flowCreator, false);
    }
}
