/* 
 * Copyright 2023-2025 Licensed under the AGPL License
 */
package test.mysql;

import com.aizuda.bpm.engine.ProcessService;
import com.aizuda.bpm.engine.core.FlowCreator;
import com.aizuda.bpm.engine.entity.FlwProcess;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * 测试子流程
 *
 * @author xdg
 */
public class TestSubProcess extends MysqlTest {

    @BeforeEach
    public void before() {
        processId = this.deployByResource("test/subProcess.json", getFlowCreator());

        // 部署子流程
        this.deployByResource("test/workHandover.json", getFlowCreator());
    }

    /**
     * 审批通过
     */
    @Test
    public void testComplete() {
        this.testProcess(false, 8);
    }

    /**
     * 审批拒绝
     */
    @Test
    public void testReject() {
        this.testProcess(true, 8);
    }

    @Test
    public void testDay() {
        this.testProcess(false, 3);
    }

    public void testProcess(boolean reject, int day) {
        ProcessService processService = flowLongEngine.processService();

        // 根据流程定义ID查询
        FlwProcess process = processService.getProcessById(processId);
        if (null != process) {
            // 根据流程定义ID和版本号查询
            Assertions.assertNotNull(processService.getProcessByVersion(process.getProcessKey(), process.getProcessVersion()));
        }

        // 启动指定流程定义ID启动流程实例
        FlowCreator flowCreator = this.getFlowCreator();
        // 发起，执行条件路由
        flowLongEngine.startInstanceById(processId, flowCreator, "这里是关联业务KEY").ifPresent(instance -> {

            // 人事审批
            Map<String, Object> args = new HashMap<>();
            args.put("day", day);
            args.put("assignee", testUser1);
            this.executeActiveTasks(instance.getId(), flowCreator, args);

            if (reject) {
                // 领导审批【拒绝强制终止，和驳回拒绝不一样】，流程结束
                flowLongEngine.runtimeService().reject(instance.getId(), flowCreator);
            } else {
                // 领导审批【通过】，流程结束
                this.executeActiveTasks(instance.getId(), flowCreator);
            }

            // 找到子流程并执行【接收工作任务】完成启动父流程执行结束
            flowLongEngine.queryService().getHisTasksByInstanceId(instance.getId()).ifPresent(flwHisTasks -> flwHisTasks.forEach(flwHisTask -> {
                if (null != flwHisTask.getCallInstanceId()) {
                    this.executeActiveTasks(flwHisTask.getCallInstanceId(), test3Creator);
                }
            }));
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

    public FlowCreator getFlowCreator() {
        return testCreator;
    }
}
