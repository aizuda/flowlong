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

/**
 * 测试子流程
 *
 * @author xdg
 */
public class TestParallelSubProcess extends MysqlTest {

    @BeforeEach
    public void before() {
        processId = this.deployByResource("test/parallelSubProcess.json", getFlowCreator());

        // 部署子流程
        this.deployByResource("test/workHandover.json", getFlowCreator());
    }

    @Test
    public void testProcess() {
        ProcessService processService = flowLongEngine.processService();

        // 根据流程定义ID查询
        FlwProcess process = processService.getProcessById(processId);
        if (null != process) {
            // 根据流程定义ID和版本号查询
            Assertions.assertNotNull(processService.getProcessByVersion(process.getTenantId(), process.getProcessKey(), process.getProcessVersion()));
        }

        // 启动指定流程定义ID启动流程实例
        flowLongEngine.startInstanceById(processId, testCreator).ifPresent(instance -> {
            this.executeTaskByKey(instance.getId(), test2Creator, "k003");
            // 找到子流程并执行【接收工作任务】完成启动父流程执行结束
            flowLongEngine.queryService().getHisTasksByInstanceId(instance.getId()).ifPresent(flwHisTasks -> flwHisTasks.forEach(flwHisTask -> {
                if (null != flwHisTask.getCallInstanceId()) {
                    this.executeActiveTasks(flwHisTask.getCallInstanceId(), test3Creator);
                }
            }));
            this.executeTaskByKey(instance.getId(), test2Creator, "k005");
        });
    }


    public FlowCreator getFlowCreator() {
        return testCreator;
    }
}
