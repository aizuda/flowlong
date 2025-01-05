/*
 * Copyright 2023-2025 Licensed under the Dual Licensing
 * website: https://aizuda.com
 */
package test.mysql;

import com.aizuda.bpm.engine.FlowDataTransfer;
import com.aizuda.bpm.engine.core.FlowCreator;
import com.aizuda.bpm.engine.model.DynamicAssignee;
import com.aizuda.bpm.engine.model.NodeAssignee;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * 重新加载模型测试类
 *
 * <p>
 * <a href="https://aizuda.com">官网</a>尊重知识产权，不允许非法使用，后果自负
 * </p>
 *
 * @author hubin
 * @since 1.0
 */
public class TestReloadProcessModel extends MysqlTest {

    @BeforeEach
    public void before() {
        processId = this.deployByResource("test/reloadProcessModel.json", testCreator);
    }

    @Test
    public void test() {
        flowLongEngine.startInstanceById(processId, testCreator).ifPresent(instance -> {

            // 作执行任务，动态设置下一步节点处理人信息
            this.dynamicAssigneeAndExecuteTask(instance.getId(), "flk1735880993544", test2Creator, testCreator);

            this.dynamicAssigneeAndExecuteTask(instance.getId(), "flk1735881014635", test3Creator, test2Creator);

            // 审核人3 执行任务
            this.executeTask(instance.getId(), test3Creator);
        });
    }

    private void dynamicAssigneeAndExecuteTask(Long instanceId, String nodeKey, FlowCreator flowCreator, FlowCreator taskCreator) {
        // 传递动态分配处理人员
        Map<String, Object> assigneeMap = new HashMap<>();
        DynamicAssignee dynamicAssignee = DynamicAssignee.builder();
        dynamicAssignee.setType(1);
        NodeAssignee nodeAssignee = new NodeAssignee();
        nodeAssignee.setId(flowCreator.getCreateId());
        nodeAssignee.setName(flowCreator.getCreateBy());
        dynamicAssignee.setAssigneeList(Collections.singletonList(nodeAssignee));
        assigneeMap.put(nodeKey, dynamicAssignee);
        FlowDataTransfer.dynamicAssignee(assigneeMap);

        // 执行当前任务
        this.executeTask(instanceId, taskCreator);
    }
}
