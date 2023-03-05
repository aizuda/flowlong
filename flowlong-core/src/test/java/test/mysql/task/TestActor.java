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

import com.flowlong.bpm.engine.entity.Instance;
import com.flowlong.bpm.engine.entity.Task;
import com.flowlong.bpm.engine.model.TaskModel;
import org.junit.jupiter.api.Test;
import test.mysql.MysqlTest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author shen tao tao
 * @date 2023/3/4 11:40
 * @since 1.0
 */
public class TestActor extends MysqlTest {

    @Test
    void test() {
        Task task = createTask();
        flowLongEngine.taskService().addTaskActor(task.getId(), 0, "test1", "test2");
        flowLongEngine.taskService().removeTaskActor(task.getId(), "test2");
    }

    private Task createTask() {
        Map<String, Object> args = new HashMap<String, Object>();
        args.put("task1.operator", new String[]{"1"});
        Long processId = this.deployByResource("test/task/simple/process.long");
        Instance instance = flowLongEngine.startInstanceById(processId, "test0", args);
        System.out.println("instance=" + instance);
        TaskModel tm1 = new TaskModel();
        tm1.setName("task1");
        tm1.setDisplayName("任务1");
        List<Task> tasks = flowLongEngine.createFreeTask(instance.getId(), "test0", args, tm1);
        return tasks.get(0);
    }
}
