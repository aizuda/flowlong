/*
 * Copyright 2023-2025 Licensed under the Dual Licensing
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

            // 人事审批
            this.executeTaskByKey(instance.getId(), test2Creator, "k002");

            // 忽略查询时间执行定时器任务
            this.executeActiveTasks(instance.getId(), flwTask ->

                    // 模拟自动完成定时触发器任务
                    flowLongEngine.autoCompleteTask(flwTask.getId()));

            this.executeActiveTasks(instance.getId(), flwTask ->

                    // CEO审批拒绝，驳回会跳过 触发器 回到人事审批
                    flowLongEngine.executeRejectTask(flwTask, testCreator));

            this.executeActiveTasks(instance.getId(), flwTask ->

                    // 进入人事审批
                    Assertions.assertEquals("人事审批", flwTask.getTaskName()));
        });
    }
}
