package com.flowlong.bpm.solon.example.controller;

import com.flowlong.bpm.engine.FlowLongEngine;
import com.flowlong.bpm.engine.core.FlowCreator;
import com.flowlong.bpm.engine.entity.FlwInstance;
import org.noear.solon.annotation.Controller;
import org.noear.solon.annotation.Get;
import org.noear.solon.annotation.Mapping;

import java.util.HashMap;
import java.util.Map;

@Controller
@Mapping("/process")
public class TestController {
    protected FlowLongEngine flowLongEngine;

    protected static FlowCreator testCreator = FlowCreator.of("test001", "测试001");

    /**
     * 流程部署
     *
     * http://localhost:8000/process/deploy
     */
    @Get
    @Mapping("/deploy")
    public Long deployByResource() {
        return flowLongEngine.processService().deployByResource("process.json", testCreator, false);
    }

    /**
     * 启动流程实例
     *
     * http://localhost:8000/process/instance-start
     */
    @Get
    @Mapping("/instance-start")
    public FlwInstance instanceStart() {
        Map<String, Object> args = new HashMap<>();
        args.put("day", 8);
        args.put("assignee", "test001");
        return flowLongEngine.startInstanceByName("请假审批", null, testCreator, args).get();
    }

}
