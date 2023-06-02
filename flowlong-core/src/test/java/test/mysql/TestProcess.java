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
import com.flowlong.bpm.engine.entity.Instance;
import com.flowlong.bpm.engine.entity.Process;
import com.flowlong.bpm.engine.entity.Task;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 测试简单流程
 *
 * @author xdg
 */
@Slf4j
public class TestProcess extends MysqlTest {

    @BeforeEach
    public void before() {
        processId = this.deployByResource("test/process.json");
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
        args.put("day", 8);
        args.put("assignee", testUser1);
        Instance instance = flowLongEngine.startInstanceById(processId, testUser1, args);

        // 获取活跃的任务
        List<Task> tasks = this.flowLongEngine.queryService().getActiveTasksByInstanceId(instance.getId());
        // 执行任务
        tasks.forEach(t -> this.flowLongEngine.executeTask(t.getId(), testUser1));

        // 获取活跃的任务
        List<Task> tasks2 = this.flowLongEngine.queryService().getActiveTasksByInstanceId(instance.getId());
        // 执行任务
        tasks2.forEach(t -> this.flowLongEngine.executeTask(t.getId(), testUser1));

        // 卸载指定的定义流程
        // Assertions.assertTrue(processService.undeploy(processId));
    }


    /**
     * 测试流程的级联删除
     */
    @Test
    public void cascadeRemove() {
        log.info("开始测试级联删除");
        ProcessService processService = flowLongEngine.processService();
        Map<String, Object> args = new HashMap<>();
        args.put("task1.assignee", testUser1);
        // 启动两个流程，然后抄送一个流程，在执行一个流程 测试级联删除
        Instance ins = this.flowLongEngine.startInstanceByName("请假审批", 1, testUser1, args);
        this.flowLongEngine.startInstanceByName("请假审批", 1, testUser1, args);
        // 抄送一个流程为了测试级联删除
        // RuntimeService runtimeService = flowLongEngine.runtimeService();
        // runtimeService.createCCInstance(ins.getId(), testUser1, testUser3);
        // 获取活跃的任务
        List<Task> tasks = this.flowLongEngine.queryService().getActiveTasksByInstanceId(ins.getId());
        // 执行任务
        tasks.forEach(t -> this.flowLongEngine.executeTask(t.getId(), testUser1));
        // 测试级联删除
        processService.cascadeRemove(processId);
    }

}
