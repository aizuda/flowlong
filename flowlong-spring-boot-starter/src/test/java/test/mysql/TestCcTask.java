/*
 * Copyright 2023-2025 Licensed under the Dual Licensing
 * website: https://aizuda.com
 */
package test.mysql;

import com.aizuda.bpm.engine.model.NodeAssignee;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * 测试抄送节点跟条件分支
 */
@Slf4j
public class TestCcTask extends MysqlTest {

    @BeforeEach
    public void before() {
        processId = this.deployByResource("test/ccTask.json", testCreator);
    }

    @Test
    public void test() {
        flowLongEngine.startInstanceById(processId, testCreator).ifPresent(instance -> {

            this.executeActiveTasks(instance.getId(), flwTask -> {

                // 手动创建抄送任务
                List<NodeAssignee> ccUserList = new ArrayList<>();
                ccUserList.add(NodeAssignee.ofFlowCreator(test2Creator));
                ccUserList.add(NodeAssignee.ofFlowCreator(test3Creator));
                Assertions.assertTrue(flowLongEngine.createCcTask(flwTask, ccUserList, testCreator));

                // 执行节点
                flowLongEngine.executeTask(flwTask.getId(), testCreator);
            });
        });
    }
}
