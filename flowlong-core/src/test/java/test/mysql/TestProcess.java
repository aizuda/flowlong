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

import com.flowlong.bpm.engine.ProcessService;
import com.flowlong.bpm.engine.assist.Assert;
import com.flowlong.bpm.engine.entity.Process;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * 测试简单流程
 *
 * @author xdg
 */
public class TestProcess extends MysqlTest {

    @BeforeEach
    public void before() {
        processId = this.deployByResource("test/task/simple.long");
    }

    @Test
    public void test() {
        ProcessService processService = flowLongEngine.processService();

        // 根据流程定义ID查询
        Process process = processService.getProcessById(processId);
        Assertions.assertNotNull(process);

        // 根据流程定义ID和版本号查询
        process = processService.getProcessByVersion(process.getName(), process.getVersion());
        Assertions.assertNotNull(process);

        // 启动指定流程定义ID启动流程实例
        Map<String, Object> args = new HashMap<>();
        args.put("task1.assignee", testUser1);
        flowLongEngine.startInstanceById(processId, testUser1, args);

        // 卸载指定的定义流程
        Assert.isTrue(processService.undeploy(processId));
    }
}
