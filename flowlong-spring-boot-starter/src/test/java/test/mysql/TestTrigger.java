/*
 * Copyright 2023-2025 Licensed under the apache-2.0 License
 * website: https://aizuda.com
 */
package test.mysql;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * 测试触发器任务
 */
@Slf4j
public class TestTrigger extends MysqlTest {

    @BeforeEach
    public void before() {
        processId = this.deployByResource("test/testTrigger.json", testCreator);
    }

    @Test
    public void test() {
        flowLongEngine.startInstanceById(processId, testCreator).ifPresent(instance -> {

            // 忽略查询时间执行定时器任务
            this.executeActiveTasks(instance.getId(), flwTask ->

                    // 模拟自动完成定时触发器任务
                    flowLongEngine.autoCompleteTask(flwTask.getId()));

            this.executeActiveTasks(instance.getId(), flwTask ->

                    // 进入人事审批
                    Assertions.assertEquals("人事审批", flwTask.getTaskName()));
        });
    }
}
