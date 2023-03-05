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

import com.flowlong.bpm.engine.entity.Process;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * 类名称：TestProcess
 * <p>
 * 描述：
 * 创建人：xdg
 * 创建时间：2023-03-04 10:58
 */
public class TestProcess extends MysqlTest {
    @Test
    public void test() {
        Long processId = this.deployByResource("test/task/simple.long");

        Process process = flowLongEngine.processService().getProcessById(processId);

        System.out.println("output 1=" + process);

        process = flowLongEngine.processService().getProcessByVersion(process.getName(), process.getVersion());

        System.out.println("output 2="+process);

        Map<String, Object> args = new HashMap<String, Object>();

        args.put("task1.operator", "1");

        flowLongEngine.startInstanceById(processId, "1", args);
        flowLongEngine.processService().undeploy(processId);
    }
}
