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
package test.mysql.task;

import com.flowlong.bpm.engine.core.enums.TaskType;
import com.flowlong.bpm.engine.entity.Instance;
import com.flowlong.bpm.engine.entity.Task;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import test.mysql.MysqlTest;

/**
 * 测试任务撤回
 *
 * @author shen tao tao
 */
public class TestWithdraw extends MysqlTest {

    @BeforeEach
    public void before() {
        processId = this.deployByResource("test/task/withdraw.long");
    }

    @Test
    void test() {
        Instance instance = flowLongEngine.startInstanceByName("withdraw", 1);
        Assertions.assertNotNull(instance);

        Task task = flowLongEngine.queryService().getActiveTasksByInstanceId(instance.getId()).get(0);
        Task nextTask = flowLongEngine.taskService().createNewTask(task.getId(), TaskType.major, testUser1).get(0);
        Assertions.assertNotNull(nextTask);

        flowLongEngine.taskService().complete(task.getId());
        Task withdrawTask = flowLongEngine.taskService().withdrawTask(task.getId(), testUser1);
        flowLongEngine.taskService().complete(withdrawTask.getId(), testUser1);
    }
}
