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
 * 测试会签流程
 *
 * @author 青苗
 */
@Slf4j
public class TestCountersign extends MysqlTest {

    @BeforeEach
    public void before() {
        processId = this.deployByResource("test/countersign.json", testCreator);
    }

    @Test
    public void test() {
        // 启动指定流程定义ID启动流程实例
        Map<String, Object> args = new HashMap<>();
        args.put("day", 8);
        args.put("assignee", testUser1);

        // 启动指定流程定义ID启动流程实例
        flowLongEngine.startInstanceById(processId, testCreator, args).ifPresent(instance -> {

            // 测试会签审批人001【审批】
            this.executeTask(instance.getId(), testCreator);

            // 执行任务跳转任意节点
            this.executeTask(instance.getId(), test3Creator, flwTask -> this.flowLongEngine.executeJumpTask(flwTask.getId(), "发起人", test3Creator));

            // 执行发起
            this.executeTask(instance.getId(), testCreator);

            // 测试会签审批人001【审批】
            this.executeTask(instance.getId(), testCreator);

            // 测试会签审批人003【审批】
            this.executeTask(instance.getId(), test3Creator);

            // 任务进入抄送人，流程自动结束

        });
    }
}
