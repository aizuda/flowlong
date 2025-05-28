/*
 * Copyright 2023-2025 Licensed under the Dual Licensing
 * website: https://aizuda.com
 */
package test.mysql;

import com.aizuda.bpm.engine.core.FlowCreator;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
        FlowCreator flowCreator = FlowCreator.of(testUser1, "青苗");

        // 启动指定流程定义ID启动流程实例
        flowLongEngine.startInstanceById(processId, testCreator).ifPresent(instance -> {

            // 驳回任务
            this.executeTask(instance.getId(), flowCreator, flwTask ->
                    flowLongEngine.executeRejectTask(flwTask, flowCreator, null));

            // 发起人继续审批
            this.executeTask(instance.getId(), testCreator);

            // test1 领导审批同意
            this.executeTask(instance.getId(), flowCreator);

            // test3 领导审批同意
            this.executeTask(instance.getId(), FlowCreator.of(testUser3, "聂秋秋"));

            // test2 不在执行达到票签值
            // 抄送人力资源，流程自动结束

        });
    }
}
