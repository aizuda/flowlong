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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import test.mysql.MysqlTest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 类名称：TestSimple
 * <p>
 * 描述：
 * 创建人：xdg
 * 创建时间：2023-03-04 13:13
 */
public class TestSimple extends MysqlTest {
    private Long processId;

    @BeforeEach
    public void before() {
        processId = this.deployByResource("test/task/simple.long");
        flowLongEngine.processService().updateType(processId, "预算管理流程");
    }

    @Test
    public void test() {
        Map<String, Object> args = new HashMap<String, Object>();
        args.put("task1.operator", new String[]{"1"});

        Instance instance = flowLongEngine.startInstanceByName("simple", 1, "2", args);
        System.out.println(instance);

        List<Task> taskList = flowLongEngine.queryService().getActiveTasksByInstanceId(instance.getId());
        for (Task task : taskList) {
            flowLongEngine.executeTask(task.getId(), "1", args);
        }
    }
}
