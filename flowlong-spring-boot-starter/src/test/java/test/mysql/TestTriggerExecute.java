/*
 * Copyright 2023-2025 Licensed under the Dual Licensing
 * website: https://aizuda.com
 */
package test.mysql;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

/**
 * 测试立即执行触发器
 */
@Slf4j
public class TestTriggerExecute extends MysqlTest {

    @BeforeEach
    public void before() {
        processId = this.deployByResource("test/testTriggerExecute.json", testCreator);
    }

    @Test
    public void test() {
        // 设置 spring 上下文
        SpringHelper.setApplicationContext(applicationContext);

        // 启动流程实例
        flowLongEngine.startInstanceById(processId, testCreator).ifPresent(instance -> {

            // 发起人跳到触发器（立即执行）
            this.executeActiveTasks(instance.getId(), flwTask ->
                    flowLongEngine.executeJumpTask(flwTask.getId(), "cf001", test2Creator));

            // 进入CEO审批
            this.executeActiveTasks(instance.getId(), flwTask -> Assertions.assertEquals("k003", flwTask.getTaskKey()));
        });
    }
}
