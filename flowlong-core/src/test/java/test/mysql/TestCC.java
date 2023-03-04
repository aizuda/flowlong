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
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * 测试抄送
 */
public class TestCC extends MysqlTest {

    /**
     * 抄送测试
     */
    @Test
    public void testCc() {
        Long processId = this.deployByResource("test/cc/process.long");
        System.out.println("流程定义ID = " + processId);
        Map<String, Object> args = new HashMap<>();
        // 设置工作流任务节点 assignee 属性
        args.put("task1.assignee", "1");
        Instance instance = flowLongEngine.startInstanceByName("simple", 1, "testUser", args);
        RuntimeService runtimeService = flowLongEngine.runtimeService();
        // 创建抄送实例，暂时先 debug 观察数据库表结构数据变化
        final String actorId = "1000";
        runtimeService.createCCInstance(instance.getId(), "test", actorId);
        runtimeService.updateCCStatus(instance.getId(), actorId);
        runtimeService.deleteCCInstance(instance.getId(), actorId);
    }
}
