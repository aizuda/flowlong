/*
 * Copyright 2023-2025 Licensed under the AGPL License
 */
package test.mysql;

import com.aizuda.bpm.engine.QueryService;
import com.aizuda.bpm.engine.TaskService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
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
            this.executeTask(instance.getId(), test3Creator, flwTask -> this.flowLongEngine.executeJumpTask(flwTask.getId(), "k001", test3Creator));

            // 执行发起
            this.executeActiveTasks(instance.getId(), testCreator, args);

            // 测试会签审批人003【转办，交给 002 审批】
            this.executeTask(instance.getId(), test3Creator, flwTask -> Assertions.assertTrue(this.flowLongEngine.taskService()
                    .transferTask(flwTask.getId(), test3Creator, test2Creator)));

            // 会签审批【转办 002 审批】
            this.executeTask(instance.getId(), test2Creator);


            // 测试会签审批人001【委派，交给 003 审批】
            this.executeTask(instance.getId(), testCreator, flwTask -> Assertions.assertTrue(this.flowLongEngine.taskService()
                    .delegateTask(flwTask.getId(), testCreator, test3Creator)));

            // 会签审批【委派 003 审批】解决任务后回到 001 确认审批
            this.executeTask(instance.getId(), test3Creator, flwTask -> Assertions.assertTrue(this.flowLongEngine.taskService()
                    .resolveTask(flwTask.getId(), test3Creator)));

            // 委派人 001 确认审批
            this.executeTask(instance.getId(), testCreator);

            final TaskService taskService = flowLongEngine.taskService();
            final QueryService queryService = flowLongEngine.queryService();

            // 部门经理确认驳回
            this.executeActiveTasks(instance.getId(), t ->
                    taskService.rejectTask(t, test2Creator, new HashMap<String, Object>() {{
                        put("reason", "不符合要求");
                    }})
            );

            // 会签审批 001 继续驳回
            this.executeTask(instance.getId(), testCreator, flwTask ->
                    taskService.rejectTask(flwTask, testCreator, new HashMap<String, Object>() {{
                        put("reason", "不符合要求（001驳回）");
                    }})
            );

            // 这里驳回到发起人
            queryService.getActiveTaskActorsByInstanceId(instance.getId()).ifPresent(flwTaskActors ->
                    Assertions.assertEquals(1, flwTaskActors.size()));

            // 执行发起
            this.executeActiveTasks(instance.getId(), testCreator, args);

            // 会签审批【转办 001 审批】
            this.executeTask(instance.getId(), testCreator);

            // 会签审批【转办 003 审批】
            this.executeTask(instance.getId(), test3Creator);

            // 部门经理确认驳回
            this.executeActiveTasks(instance.getId(), t ->
                    taskService.rejectTask(t, test2Creator, new HashMap<String, Object>() {{
                        put("reason", "不符合要求");
                    }})
            );

            // 测试 https://gitee.com/aizuda/flowlong/issues/I9HBJF 校验会签节点存在 2 个处理人
            queryService.getActiveTaskActorsByInstanceId(instance.getId()).ifPresent(flwTaskActors ->
                    Assertions.assertEquals(2, flwTaskActors.size()));

            // 校验一个会签节点自动完成的话，不创建下一个task
            queryService.getActiveTasksByInstanceId(instance.getId()).ifPresent(flwTasks -> {
                flowLongEngine.autoCompleteTask(flwTasks.get(0).getId());
            });

            queryService.getActiveTaskActorsByInstanceId(instance.getId()).ifPresent(flwTaskActors ->
                    Assertions.assertEquals(1, flwTaskActors.size())
            );

        });
    }
}
