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

import com.flowlong.bpm.engine.RuntimeService;
import com.flowlong.bpm.engine.entity.Instance;
import com.flowlong.bpm.engine.entity.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 测试抄送
 */
public class TestCC extends MysqlTest {

    @BeforeEach
    public void before() {
        processId = this.deployByResource("test/cc.long");
    }

    /**
     * 抄送测试
     */
    @Test
    public void testCc() {
        System.out.println("流程定义ID = " + processId);
        Map<String, Object> args = new HashMap<>();
        // 设置工作流任务节点 assignee 属性
        args.put("task1.assignee", "1");
        Instance instance = flowLongEngine.startInstanceByName("simple", 1, testUser1, args);
        RuntimeService runtimeService = flowLongEngine.runtimeService();

        // 创建抄送实例，暂时先 debug 观察数据库表结构数据变化
        final String actorId = "1000";
        runtimeService.createCCInstance(instance.getId(), testUser2, actorId);

        // 查阅完成，结束抄送实例
        runtimeService.finishCCInstance(instance.getId(), actorId);

        // 删除抄送实例
        runtimeService.deleteCCInstance(instance.getId(), actorId);
    }

    @Test
    public void activeTasksByInstanceId() {
        List<Task> taskList = flowLongEngine.queryService().getActiveTasksByInstanceId(1631867495096328191L);
        if (null != taskList) {
            taskList.forEach(t -> System.out.println(t.getTaskName()));
        }
    }
}
