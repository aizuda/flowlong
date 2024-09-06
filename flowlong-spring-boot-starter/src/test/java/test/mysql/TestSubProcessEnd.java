/*
 * Copyright 2023-2025 Licensed under the AGPL License
 */
package test.mysql;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * 测试子流程
 *
 * @author xdg
 */
public class TestSubProcessEnd extends MysqlTest {

    @BeforeEach
    public void before() {
        processId = this.deployByResource("test/subProcessEnd.json", testCreator);

        // 部署子流程
        this.deployByResource("test/workHandover.json", testCreator);
    }

    @Test
    public void test() {
        // 发起，执行条件路由
        flowLongEngine.startInstanceById(processId, testCreator, "这里是关联业务KEY").ifPresent(instance -> {

            // 人事审批
            this.executeActiveTasks(instance.getId(), testCreator);

            // 找到子流程并执行【接收工作任务】完成启动父流程执行结束
            flowLongEngine.queryService().getHisTasksByInstanceId(instance.getId()).ifPresent(flwHisTasks -> flwHisTasks.forEach(flwHisTask -> {
                if (null != flwHisTask.getCallInstanceId()) {
                    this.executeActiveTasks(flwHisTask.getCallInstanceId(), test3Creator);
                }
            }));
        });
    }
}
