/*
 * Copyright 2023-2025 Licensed under the Dual Licensing
 * website: https://aizuda.com
 */
package test.mysql;

import com.aizuda.bpm.engine.core.FlowCreator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * 测试并行触发器
 */
public class TestParallelTrigger extends MysqlTest {

    @BeforeEach
    public void before() {
        processId = this.deployByResource("test/parallelTrigger.json", getFlowCreator());
    }

    @Test
    public void testProcess() {
        flowLongEngine.startInstanceById(processId, testCreator).ifPresent(instance -> {

        });
    }


    public FlowCreator getFlowCreator() {
        return testCreator;
    }
}
