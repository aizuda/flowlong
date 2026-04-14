/*
 * Copyright 2023-2025 Licensed under the Dual Licensing
 * website: https://aizuda.com
 */
package test.mysql;

import com.aizuda.bpm.engine.entity.FlwTaskActor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

/**
 * 测试抄送节点跟条件分支
 */
@Slf4j
public class TestCcTask extends MysqlTest {

    @BeforeEach
    public void before() {
        processId = this.deployByResource("test/ccTask.json", testCreator);
    }

    @Test
    public void test() {
        flowLongEngine.startInstanceById(processId, testCreator).ifPresent(instance -> {

            this.executeActiveTasks(instance.getId(), flwTask -> {

                // 手动创建抄送任务
                Assertions.assertTrue(flowLongEngine.createCcTask(flwTask, Arrays.asList(
                        FlwTaskActor.of(test2Creator, flwTask),
                        FlwTaskActor.of(test2Creator, flwTask)
                ), testCreator));

                // 执行节点
                flowLongEngine.executeTask(flwTask.getId(), testCreator);
            });
        });
    }
}
