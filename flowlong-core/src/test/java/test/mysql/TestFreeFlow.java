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

import com.flowlong.bpm.engine.entity.Instance;
import com.flowlong.bpm.engine.entity.Task;
import com.flowlong.bpm.engine.model.TaskModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 测试创建自由任务
 *
 * @author shen tao tao
 */
public class TestFreeFlow extends MysqlTest {

    @BeforeEach
    public void before() {
        processId = this.deployByResource("test/free.long");
    }

    @Test
    public void test() {
        Map<String, Object> args = new HashMap<>();
        args.put("task1.assignee", testUser1);
        Instance instance = flowLongEngine.startInstanceById(processId, testUser1, args);
        TaskModel tm1 = new TaskModel();
        tm1.setName("task1");
        tm1.setDisplayName("自由任务1");
        List<Task> tasks = flowLongEngine.createFreeTask(instance.getId(), testUser2, args, tm1);
        for (Task task : tasks) {
            // 完成自由任务
            flowLongEngine.taskService().complete(task.getId(), testUser2, null);
        }
        // 终止流程
        flowLongEngine.runtimeService().terminate(instance.getId());
    }
}
