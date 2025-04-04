/*
 * Copyright 2023-2025 Licensed under the Dual Licensing
 * website: https://aizuda.com
 */
package test.mysql;

import com.aizuda.bpm.engine.TaskService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * 测试简单流程
 *
 * @author ming
 */
@Slf4j
class TestSimpleProcess extends MysqlTest {

    @BeforeEach
    public void before() {
        processId = this.deployByResource("test/simpleProcess.json", testCreator);
    }

    @Test
    void testStartAsDraft() {
        // 测试启动为草稿
        flowLongEngine.startInstanceById(processId, testCreator, true).ifPresent(instance -> {

            // 停留在发起人节点
            this.executeActiveTasks(instance.getId(), t -> Assertions.assertEquals("发起人", t.getTaskName()));
        });
    }

    @Test
    void test() {
        // 启动指定流程定义ID启动流程实例
        Map<String, Object> args = new HashMap<>();
        args.put("day", 8);
        args.put("age", 18);
        args.put("assignee", testUser1);

        // 启动指定流程定义ID启动流程实例
        flowLongEngine.startInstanceById(processId, testCreator, args).ifPresent(instance -> {

            // 测试会签审批人001【审批】
            this.executeTask(instance.getId(), testCreator);

            // 延迟下一会签任务完成时间
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                // to do noting
            }

            // 测试会签审批人003【审批】
            this.executeActiveTasks(instance.getId(), test3Creator, args);

            // 拿回任务(条件路由子审批) 回到测试会签审批人003【审批】任务
            TaskService taskService = flowLongEngine.taskService();
            this.executeActiveTasks(instance.getId(), t -> taskService.reclaimTask(t.getParentTaskId(), testCreator));

            // 测试会签审批人003【审批】
            this.executeTask(instance.getId(), test3Creator, args);

            // 年龄审批【审批】
            this.executeTask(instance.getId(), testCreator);

            // 条件内部审核【审批】
            this.executeTask(instance.getId(), testCreator);

            // 条件路由子审批【审批】 抄送 结束
            this.executeTask(instance.getId(), testCreator);

        });
    }
}
