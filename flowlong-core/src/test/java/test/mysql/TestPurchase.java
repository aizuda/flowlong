/* Copyright 2023-2025 jobob@qq.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package test.mysql;

import com.flowlong.bpm.engine.QueryService;
import com.flowlong.bpm.engine.TaskService;
import com.flowlong.bpm.engine.entity.HisTask;
import com.flowlong.bpm.engine.entity.TaskActor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;

/**
 * 测试简单流程
 *
 * @author xdg
 */
@Slf4j
public class TestPurchase extends MysqlTest {

    @BeforeEach
    public void before() {
        processId = this.deployByResource("test/purchase.json");
    }

    @Test
    public void test() {
        // 启动指定流程定义ID启动流程实例
        flowLongEngine.startInstanceById(processId, testUser1).ifPresent(instance -> {

            TaskActor testActor = TaskActor.ofUser(testUser1, "测试");

            // 发起
            this.executeActiveTasks(instance.getId(), testActor);

            // 领导审批
            this.executeActiveTasks(instance.getId(), testActor);

            // 撤回任务（领导审批）
            QueryService queryService = flowLongEngine.queryService();
            List<HisTask> hisTasks = queryService.getHisTasksByInstanceId(instance.getId()).get();
            HisTask hisTask = hisTasks.get(0);
            TaskService taskService = flowLongEngine.taskService();
            taskService.withdrawTask(hisTask.getId(), testActor);

            // 驳回任务（领导审批驳回，任务至发起人）
            this.executeActiveTasks(instance.getId(), t ->
                    taskService.rejectTask(t, testActor, new HashMap<String, Object>() {{
                        put("reason", "不符合要求");
                    }})
            );

            // 执行当前任务并跳到【经理确认】节点
            this.executeActiveTasks(instance.getId(), t ->
                    flowLongEngine.executeAndJumpTask(t.getId(), "经理确认", testActor)
            );

            // 经理确认，流程结束
            this.executeActiveTasks(instance.getId(), testActor);
        });
    }
}
