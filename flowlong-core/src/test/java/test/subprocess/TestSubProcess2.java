/* Copyright 2023-2025 www.flowlong.com
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
package test.subprocess;

import com.flowlong.bpm.engine.access.QueryFilter;
import com.flowlong.bpm.engine.entity.Instance;
import com.flowlong.bpm.engine.entity.Task;
import test.TestLongBase;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 测试子流程的fork-join流程
 * start->subprocess1----->end
 * |___subprocess2_______|
 *
 * @author hubin
 * @since 1.0
 */
public class TestSubProcess2 extends TestLongBase {
    @Before
    public void before() {
        engine.processService().deployByResource("test/subprocess/child.long");
        this.deployByResource("test/subprocess/subprocess2.long");
    }

    @Test
    public void test() {
        Map<String, Object> args = new HashMap<>();
        args.put("task1.operator", new String[]{"1"});
        Instance instance = engine.startInstanceById(processId, "2", args);
        System.out.println(instance);
        List<Task> tasks = engine.queryService().getActiveTasks(new QueryFilter().setInstanceId(instance.getId()));
        for (Task task : tasks) {
            engine.executeTask(task.getId(), "1", args);
        }
    }
}
