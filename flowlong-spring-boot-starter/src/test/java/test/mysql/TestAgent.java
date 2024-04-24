/*
 * Copyright 2023-2025 Licensed under the AGPL License
 */
package test.mysql;

import com.aizuda.bpm.engine.TaskService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * 测试简单流程
 *
 * @author xdg
 */
@Slf4j
public class TestAgent extends MysqlTest {

    @BeforeEach
    public void before() {
        processId = this.deployByResource("test/purchase.json", testCreator);
    }

    @Test
    public void test() {
        flowLongEngine.startInstanceById(processId, testCreator).ifPresent(instance -> {

            final TaskService taskService = flowLongEngine.taskService();

            // 领导审批指定代理人 test002
            executeActiveTasks(instance.getId(), flwTask -> taskService.agentTask(flwTask.getId(), testCreator, test2Creator));

            // 代理人 test002 完成任务
            executeActiveTasks(instance.getId(), test2Creator);

            // 领导审批，代理人历史任务清理，进入下一个节点
            executeActiveTasks(instance.getId(), testCreator);

        });
    }
}
