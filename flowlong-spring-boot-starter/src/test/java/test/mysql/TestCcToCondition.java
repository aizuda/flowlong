/*
 * Copyright 2023-2025 Licensed under the AGPL License
 */
package test.mysql;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * 测试抄送节点跟条件分支
 */
@Slf4j
public class TestCcToCondition extends MysqlTest {

    @BeforeEach
    public void before() {
        processId = this.deployByResource("test/ccToCondition.json", testCreator);
    }

    @Test
    public void test() {

        // 启动指定流程定义ID启动流程实例
        Map<String, Object> args = new HashMap<>();
        args.put("day", 8);
        flowLongEngine.startInstanceById(processId, testCreator, args).ifPresent(instance -> {

            // 领导审批，自动抄送，流程结束
            this.executeActiveTasks(instance.getId(), test3Creator);

        });
    }
}
