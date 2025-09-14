/*
 * Copyright 2023-2025 Licensed under the Dual Licensing
 * website: https://aizuda.com
 */
package test.mysql;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

/**
 * 测试触发器任务
 */
@Slf4j
public class TestTrigger extends MysqlTest {

    @BeforeEach
    public void before() {
        processId = this.deployByResource("test/testTrigger.json", testCreator);
    }

    @Test
    public void test() {
        // 设置 spring 上下文
        SpringHelper.setApplicationContext(applicationContext);

        // 启动流程实例
        flowLongEngine.startInstanceById(processId, testCreator).ifPresent(instance -> {

            // 人事审批
            this.executeTaskByKey(instance.getId(), test2Creator, "k002");

            // 忽略查询时间执行定时器任务
            this.executeActiveTasks(instance.getId(), flwTask ->

                    // 模拟自动完成定时触发器任务
                    flowLongEngine.autoCompleteTask(flwTask.getId(), new HashMap<String, Object>() {{
                        put("jump2k001", "1");
                    }}, testCreator));

            // 发起人跳到触发器
            this.executeActiveTasks(instance.getId(), flwTask ->
                    flowLongEngine.executeJumpTask(flwTask.getId(), "flk1752571403215", testCreator));


            // 模拟自动完成定时触发器任务
            this.executeActiveTasks(instance.getId(), flwTask ->
                    flowLongEngine.autoCompleteTask(flwTask.getId(), testCreator));

            this.executeActiveTasks(instance.getId(), flwTask ->

                    // CEO审批拒绝，驳回会跳过 触发器 回到发起人
                    flowLongEngine.executeRejectTask(flwTask, testCreator));

            this.executeActiveTasks(instance.getId(), flwTask -> Assertions.assertEquals("k001", flwTask.getTaskKey()));
        });
    }
}
