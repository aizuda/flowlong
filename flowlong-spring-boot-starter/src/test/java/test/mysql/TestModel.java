/*
 * Copyright 2023-2025 Licensed under the apache-2.0 License
 * website: https://aizuda.com
 */
package test.mysql;

import com.aizuda.bpm.engine.FlowDataTransfer;
import com.aizuda.bpm.engine.assist.StreamUtils;
import com.aizuda.bpm.engine.core.Execution;
import com.aizuda.bpm.engine.core.FlowCreator;
import com.aizuda.bpm.engine.core.FlowLongContext;
import com.aizuda.bpm.engine.model.*;
import com.aizuda.bpm.spring.adaptive.FlowJacksonHandler;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.*;

/**
 * 流程模型相关测试类
 *
 * <p>
 * <a href="https://aizuda.com">官网</a>尊重知识产权，不允许非法使用，后果自负
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

    @Test
    public void testProcessModel2json() throws Exception {
        FlowLongContext.setFlowJsonHandler(new FlowJacksonHandler());

        // 测试子流程
        this.testModel2json("test/subProcess.json", "测试子流程");

        // 测试简单流程
        this.testModel2json("test/simpleProcess.json", "测试简单流程");

        // 测试条件分支
        this.testModel2json("test/ccToCondition.json", "抄送节点跟条件分支");

        // 测试条件分支嵌套
        this.testModel2json("test/condNestCondition.json", "条件分支嵌套");

        // 测试并行分支
        this.testModel2json("test/parallelProcess.json", "测试并行分支");

        // 测试包容分支
        this.testModel2json("test/inclusiveProcess.json", "测试包容分支");
    }

    private void testModel2json(String jsonFile, String modelName) throws Exception {
        String jsonContent = StreamUtils.readBytes(StreamUtils.getResourceAsStream(jsonFile));
        ProcessModel processModel = FlowLongContext.fromJson(jsonContent, ProcessModel.class);
        Assertions.assertEquals(processModel.getName(), modelName);
        System.err.println(FlowLongContext.toJson(processModel.cleanParentNode()));
    }

    /**
     * 测试获取节点 Map 格式列表
     */
    @Test
    public void testProcessModel() {
        ProcessModel processModel = getProcessModel("test/simpleProcess.json");
        Assertions.assertEquals("simpleProcess", processModel.getKey());
        NodeModel rootNode = processModel.getNodeConfig();
        Assertions.assertEquals(rootNode.getNodeName(), "发起人");
        List<NodeAssignee> nodeAssigneeList = rootNode.getNodeAssigneeList();
        Assertions.assertEquals(2, nodeAssigneeList.size());
    }

    /**
     * 测试模型节点名称
     */
    @Test
    public void testNodeNames() {
        ProcessModel processModel = getProcessModel("test/simpleProcess.json");
        Assertions.assertEquals("simpleProcess", processModel.getKey());
        processModel.buildParentNode(processModel.getNodeConfig());
        List<String> previousNodeNames = ModelHelper.getAllPreviousNodeKeys(processModel.getNode("k012")); // 条件内部审核
        Assertions.assertEquals(previousNodeNames.size(), 4);

        NodeModel rootNodeModel = processModel.getNodeConfig();
        Assertions.assertFalse(ModelHelper.checkDuplicateNodeKeys(rootNodeModel));
        Assertions.assertEquals(ModelHelper.checkConditionNode(rootNodeModel), 0);
    }

    /**
     * 测试检查是否存在重复节点名称
     */
    @Test
    public void testCheckDuplicateNodeNames() {
        ProcessModel processModel = getProcessModel("test/duplicateNodeNames.json");
        Assertions.assertTrue(ModelHelper.checkDuplicateNodeKeys(processModel.getNodeConfig()));
    }

    /**
     * 测试获取所有未设置处理人员节点
     */
    @Test
    public void testGetUnsetAssigneeNodeKeys() {
        ProcessModel processModel = getProcessModel("test/unsetAssigneeNodes.json");
        List<NodeModel> NodeModels = ModelHelper.getUnsetAssigneeNodes(processModel.getNodeConfig());
        Assertions.assertEquals(NodeModels.size(), 2);
    }

    /**
     * 测试动态追加处理人
     */
    @Test
    public void testDynamicAssignee() {
        processId = this.deployByResource("test/purchase.json", testCreator);
        String nodeName = "k002"; // 抄送主管
        String nodeName2 = "k003"; // 领导审批
        List<NodeAssignee> assigneeList = Arrays.asList(
                // 动态抄送给：用户01、用户02、用户03
                NodeAssignee.ofFlowCreator(testCreator),
                NodeAssignee.ofFlowCreator(test2Creator),
                NodeAssignee.ofFlowCreator(test3Creator)
        );
        // 传输动态节点处理人
        FlowDataTransfer.dynamicAssignee(new HashMap<String, Object>() {{
            put(nodeName, DynamicAssignee.assigneeUserList(assigneeList));
            put(nodeName2, DynamicAssignee.assigneeUserList(Collections.singletonList(NodeAssignee.ofFlowCreator(test3Creator))));
        }});
        // 发起流程验证
        flowLongEngine.startInstanceById(processId, testCreator).ifPresent(instance -> {
            ProcessModel processModel = flowLongEngine.runtimeService().getProcessModelByInstanceId(instance.getId());
            List<NodeAssignee> nodeUserList = processModel.getNode(nodeName).getNodeAssigneeList();
            Assertions.assertEquals(3, nodeUserList.size());
            nodeUserList.forEach(t -> Assertions.assertTrue(assigneeList.stream().anyMatch(s -> s.getName().equals(t.getName()))));
            Assertions.assertTrue(processModel.getNode(nodeName).getNodeAssigneeList().stream().anyMatch(t -> t.getName().equals(test3Creator.getCreateBy())));
        });
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
                    getNodeModel("前置加签", "qzjq01", test3Creator), testCreator, true));

            // 执行前加签
            this.executeTask(instance.getId(), test3Creator);

            // 测试会签审批人003【审批】，执行后置加签
            this.executeTask(instance.getId(), test3Creator, flwTask -> flowLongEngine.executeAppendNodeModel(flwTask.getId(),
                    getNodeModel("后置加签", "hzjq01", test2Creator), test3Creator, false));

            // 会签审批人001【审批】，执行转办、任务交给 test2 处理
            this.executeTask(instance.getId(), testCreator, flwTask -> flowLongEngine.taskService()
                    .transferTask(flwTask.getId(), testCreator, test2Creator));

            // 被转办人 test2 审批
            this.executeTask(instance.getId(), test2Creator);

            // 会签审批人003【审批】，执行委派、任务委派给 test2 处理
            this.executeTask(instance.getId(), test3Creator, flwTask -> flowLongEngine.taskService()
                    .delegateTask(flwTask.getId(), test3Creator, test2Creator));

            // 被委派人 test2 解决问题，后归还任务给委派人
            this.executeTask(instance.getId(), test2Creator, flwTask -> flowLongEngine.taskService()
                    .resolveTask(flwTask.getId(), test2Creator));

            // 委派人 test3 执行完成任务
            this.executeTask(instance.getId(), test3Creator);

            // 执行后加签
            this.executeTask(instance.getId(), test2Creator);

            // 观察 flw_ext_instance 对应实例的模型变化

        });
    }

    /**
     * 构建加签节点
     */
    public NodeModel getNodeModel(String nodeName, String nodeKey, FlowCreator flowCreator) {
        NodeModel nodeModel = new NodeModel();
        nodeModel.setNodeName(nodeName);
        nodeModel.setNodeKey(nodeKey);
        nodeModel.setType(1);
        nodeModel.setSetType(1);
        nodeModel.setExamineMode(1);
        NodeAssignee nodeAssignee = new NodeAssignee();
        nodeAssignee.setId(flowCreator.getCreateId());
        nodeAssignee.setName(flowCreator.getCreateBy());
        nodeModel.setNodeAssigneeList(Collections.singletonList(nodeAssignee));
        return nodeModel;
    }

    /**
     * 测试错误模型
     */
    @Test
    public void errorModel01() {
        ProcessModel processModel = getProcessModel("test/simpleProcess.json");
        Assertions.assertTrue(ModelHelper.checkExistApprovalNode(processModel.getNodeConfig()));
        ProcessModel errorModel01 = getProcessModel("test/errorModel01.json");
        Assertions.assertFalse(ModelHelper.checkExistApprovalNode(errorModel01.getNodeConfig()));
    }

    /**
     * 测试获取当前已使用的节点key列表
     */
    @Test
    public void testCurrentUsedNodeKeys() {
        ProcessModel processModel = getProcessModel("test/ccToCondition.json");

        Assertions.assertEquals(2, ModelHelper.getAllUsedNodeKeys(flowLongEngine.getContext(), new Execution(testCreator, null),
                processModel.getNodeConfig(), "k002").size());

        Assertions.assertEquals(4, ModelHelper.getAllUsedNodeKeys(flowLongEngine.getContext(), new Execution(testCreator, new HashMap<String, Object>() {{
            put("day", 3);
        }}), processModel.getNodeConfig(), "k007").size());

        Assertions.assertEquals(4, ModelHelper.getAllUsedNodeKeys(flowLongEngine.getContext(), new Execution(testCreator, new HashMap<String, Object>() {{
            put("day", 8);
        }}), processModel.getNodeConfig(), "k005").size());

        Assertions.assertEquals(5, ModelHelper.getAllUsedNodeKeys(flowLongEngine.getContext(), new Execution(testCreator, new HashMap<String, Object>() {{
            put("day", 8);
        }}), processModel.getNodeConfig(), "k008").size());
    }
}
