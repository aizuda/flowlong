package com.flowlong.bpm.spring.example.controller;

import com.flowlong.bpm.engine.FlowLongEngine;
import com.flowlong.bpm.engine.core.FlowCreator;
import com.flowlong.bpm.engine.entity.FlwInstance;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/process")
@AllArgsConstructor
public class TestController {
    protected FlowLongEngine flowLongEngine;

    protected static FlowCreator testCreator = FlowCreator.of("test001", "测试001");

    /**
     * <a href="http://localhost:8000/process/deploy">流程部署</a>
     */
    @GetMapping("/deploy")
    public Long deployByResource() {
        return flowLongEngine.processService().deployByResource("process.json", testCreator, false);
    }

    /**
     * <a href="http://localhost:8000/process/instance-start">启动流程实例</a>
     */
    @GetMapping("/instance-start")
    public FlwInstance instanceStart() {
        Map<String, Object> args = new HashMap<>();
        args.put("day", 8);
        args.put("assignee", "test001");
        return flowLongEngine.startInstanceByProcessKey("process", null, testCreator, args).get();
    }

}
