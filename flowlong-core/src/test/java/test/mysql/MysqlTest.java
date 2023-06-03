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

import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import test.TestFlowLong;

/**
 * Mysql 测试基类
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(locations = {"classpath:spring-test-mysql.xml"})
public class MysqlTest extends TestFlowLong {

    /**
     * 执行当前活跃用户
     *
     * @param instanceId 流程实例ID
     * @param createBy   创建人ID
     */
    public void executeActiveTasks(Long instanceId, String createBy) {
        this.flowLongEngine.queryService().getActiveTasksByInstanceId(instanceId)
                .ifPresent(tasks -> tasks.forEach(t -> this.flowLongEngine.executeTask(t.getId(), createBy)));
    }

}
