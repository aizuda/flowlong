/*
 * Copyright 2023-2025 Licensed under the Dual Licensing
 * website: https://aizuda.com
 */
package test.mysql;

import com.aizuda.bpm.engine.FlowDataTransfer;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * 测试定时器任务
 */
@Slf4j
public class TestCreateTime extends MysqlTest {
    public static final String setFlowCreateTime = "setFlowCreateTime";

    @BeforeEach
    public void before() {
        processId = this.deployByResource("test/simpleProcess.json", testCreator);
    }

    @Test
    public void testCreateTime() {
        // 发起流程设置指定日期
        FlowDataTransfer.put(setFlowCreateTime, "2025-01-10");

        // 发起流程实例
        flowLongEngine.startInstanceById(processId, testCreator).ifPresent(instance -> {

            // 默认主管审批
            this.executeTask(instance.getId(), testCreator);

            // 条件路由子审批
            this.executeTask(instance.getId(), testCreator);

        });
    }
}
