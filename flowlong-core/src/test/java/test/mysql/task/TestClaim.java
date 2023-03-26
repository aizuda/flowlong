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

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.flowlong.bpm.engine.TaskService;
import com.flowlong.bpm.engine.entity.Instance;
import com.flowlong.bpm.engine.entity.Task;
import com.flowlong.bpm.engine.exception.FlowLongException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import test.mysql.MysqlTest;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 测试 认领任务
 *
 * @Author: Binfeng.Yan
 */
public class TestClaim extends MysqlTest {

	@BeforeEach
	public void before() {
		processId = this.deployByResource("test/task/claim.long");
	}

	@Test
	public void test() {
		Map<String, Object> args = new HashMap<>();
		args.put("task1.assignee", Arrays.asList(testUser1, testUser2));
		Instance instance = flowLongEngine.startInstanceById(processId, testUser1, args);
		Assertions.assertNotNull(instance);

		List<Task> taskList = flowLongEngine.queryService().getActiveTasksByInstanceId(instance.getId());
		Assertions.assertTrue(CollectionUtils.isNotEmpty(taskList));
		Assertions.assertEquals(taskList.get(0).getCreateBy(), testUser1);

		TaskService taskService = flowLongEngine.taskService();
		for(Task task : taskList) {
			// 认领任务，该任务只能 testUser2 可以操作
			taskService.claim(task.getId(), testUser2);
		}

		taskList = flowLongEngine.queryService().getActiveTasksByInstanceId(instance.getId());
		Assertions.assertTrue(CollectionUtils.isNotEmpty(taskList));
		Task firstTask = taskList.get(0);

		// 不允许执行异常
		Assertions.assertThrows(FlowLongException.class,
				() -> taskService.complete(firstTask.getId(), testUser1));
	}

}
