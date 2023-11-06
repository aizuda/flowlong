/* 
 * Copyright 2023-2025 Licensed under the AGPL License
 */
package test.mysql;

import com.flowlong.bpm.engine.TaskService;
import com.flowlong.bpm.engine.core.FlowCreator;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

/**
 * 测试票签流程
 *
 * @author 青苗
 */
@Slf4j
public class TestVoteSign extends MysqlTest {

    @BeforeEach
    public void before() {
        processId = this.deployByResource("test/voteSign.json", testCreator);
    }

    @Test
    public void test() {
        // 启动指定流程定义ID启动流程实例
        flowLongEngine.startInstanceById(processId, testCreator).ifPresent(instance -> {

            // 发起
            this.executeActiveTasks(instance.getId(), testCreator);

            // test1 领导审批同意
            this.executeActiveTasks(instance.getId(), FlowCreator.of(testUser1, "青苗"));

            // test3 领导审批同意
            this.executeActiveTasks(instance.getId(), FlowCreator.of(testUser3, "聂秋秋"));

            // test2 不在执行达到票签值
            // 抄送人力资源，流程自动结束

        });
    }
}
