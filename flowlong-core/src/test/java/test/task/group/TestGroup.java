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
package test.task.group;

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
 * 测试该类时，确认是否配置了自定义的访问策略，请检查long.long中的配置
 *
 * @author hubin
 * @since 1.0
 */
public class TestGroup extends TestLongBase {
    @Before
    public void before() {
        this.deployByResource("test/task/group/process.long");
    }

    @Test
    public void test() {
        Map<String, Object> args = new HashMap<>();
        args.put("task1.operator", new String[]{"role1"});
        Instance instance = engine.startInstanceByName("group", 0, "2", args);
        System.out.println("instance=" + instance);
        List<Task> tasks = queryService.getActiveTasks(new QueryFilter().setInstanceId(instance.getId()));
        for (Task task : tasks) {
            //操作人改为test时，角色对应test，会无权处理
            engine.executeTask(task.getId(), "test1", args);
        }
    }
}
