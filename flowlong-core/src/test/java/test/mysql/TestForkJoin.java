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
import com.flowlong.bpm.engine.entity.Task;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import test.mysql.MysqlTest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 测试测试分支合并
 *
 * @author zhaoxb123
 */
public class TestForkJoin extends MysqlTest {

	@BeforeEach
	public void before() {
		processId = this.deployByResource("test/forkjoin.long");
	}

	@Test
	public void test() {
		Map<String, Object> args = new HashMap<>();
		args.put("task1.assignee", testUser1);
		args.put("task2.assignee", testUser2);
		args.put("task3.assignee", testUser3);
		Instance instance = flowLongEngine.startInstanceById(processId, testUser1, args);
		Assertions.assertNotNull(instance);

		List<Task> taskList = flowLongEngine.queryService().getActiveTasksByInstanceId(instance.getId());
		for(Task task : taskList) {
			System.out.println("==========================="+task.getTaskName());
			flowLongEngine.executeTask(task.getId(), testUser1, args);
		}
	}
}
