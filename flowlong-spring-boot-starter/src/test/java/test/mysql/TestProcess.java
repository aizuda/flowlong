/* 
 * Copyright 2023-2025 Licensed under the AGPL License
 */
package test.mysql;

import com.flowlong.bpm.engine.ProcessService;
import com.flowlong.bpm.engine.entity.FlwProcess;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * 测试简单流程
 *
 * @author xdg
 */
public class TestProcess extends MysqlTest {

    @BeforeEach
    public void before() {
        processId = this.deployByResource("test/process.json", testCreator);
    }

    /**
     * 审批通过
     */
    @Test
    public void testComplete() {
        this.testProcess(false);
    }

    /**
     * 审批拒绝
     */
    @Test
    public void testReject() {
        this.testProcess(true);
    }

    public void testProcess(boolean reject) {
        ProcessService processService = flowLongEngine.processService();

        // 根据流程定义ID查询
        FlwProcess process = processService.getProcessById(processId);
        if (null != process) {
            // 根据流程定义ID和版本号查询
            Assertions.assertNotNull(processService.getProcessByVersion(process.getProcessName(), process.getProcessVersion()));
        }

        // 启动指定流程定义ID启动流程实例
        Map<String, Object> args = new HashMap<>();
        args.put("day", 8);
        args.put("assignee", testUser1);
        flowLongEngine.startInstanceById(processId, testCreator, args).ifPresent(instance -> {

            // 发起，执行条件路由
            this.executeActiveTasks(instance.getId(), testCreator);

            if (reject) {
                // 领导审批【拒绝强制终止，和驳回拒绝不一样】，流程结束
                flowLongEngine.runtimeService().reject(instance.getId(), testCreator);
            } else {
                // 领导审批【通过】，流程结束
                this.executeActiveTasks(instance.getId(), testCreator);
            }
        });

        // 卸载指定的定义流程
        // Assertions.assertTrue(processService.undeploy(processId));
    }


    /**
     * 测试流程的级联删除
     */
    @Test
    public void cascadeRemove() {
        ProcessService processService = flowLongEngine.processService();

        // 测试级联删除
        processService.cascadeRemove(processId);
    }

}
