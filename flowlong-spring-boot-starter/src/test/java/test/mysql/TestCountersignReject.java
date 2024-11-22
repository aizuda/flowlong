/*
 * Copyright 2023-2025 Licensed under the apache-2.0 License
 * website: https://aizuda.com
 */
package test.mysql;

import com.aizuda.bpm.engine.TaskService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * 测试会签流程驳回测试
 */
@Slf4j
public class TestCountersignReject extends MysqlTest {

    @BeforeEach
    public void before() {
        processId = this.deployByResource("test/countersign.json", testCreator);
    }

    // 启动参数
    private Map<String, Object> getArgs() {
        Map<String, Object> args = new HashMap<>();
        args.put("day", 8);
        args.put("assignee", testUser1);
        return args;
    }

    @Test
    public void test() {
        final TaskService taskService = flowLongEngine.taskService();

        // 启动指定流程定义ID启动流程实例
        flowLongEngine.startInstanceById(processId, testCreator, this.getArgs()).ifPresent(instance -> {

            // 测试会签审批人001【审批】
            this.executeTask(instance.getId(), testCreator, flwTask -> {
                // 执行审批
                this.flowLongEngine.executeTask(flwTask.getId(), testCreator);

                // 执行拿回
                taskService.reclaimTask(flwTask.getId(), testCreator);
            });

            // 会签全部审批完成
            this.executeTask(instance.getId(), testCreator);
            this.executeTask(instance.getId(), test3Creator);

            // 拒绝审批
            this.executeTask(instance.getId(), test2Creator, flwTask -> {

                // 驳回至会签
                taskService.rejectTask(flwTask, test2Creator);
            });
        });
    }

    @Test
    public void test2() {
        final TaskService taskService = flowLongEngine.taskService();

        // 启动指定流程定义ID启动流程实例
        flowLongEngine.startInstanceById(processId, testCreator, this.getArgs()).ifPresent(instance -> {

            // 测试会签审批人001【审批】
            this.executeTask(instance.getId(), testCreator, flwTask -> {

                // 会签其中一个用户驳回
                taskService.rejectTask(flwTask, testCreator);
            });
        });
    }
}
