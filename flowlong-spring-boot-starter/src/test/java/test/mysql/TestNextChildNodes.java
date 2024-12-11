package test.mysql;

import com.aizuda.bpm.engine.FlowLongEngine;
import com.aizuda.bpm.engine.core.Execution;
import com.aizuda.bpm.engine.model.ModelHelper;
import com.aizuda.bpm.engine.model.ProcessModel;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;

public class TestNextChildNodes extends TestModel {

    @Autowired
    protected FlowLongEngine flowLongEngine;

    @Test
    public void testNextChildNodes() {
        ProcessModel processModel = getProcessModel("test/TestNextChildNodes.json");
        processModel.buildParentNode(processModel.getNodeConfig());

        Assertions.assertEquals("flk1733396309547", ModelHelper.getNextChildNodes(flowLongEngine.getContext(), new Execution(testCreator, null),
                processModel.getNodeConfig(), "flk1733380944015").get(0).getNodeKey());

        Assertions.assertEquals("flk1733380972879", ModelHelper.getNextChildNodes(flowLongEngine.getContext(), new Execution(testCreator, null),
                processModel.getNodeConfig(), "flk1733396309547").get(0).getNodeKey());

        Assertions.assertEquals("flk1733446929109", ModelHelper.getNextChildNodes(flowLongEngine.getContext(), new Execution(testCreator, null),
                processModel.getNodeConfig(), "flk1733380972879").get(0).getNodeKey());

        Assertions.assertEquals("flk1733446923640", ModelHelper.getNextChildNodes(flowLongEngine.getContext(), new Execution(testCreator, new HashMap<String, Object>(){{
            put("aaa", "11");
        }}), processModel.getNodeConfig(), "flk1733380972879").get(0).getNodeKey());

        // 下一步执行到并行分支
        Assertions.assertEquals(2, ModelHelper.getNextChildNodes(flowLongEngine.getContext(), new Execution(testCreator, null),
                processModel.getNodeConfig(), "flk1733446929109").size());

        Assertions.assertEquals("flk1733446917296", ModelHelper.getNextChildNodes(flowLongEngine.getContext(), new Execution(testCreator, null),
                processModel.getNodeConfig(), "flk1733446912655").get(0).getNodeKey());

        // 路由分支
        Assertions.assertEquals("flk1733446923640", ModelHelper.getNextChildNodes(flowLongEngine.getContext(), new Execution(testCreator, new HashMap<String, Object>() {{
            put("aaa", "11");
        }}), processModel.getNodeConfig(), "flk1733446917296").get(0).getNodeKey());

        // 跳过路由分支，包容分支执行默认条件
        Assertions.assertEquals("flk1733658019276", ModelHelper.getNextChildNodes(flowLongEngine.getContext(), new Execution(testCreator, null),
                processModel.getNodeConfig(), "flk1733446917296").get(0).getNodeKey());

        // 包容分支
        Assertions.assertEquals("flk1733658095760", ModelHelper.getNextChildNodes(flowLongEngine.getContext(), new Execution(testCreator, new HashMap<String, Object>() {{
            put("bbb", "11");
        }}), processModel.getNodeConfig(), "flk1733446917296").get(0).getNodeKey());

        Assertions.assertEquals("flk1733658019276", ModelHelper.getNextChildNodes(flowLongEngine.getContext(), new Execution(testCreator, null),
                processModel.getNodeConfig(), "flk1733658095760").get(0).getNodeKey());

    }
}
