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
package test.mysql.expire;

import com.flowlong.bpm.engine.entity.Instance;
import com.flowlong.bpm.engine.entity.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import test.mysql.MysqlTest;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 测试时限控制
 *
 * @author wangzi
 */
public class TestExpire extends MysqlTest {

    @BeforeEach
    public void before() {
        processId = this.deployByResource("test/expire.long");
    }

    @Test
    public void test() {
        System.out.println("部署的流程定义ID = " + processId);
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        System.out.println(tomorrow);
        Map<String, Object> args = new HashMap<>();
        args.put("task1.assignee", testUser1);
        args.put("task1.expireTime", tomorrow);
        args.put("task1.reminderTime", tomorrow);
        Instance instance = flowLongEngine.startInstanceByName("expire", null, testUser1, args);
        Long id = instance.getId();
        System.out.println("流程实例ID = " + id);
        List<Task> tasks = flowLongEngine.queryService().getTasksByInstanceId(id);
        for (Task task : tasks) {
            System.out.println("************************begin:::::" + task);
            flowLongEngine.executeTask(task.getId(), testUser1, args);
            System.out.println("************************end:::::" + task);
        }
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}