/*
 * Copyright 2023-2025 Licensed under the AGPL License
 */
package test.mysql;

import com.flowlong.bpm.engine.assist.StreamUtils;
import com.flowlong.bpm.engine.core.FlowLongContext;
import com.flowlong.bpm.engine.model.ModelHelper;
import com.flowlong.bpm.engine.model.ProcessModel;
import com.flowlong.bpm.spring.adaptive.FlowJacksonHandler;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
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
        System.out.println(processModel.getKey());
        List<Map<String, Object>> nodeMapList = ModelHelper.getNodeMapList(processModel.getNodeConfig(), ((nodeMap, nodeModel) -> {
            nodeMap.put("termAuto", nodeModel.getTermAuto());
            nodeMap.put("term", nodeModel.getTerm());
            nodeMap.put("termMode", nodeModel.getTermMode());
        }));
        Assertions.assertEquals(nodeMapList.get(1).get("conditionNode"), 1);
    }

}
