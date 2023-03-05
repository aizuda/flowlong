/* Copyright 2013-2015 www.snakerflow.com.
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
package test.task.aidant;

import com.flowlong.bpm.engine.QueryService;
import com.flowlong.bpm.engine.entity.Instance;
import com.flowlong.bpm.engine.entity.Task;
import com.flowlong.bpm.engine.model.TaskModel;
import org.junit.jupiter.api.Test;
import test.mysql.MysqlTest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author liulf
 * @since 1.0
 */
public class TestAidant extends MysqlTest {
    @Test
    public void testAidantTask() {
        Long processId = this.deployByResource("test/task/aidant/process.long");
        Map<String, Object> args = new HashMap<>();
        args.put("task1.assignee", "1");
        Instance instance = flowLongEngine.startInstanceByName("aidant", 1, "creteUser", args);
        List<Task> tasks = flowLongEngine.queryService().getActiveTasksByInstanceId(instance.getId());
        for(Task task : tasks) {
            flowLongEngine.taskService().createNewTask(task.getId(), 1, "test");
        }
    }
}
