/*
 * Copyright 2023-2025 Licensed under the AGPL License
 */
package test.mysql;

import com.flowlong.bpm.engine.assist.StreamUtils;
import com.flowlong.bpm.engine.core.FlowLongContext;
import com.flowlong.bpm.engine.model.ModelHelper;
import com.flowlong.bpm.engine.model.NodeAssignee;
import com.flowlong.bpm.engine.model.NodeModel;
import com.flowlong.bpm.engine.model.ProcessModel;
import com.flowlong.bpm.spring.adaptive.FlowJacksonHandler;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 流程模型相关测试类
 *
 * <p>
 * 尊重知识产权，不允许非法使用，后果自负
 * </p>
 *
 * @author hubin
 * @since 1.0
 */
public class TestModel extends MysqlTest {

    public ProcessModel getProcessModel(String name) {
        try {
            String modeContent = StreamUtils.readBytes(StreamUtils.getResourceAsStream(name));
            FlowLongContext.setFlowJsonHandler(new FlowJacksonHandler());
            return FlowLongContext.fromJson(modeContent, ProcessModel.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 测试获取节点 Map 格式列表
     */
    @Test
    public void testNodeMapList() {
        ProcessModel processModel = getProcessModel("test/simpleProcess.json");
        Assertions.assertEquals("simpleProcess", processModel.getKey());
        List<Map<String, Object>> nodeMapList = ModelHelper.getNodeMapList(processModel.getNodeConfig(), ((nodeMap, nodeModel) -> {
            nodeMap.put("termAuto", nodeModel.getTermAuto());
            nodeMap.put("term", nodeModel.getTerm());
            nodeMap.put("termMode", nodeModel.getTermMode());
        }));
        Assertions.assertEquals(nodeMapList.get(1).get("conditionNode"), 1);
    }

    /**
     * 测试动态加签
     */
    @Test
    public void testAddNode() {
        processId = this.deployByResource("test/simpleProcess.json", testCreator);
        // 启动指定流程定义ID启动流程实例
        Map<String, Object> args = new HashMap<>();
        args.put("day", 8);
        args.put("age", 18);
        args.put("assignee", testUser1);

        // 启动指定流程定义ID启动流程实例
        flowLongEngine.startInstanceById(processId, testCreator, args).ifPresent(instance -> {

            // 测试会签审批人001【审批】，执行前置加签
            this.executeTask(instance.getId(), testCreator, flwTask -> flowLongEngine.executeAppendNodeModel(flwTask.getId(),
                    getNodeModel("前置加签"), testCreator, true));

            // 测试会签审批人003【审批】，执行后置加签
//            this.executeTask(instance.getId(), test3Creator, flwTask -> flowLongEngine.executeAppendNodeModel(flwTask.getId(),
//                    getNodeModel("后置加签"), test3Creator, false));

            // 观察 flw_ext_instance 对应实例的模型变化

        });
    }

    /**
     * 构建加签节点
     */
    public NodeModel getNodeModel(String nodeName) {
        NodeModel nodeModel = new NodeModel();
        nodeModel.setNodeName(nodeName);
        nodeModel.setType(1);
        nodeModel.setSetType(1);
        NodeAssignee nodeAssignee = new NodeAssignee();
        nodeAssignee.setId(test3Creator.getCreateId());
        nodeAssignee.setName(test3Creator.getCreateBy());
        nodeModel.setNodeUserList(Collections.singletonList(nodeAssignee));
        return nodeModel;
    }
}
