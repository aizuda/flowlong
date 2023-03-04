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
package test.query;

import com.flowlong.bpm.engine.core.mapper.TaskMapper;
import com.flowlong.bpm.engine.entity.Task;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import test.mysql.MysqlTest;

/**
 * 查询任务
 */
public class TestQueryTask extends MysqlTest {
	@Autowired
	private TaskMapper taskMapper;

	@Test
	public void test() {
		Long taskId = 0L;

		// 方式一 取 queryService 查询
		Task byQueryService = flowLongEngine.queryService().getTask(taskId);
		System.out.println("QueryService查询 = " + byQueryService);

		// 方式二 注入 Mapper 查询
		Task byMapper = taskMapper.selectById(taskId);
		System.out.println("Mapper查询 = " + byMapper);
	}
}
