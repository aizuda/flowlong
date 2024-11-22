/*
 * Copyright 2023-2025 Licensed under the apache-2.0 License
 * website: https://aizuda.com
 */
package test.mysql;

import com.aizuda.bpm.engine.entity.FlwHisInstance;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * 测试自动认领角色审批
 */
@Slf4j
public class TestAutoClaimRole extends MysqlTest {

    @BeforeEach
    public void before() {
        processId = this.deployByResource("test/testAutoClaimRole.json", testCreator);
    }

    @Test
    public void test() {
        flowLongEngine.startInstanceById(processId, testCreator).ifPresent(instance -> {

            // 先认领角色（假设 test02 为项目经理）
            this.executeActiveTasks(instance.getId(), t -> this.flowLongEngine.taskService().claimRole(t.getId(), test2Creator));

            // 执行任务
            executeActiveTasks(instance.getId(), test2Creator);

            FlwHisInstance hisInstance = flowLongEngine.queryService().getHistInstance(instance.getId());
            Assertions.assertEquals(1, hisInstance.getInstanceState());
        });
    }
}
