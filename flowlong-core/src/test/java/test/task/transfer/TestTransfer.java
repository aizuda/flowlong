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
package test.task.transfer;

import com.flowlong.bpm.engine.access.QueryFilter;
import com.flowlong.bpm.engine.entity.Instance;
import com.flowlong.bpm.engine.entity.Task;
import test.TestLongBase;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

/**
 * @author hubin
 * @since 1.0
 */
public class TestTransfer extends TestLongBase {
    @Before
    public void before() {
        this.deployByResource(
                "test/task/transfer/process.long");
    }

    @Test
    public void test() {
        Instance instance = engine.startInstanceByName("transfer", 0);
        System.out.println("instance=" + instance);
        List<Task> tasks = queryService.getActiveTasks(new QueryFilter().setInstanceId(instance.getId()));
        for (Task task : tasks) {
            engine.taskService().createNewTask(task.getId(), 0, "test");
            engine.taskService().complete(task.getId());
        }
    }
}
