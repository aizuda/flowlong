/* 
 * Copyright 2023-2025 Licensed under the AGPL License
 */
package test.mysql;

import com.flowlong.bpm.engine.QueryService;
import com.flowlong.bpm.engine.TaskService;
import com.flowlong.bpm.engine.entity.FlwHisTask;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;

/**
 * 测试简单流程
 *
 * @author xdg
 */
@Slf4j
public class TestPurchase extends MysqlTest {

    @BeforeEach
    public void before() {
        processId = this.deployByResource("test/purchase.json", testCreator);
    }

    @Test
    public void test() {
        // 启动指定流程定义ID启动流程实例
        flowLongEngine.startInstanceById(processId, testCreator).ifPresent(instance -> {

            // 领导审批
            this.executeActiveTasks(instance.getId(), testCreator);

            // 撤回任务（领导审批）
            QueryService queryService = flowLongEngine.queryService();
            List<FlwHisTask> hisTasks = queryService.getHisTasksByInstanceId(instance.getId()).get();
            FlwHisTask hisTask = hisTasks.get(0);
            TaskService taskService = flowLongEngine.taskService();
            taskService.withdrawTask(hisTask.getId(), testCreator);

            // 驳回任务（领导审批驳回，任务至发起人）
            this.executeActiveTasks(instance.getId(), t ->
                    taskService.rejectTask(t, testCreator, new HashMap<String, Object>() {{
                        put("reason", "不符合要求");
                    }})
            );

            // 执行当前任务并跳到【经理确认】节点
            this.executeActiveTasks(instance.getId(), t ->
                    flowLongEngine.executeJumpTask(t.getId(), "经理确认", testCreator)
            );

            // 经理确认，流程结束
            this.executeActiveTasks(instance.getId(), testCreator);
        });
    }
}
