/*
 * Copyright 2023-2025 Licensed under the AGPL License
 */
package test.mysql;

import com.flowlong.bpm.engine.assist.StreamUtils;
import com.flowlong.bpm.engine.model.*;
import com.flowlong.bpm.spring.adaptive.FlowJacksonHandler;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.*;

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
public class TestModel {

    @Test
    public void testNodeMapList() {
        try {
            String modeContent = StreamUtils.readBytes(StreamUtils.getResourceAsStream("test/simpleProcess.json"));
            FlowJacksonHandler flowJacksonHandler = new FlowJacksonHandler();
            ProcessModel processModel = flowJacksonHandler.fromJson(modeContent, ProcessModel.class);
            System.out.println(processModel.getKey());
            List<Map<String, Object>> nodeMapList = ModelHelper.getNodeMapList(processModel.getNodeConfig(), ((nodeMap, nodeModel) -> {
                nodeMap.put("termAuto", nodeModel.getTermAuto());
                nodeMap.put("term", nodeModel.getTerm());
                nodeMap.put("termMode", nodeModel.getTermMode());
            }));
            Assertions.assertEquals(nodeMapList.get(1).get("conditionNode"), 1);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
