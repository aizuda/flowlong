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
 * 测试自动跳转
 */
@Slf4j
public class TestAutoSkip extends MysqlTest {

    @BeforeEach
    public void before() {
        processId = this.deployByResource("test/autoSkip.json", testCreator);
    }

    @Test
    public void test() {
        // 考勤管理员【角色】不走认领逻辑，通过任务参与者提供类直接分配处理人员
        flowLongEngine.startInstanceById(processId, testCreator).ifPresent(instance -> {

            // 非自动完成 test002 操作执行任务
            executeActiveTasks(instance.getId(), test2Creator);

            FlwHisInstance hisInstance = flowLongEngine.queryService().getHistInstance(instance.getId());
            Assertions.assertEquals(1, hisInstance.getInstanceState());
        });
    }
}
