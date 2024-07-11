/*
 * Copyright 2023-2025 Licensed under the AGPL License
 */
package test.mysql;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * 测试定时器任务
 */
@Slf4j
public class TestTimer extends MysqlTest {

    @BeforeEach
    public void before() {
        processId = this.deployByResource("test/testTimer.json", testCreator);
    }

    @Test
    public void test() {
        flowLongEngine.startInstanceById(processId, testCreator).ifPresent(instance -> {

            // 忽略查询时间执行定时器任务
            this.executeActiveTasks(instance.getId(), flwTask ->

                    // 模拟自动完成定时任务
                    flowLongEngine.autoCompleteTask(flwTask.getId()));

            this.executeActiveTasks(instance.getId(), flwTask -> {
                        // 进入人事审批
                        Assertions.assertEquals("人事审批", flwTask.getTaskName());

                        // 模拟超时，自动完成任务
                        flowLongEngine.autoCompleteTask(flwTask.getId());
                    });

            this.executeActiveTasks(instance.getId(), flwTask -> {
                        // 进入经理审批
                        Assertions.assertEquals("经理审批", flwTask.getTaskName());

                        // 模拟超时，自动完成任务
                        flowLongEngine.autoCompleteTask(flwTask.getId());
                    });
        });
    }
}
