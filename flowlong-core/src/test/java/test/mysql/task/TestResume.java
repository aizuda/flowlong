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
import com.flowlong.bpm.engine.exception.FlowLongException;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import test.mysql.MysqlTest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 测试唤醒历史任务
 *
 * @author he.wenyao
 */

@Slf4j
public class TestResume extends MysqlTest {

    @BeforeEach
    public void before() {
        processId = this.deployByResource("test/task/resume.long");
    }

    @Test
    public void test() {
        log.info("流程定义ID = {}", processId);

        // 启动流程，生成一个实例
        Map<String, Object> args = new HashMap<>();
        args.put("task1.assignee", testUser1);
        Instance instance = this.flowLongEngine.startInstanceByName("resume", 1, testUser1, args);
        Assertions.assertNotNull(instance);

        // 获取活跃的任务
        List<Task> tasks = this.flowLongEngine.queryService().getActiveTasksByInstanceId(instance.getId());

        // 执行任务
        tasks.forEach(t -> this.flowLongEngine.executeTask(t.getId(), testUser1));

        // 唤醒已经被执行过的任务
        Assertions.assertThrows(FlowLongException.class, () ->
                tasks.forEach(t -> this.flowLongEngine.taskService().resume(t.getId(), testUser1)));
    }
}
