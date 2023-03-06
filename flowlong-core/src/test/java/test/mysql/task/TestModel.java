/*
 *  Copyright 2013-2015 www.snakerflow.com.
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package test.mysql.task;

import com.flowlong.bpm.engine.entity.Instance;
import com.flowlong.bpm.engine.entity.Task;
import com.flowlong.bpm.engine.model.TaskModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import test.mysql.MysqlTest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 测试模型操作
 *
 * @author yuqs
 * @since 2.0
 */
public class TestModel extends MysqlTest {

    @BeforeEach
    public void before() {
        processId = this.deployByResource("test/task/process.long");
    }

    @Test
    public void test() {
        Map<String, Object> args = new HashMap<String, Object>();
        args.put("task1.operator", new String[]{"1"});
        Instance instance = flowLongEngine.startInstanceByName("simple", null, "2", args);
        System.out.println("instance=" + instance);
        List<Task> tasks = flowLongEngine.queryService().getActiveTasksByInstanceId(instance.getId());
        for (Task task : tasks) {
            TaskModel model = flowLongEngine.taskService().getTaskModel(String.valueOf(task.getId()));
            System.out.println(model.getName());
            List<TaskModel> models = model.getNextModels(TaskModel.class);
            for (TaskModel tm : models) {
                System.out.println(tm.getName());
            }
        }
        List<TaskModel> models = flowLongEngine.processService().getProcessById(processId).getProcessModel().getModels(TaskModel.class);
        for (TaskModel tm : models) {
            System.out.println(tm.getName());
        }
    }

}
