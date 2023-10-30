/* 
 * Copyright 2023-2025 Licensed under the AGPL License
 */
package test.mysql;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * 测试按顺序依次审批流程
 *
 * @author 青苗
 */
@Slf4j
public class TestSortSign extends MysqlTest {

    @BeforeEach
    public void before() {
        processId = this.deployByResource("test/sortSign.json", testCreator);
    }

    @Test
    public void test() {
        // 启动指定流程定义ID启动流程实例
        flowLongEngine.startInstanceById(processId, testCreator).ifPresent(instance -> {

            // 发起
            this.executeActiveTasks(instance.getId(), testCreator);

            // test1 领导审批同意
            this.executeActiveTasks(instance.getId(), testCreator);

            // test3 领导审批同意
            this.executeActiveTasks(instance.getId(), test3Creator);

            // 抄送人力资源，流程自动结束

        });
    }
}
