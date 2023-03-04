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
package test.subprocess;

import com.flowlong.bpm.engine.entity.Instance;
import com.flowlong.bpm.engine.entity.Task;
import org.junit.jupiter.api.Test;


import test.mysql.MysqlTest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 测试并行子流程
 */
public class TestParallelSubProcess extends MysqlTest {

    @Test
    public void test() {
        // 创建子流程和并行子流程
        Long childProcessId = this.deployByResource("test/subprocess/child.long");
        System.out.println("子流程ID = " + childProcessId);
        Long parallelChildProcessId = this.deployByResource("test/subprocess/parallel-subprocess.long");
        System.out.println("并行子流程ID = " + parallelChildProcessId);

        // 设置工作流任务节点 operator 属性
        Map<String, Object> args = new HashMap<>(2);
        args.put("task1.operator", "1");
        // 创建流程实例
        Instance instance = flowLongEngine.startInstanceById(parallelChildProcessId, "createUserName", args);
        Long id = instance.getId();
        System.out.println("流程实例ID = " + id);
        List<Task> tasks = flowLongEngine.queryService().getTasksByInstanceId(id);
        for (Task task : tasks) {
            System.out.println("************************begin:::::" + task);
            flowLongEngine.executeTask(task.getId(), "1", args);
            System.out.println("************************end:::::" + task);
        }
    }

}
