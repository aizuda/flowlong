/*
 * Copyright 2023-2025 Licensed under the Dual Licensing
 * website: https://aizuda.com
 */
package test.mysql;

import com.aizuda.bpm.engine.TaskService;
import com.aizuda.bpm.engine.exception.FlowLongException;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

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

                // 并行任务不允许拿回执行异常
                Assertions.assertThrows(FlowLongException.class, new Executable() {

                    @Override
                    public void execute() throws Throwable {
                        taskService.reclaimTask(flwTask.getId(), testCreator);
                    }
                });
            });

            // 会签全部审批完成
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
