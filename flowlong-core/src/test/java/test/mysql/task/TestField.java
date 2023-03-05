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

import com.flowlong.bpm.engine.model.ProcessModel;
import com.flowlong.bpm.engine.model.TaskModel;
import org.junit.jupiter.api.Test;
import test.mysql.MysqlTest;

/**
 * 测试获取任务field
 *
 * @author lipanre
 * @date 2023/3/4 12:04
 */
public class TestField extends MysqlTest {

    /**
     * 测试获取流程Field
     */
    @Test
    public void testField() {
        Long processId = this.deployByResource("test/task/field/process.long");
        ProcessModel processModel = flowLongEngine.processService().getProcessById(processId).getProcessModel();
        // 获取名称为task1的task
        TaskModel taskModel = (TaskModel) processModel.getNode("task1");

        // 获取task下面的field
        System.out.println("taskModel.getFields() : " + taskModel.getFields());
    }

}
