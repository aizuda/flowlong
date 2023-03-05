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

import com.flowlong.bpm.engine.entity.Instance;
import com.flowlong.bpm.engine.entity.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import test.mysql.MysqlTest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 测试任务提取
 *
 * @Author: Binfeng.Yan
 */
public class TestTake extends MysqlTest {

	@BeforeEach
	public void before() {
		processId = this.deployByResource("test/task/take.long");
	}

	@Test
	public void test() {
		Map<String, Object> args = new HashMap<String, Object>();
		args.put("task1.assignee", new String[]{"1"});
		Instance instance = flowLongEngine.startInstanceById(processId, "2", args);
		System.out.println("instance=" + instance);

		List<Task> tasks = flowLongEngine.queryService().getActiveTasksByInstanceId(instance.getId());
		for(Task task : tasks) {
			//flowLongEngine.executeTask(task.getId(), "1");
			flowLongEngine.taskService().take(task.getId(), "1");
		}
	}

}
