/*
 * Copyright 2023-2025 Licensed under the Dual Licensing
 * website: https://aizuda.com
 */
package test.mysql;

import com.aizuda.bpm.engine.core.enums.InstanceState;
import com.aizuda.bpm.engine.entity.FlwHisInstance;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * 测试自动节点
 * <p>在某些特定条件下流程自动通过或者自动拒绝</p>
 */
@Slf4j
public class TestAutoNode extends MysqlTest {

    @BeforeEach
    public void before() {
        processId = this.deployByResource("test/testAutoNode.json", testCreator);
    }

    @Test
    public void testAutoPass() {
        // 测试自动通过
        Map<String, Object> args = new HashMap<>();
        args.put("aaa", 111);
        flowLongEngine.startInstanceById(processId, testCreator, args).ifPresent(instance -> {

            // 自动通过流程结束
            FlwHisInstance fhi = flowLongEngine.queryService().getHistInstance(instance.getId());
            Assertions.assertTrue(InstanceState.autoPass.eq(fhi.getInstanceState()));
        });
    }

    @Test
    public void testAutoReject() {
        // 测试自动拒绝
        Map<String, Object> args = new HashMap<>();
        args.put("aaa", 222);
        flowLongEngine.startInstanceById(processId, testCreator, args).ifPresent(instance -> {

            // 自动拒绝流程结束
            FlwHisInstance fhi = flowLongEngine.queryService().getHistInstance(instance.getId());
            Assertions.assertTrue(InstanceState.autoReject.eq(fhi.getInstanceState()));
        });
    }
}
