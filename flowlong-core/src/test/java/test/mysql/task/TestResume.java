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

import com.flowlong.bpm.engine.assist.StreamUtils;
import com.flowlong.bpm.engine.entity.Instance;
import com.flowlong.bpm.engine.entity.Task;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import test.mysql.MysqlTest;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 测试唤醒历史任务
 *
 * @author he.wenyao
 */

@Slf4j
public class TestResume extends MysqlTest {

    @Test
    public void test() {
        String createBy = "he.wenyao"; // 创建者
        InputStream resourceAsStream = StreamUtils.getResourceAsStream("test/task/resume.long");
        Long processId = this.deploy(resourceAsStream, createBy, true);
        log.info("流程定义ID = {}", processId);
        Map<String, Object> args = new HashMap<>();
        args.put("task1.operator", new String[]{"1"});
        // 启动流程，生成一个实例
        Instance ins = this.flowLongEngine.startInstanceByName("resume", 1, createBy, args);
        // 获取活跃的任务
        List<Task> tasks = this.flowLongEngine.queryService().getActiveTasksByInstanceId(ins.getId());
        // 执行任务
        tasks.forEach(t -> {
            this.flowLongEngine.executeTask(t.getId(),createBy);
        });
        // 唤醒已经被执行过的任务
        tasks.forEach(t -> {
            this.flowLongEngine.taskService().resume(t.getId(), createBy);
        });
    }
}
