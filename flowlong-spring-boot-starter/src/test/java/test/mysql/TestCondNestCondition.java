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
 * 测试条件分支嵌套
 */
@Slf4j
public class TestCondNestCondition extends MysqlTest {

    @BeforeEach
    public void before() {
        processId = this.deployByResource("test/condNestCondition.json", testCreator);
    }

    @Test
    public void test() {

        // 启动指定流程定义ID启动流程实例
        flowLongEngine.startInstanceById(processId, testCreator).ifPresent(instance -> {

            // 发起，执行连续条件分支嵌套
            Map<String, Object> args = new HashMap<>();
            args.put("day", 11);
            this.executeActiveTasks(instance.getId(), testCreator, args);

            // 人事审批
            this.executeActiveTasks(instance.getId(), testCreator);

            // 领导审批，自动抄送，流程结束
            this.executeActiveTasks(instance.getId(), test3Creator);

        });
    }
}
