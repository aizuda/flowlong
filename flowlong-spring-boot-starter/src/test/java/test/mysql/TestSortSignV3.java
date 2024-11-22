/*
 * Copyright 2023-2025 Licensed under the apache-2.0 License
 * website: https://aizuda.com
 */
package test.mysql;

import com.aizuda.bpm.engine.core.FlowCreator;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * 测试按顺序依次审批流程
 *
 * @author 青苗
 */
@Slf4j
public class TestSortSignV3 extends MysqlTest {

    @BeforeEach
    public void before() {
        processId = this.deployByResource("test/sortSignV3.json", testCreator);
    }

    @Test
    public void test() {
        // 启动指定流程定义ID启动流程实例
        flowLongEngine.startInstanceById(processId, testCreator).ifPresent(instance -> {

            // test01 认领 role01 技术总监任务
            this.executeActiveTasks(instance.getId(), t -> this.flowLongEngine.taskService().claimRole(t.getId(), testCreator));

            // test01 执行任务
            executeActiveTasks(instance.getId(), testCreator);

            // test02 认领 role02 财务总监任务
            this.executeActiveTasks(instance.getId(), t -> this.flowLongEngine.taskService().claimRole(t.getId(), test2Creator));

            // test02 执行任务
            executeActiveTasks(instance.getId(), test2Creator);

        });
    }
}
