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

import com.flowlong.bpm.engine.FlowLongEngine;
import com.flowlong.bpm.engine.assist.ObjectUtils;
import com.flowlong.bpm.engine.entity.Instance;
import com.flowlong.bpm.engine.entity.Task;
import com.flowlong.bpm.engine.exception.FlowLongException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import test.mysql.MysqlTest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 测试无权执行流程
 *
 * @author xdg
 */
public class TestNotAllow extends MysqlTest {

    @BeforeEach
    public void before() {
        processId = this.deployByResource("test/task/right.long");
    }

    @Test
    public void test() {
        Map<String, Object> args = new HashMap<>();
        args.put("task1.assignee", testUser1);

        Instance instance = flowLongEngine.startInstanceById(processId, testUser1, args);
        Assertions.assertNotNull(instance);

        List<Task> taskList = flowLongEngine.queryService().getActiveTasksByInstanceId(instance.getId());
        Assertions.assertTrue(ObjectUtils.isNotEmpty(taskList));

        // 不允许执行异常
        Assertions.assertThrows(FlowLongException.class,
                () -> flowLongEngine.executeTask(taskList.get(0).getId(), testUser2, args));

        // 管理员权限允许执行
        for (Task task : taskList) {
            flowLongEngine.executeTask(task.getId(), FlowLongEngine.ADMIN, args);
        }
    }
}
