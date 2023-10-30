/* 
 * Copyright 2023-2025 Licensed under the AGPL License
 */
package test.mysql;

import com.flowlong.bpm.engine.TaskService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

/**
 * 测试或签流程
 *
 * @author 青苗
 */
@Slf4j
public class TestOrSign extends MysqlTest {

    @BeforeEach
    public void before() {
        processId = this.deployByResource("test/orSign.json", testCreator);
    }

    @Test
    public void test() {
        // 启动指定流程定义ID启动流程实例
        flowLongEngine.startInstanceById(processId, testCreator).ifPresent(instance -> {

            // 发起
            this.executeActiveTasks(instance.getId(), testCreator);

            // test1 驳回任务（领导审批驳回，任务至发起人）
            TaskService taskService = flowLongEngine.taskService();
            this.executeActiveTasks(instance.getId(), t ->
                    taskService.rejectTask(t, testCreator, new HashMap<String, Object>() {{
                        put("reason", "不符合要求");
                    }})
            );

            // 调整申请内容，重新提交审批
            this.executeActiveTasks(instance.getId(), t ->
                    flowLongEngine.executeTask(t.getId(), testCreator)
            );

            // test3 领导审批同意
            this.executeActiveTasks(instance.getId(), test3Creator);

            // 抄送人力资源，流程自动结束

        });
    }
}
