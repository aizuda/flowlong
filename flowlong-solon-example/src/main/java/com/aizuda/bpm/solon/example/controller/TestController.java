package com.aizuda.bpm.solon.example.controller;

import com.aizuda.bpm.engine.FlowLongEngine;
import com.aizuda.bpm.engine.core.FlowCreator;
import com.aizuda.bpm.engine.entity.FlwInstance;
import lombok.AllArgsConstructor;
import org.noear.solon.annotation.Controller;
import org.noear.solon.annotation.Get;
import org.noear.solon.annotation.Mapping;

import java.util.HashMap;
import java.util.Map;

@Controller
@Mapping("/process")
@AllArgsConstructor
public class TestController {
    protected static FlowCreator testCreator = FlowCreator.of("test001", "测试001");
    protected FlowLongEngine flowLongEngine;

    /**
     * <a href="http://localhost:8000/process/deploy">流程部署</a>
     */
    @Get
    @Mapping("/deploy")
    public Long deployByResource() {
        return flowLongEngine.processService().deployByResource("process.json", testCreator, false);
    }

    /**
     * <a href="http://localhost:8000/process/instance-start">启动流程实例</a>
     */
    @Get
    @Mapping("/instance-start")
    public FlwInstance instanceStart() {
        Map<String, Object> args = new HashMap<>();
        args.put("day", 8);
        args.put("assignee", "test001");
        return flowLongEngine.startInstanceByProcessKey("process", null, testCreator, args).get();
    }

}
