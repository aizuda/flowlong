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
package test.mysql.task.countersign;

import com.flowlong.bpm.engine.entity.Instance;
import com.flowlong.bpm.engine.entity.Task;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import test.mysql.MysqlTest;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 测试会签任务
 *
 * @author lijing
 * @since 1.0
 */
public class TestCountersignTask extends MysqlTest {

    @BeforeEach
    public void before() {
        processId = this.deployByResource("test/countersign/countersign.long");
    }

    /**
     * 简单会签测试
     */
    @Test
    public void test() {
        Instance instance = flowLongEngine.startInstanceById(processId, testUser1);
        List<Task> tasks = flowLongEngine.queryService().getActiveTasksByInstanceId(instance.getId());
        int count = 0;
        for (Task task : tasks) {
            String createBy = task.getCreateBy();
            // 会签任务只允许为 test001 、test002
            Assertions.assertTrue(Objects.equals(createBy, testUser1)
                    || Objects.equals(createBy, testUser2));
            flowLongEngine.executeTask(task.getId(), createBy);
            count++;
        }
        Assertions.assertEquals(2, count);
    }

    /**
     * 动态加签测试
     */
    @Test
    public void test1() {
        Instance instance = flowLongEngine.startInstanceById(processId, testUser1);
        List<Task> tasks = flowLongEngine.queryService().getActiveTasksByInstanceId(instance.getId());
        List<String> taskActor = new ArrayList<>();
        taskActor.add(testUser3);
        flowLongEngine.taskService().addTaskActor(tasks.get(0).getId(), taskActor);
        List<Task> newTasks = flowLongEngine.queryService().getActiveTasksByInstanceId(instance.getId());
        Assertions.assertEquals(tasks.size() + 1, newTasks.size());
    }
}
