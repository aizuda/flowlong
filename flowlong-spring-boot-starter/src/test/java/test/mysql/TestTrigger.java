/*
 * Copyright 2023-2025 Licensed under the AGPL License
 */
package test.mysql;

import lombok.extern.slf4j.Slf4j;
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

            // TODO 测试触发器任务

        });
    }
}
