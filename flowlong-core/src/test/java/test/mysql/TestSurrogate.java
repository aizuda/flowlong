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
package test.mysql;

import com.flowlong.bpm.engine.entity.Instance;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * 测试委托代理
 *
 * @author xlsea
 * @since 2023-03-04
 */
public class TestSurrogate extends MysqlTest {

    @BeforeEach
    public void before() {
        processId = this.deployByResource("test/surrogate.long");
    }

    @Test
    public void test() {
        // 组装参数列表
        Map<String, Object> args = new HashMap<>();
        args.put("task1.operator", new String[]{"test"});
        // 启动流程实例
        Instance surrogate = flowLongEngine.startInstanceById(processId, "2", args);
        // 输出流程实例信息
        System.out.println("surrogate=" + surrogate);
        // List<Task> tasks = flowLongEngine.queryService().getTasksByInstanceId(surrogate.getId());
        // for (Task task : tasks) {
        //     // flowLongEngine.executeTask(task.getId(), "1", args);
        // }
    }

}
