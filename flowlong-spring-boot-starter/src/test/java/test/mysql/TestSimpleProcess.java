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
import com.flowlong.bpm.engine.entity.FlwHisTask;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 测试简单流程
 *
 * @author ming
 */
@Slf4j
class TestSimpleProcess extends MysqlTest {

    @BeforeEach
    public void before() {
        processId = this.deployByResource("test/simpleProcess.json", testCreator);
    }

    @Test
    void test() {
        // 启动指定流程定义ID启动流程实例
        Map<String, Object> args = new HashMap<>();
        args.put("day", 8);
        args.put("age", 18);
        args.put("assignee", testUser1);

        // 启动指定流程定义ID启动流程实例
        flowLongEngine.startInstanceById(processId, testCreator, args).ifPresent(instance -> {

            // 发起
            this.executeActiveTasks(instance.getId(), testCreator);

            // 测试会签审批人001【审批】
            this.executeTask(instance.getId(), testCreator);

            // 延迟下一会签任务完成时间
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // 测试会签审批人003【审批】
            this.executeTask(instance.getId(), test3Creator);

            //撤回任务(条件路由子审批) 回到测试会签审批人003【审批】任务
            QueryService queryService = flowLongEngine.queryService();
            List<FlwHisTask> hisTasks = queryService.getHisTasksByInstanceId(instance.getId()).get();
            FlwHisTask hisTask = hisTasks.get(0);
            TaskService taskService = flowLongEngine.taskService();
            taskService.withdrawTask(hisTask.getId(), testCreator);

            // 测试会签审批人003【审批】
            this.executeTask(instance.getId(), test3Creator);

            // 年龄审批【审批】
            this.executeTask(instance.getId(), testCreator);

            // 条件内部审核【审批】
            this.executeTask(instance.getId(), testCreator);

            // 条件路由子审批【审批】 抄送 结束
            this.executeTask(instance.getId(), testCreator);

        });
    }
}