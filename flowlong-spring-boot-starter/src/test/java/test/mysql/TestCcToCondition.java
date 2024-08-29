/*
 * Copyright 2023-2025 Licensed under the AGPL License
 */
package test.mysql;

import com.aizuda.bpm.engine.FlowDataTransfer;
import com.aizuda.bpm.engine.model.DynamicAssignee;
import com.aizuda.bpm.engine.model.NodeAssignee;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 测试抄送节点跟条件分支
 */
@Slf4j
public class TestCcToCondition extends MysqlTest {

    @BeforeEach
    public void before() {
        processId = this.deployByResource("test/ccToCondition.json", testCreator);
    }

    @Test
    public void test() {

        String ccNodeKey = "k002"; // 抄送
        List<NodeAssignee> assigneeList = Arrays.asList(
                // 动态抄送给：用户01、用户02、用户03
                NodeAssignee.ofFlowCreator(testCreator),
                NodeAssignee.ofFlowCreator(test2Creator)
        );

        // 传输动态节点处理人
        Map<String, Object> dataMap = new HashMap<String, Object>() {{
            put(ccNodeKey, DynamicAssignee.assigneeUserList(assigneeList));
        }};

        FlowDataTransfer.dynamicAssignee(dataMap);

        // 启动指定流程定义ID启动流程实例
        Map<String, Object> args = new HashMap<>();
        args.put("day", 5);
        flowLongEngine.startInstanceById(processId, testCreator, args).ifPresent(instance -> {

            // 领导审批，自动抄送，流程结束
            this.executeActiveTasks(instance.getId(), test2Creator);

        });
    }
}
