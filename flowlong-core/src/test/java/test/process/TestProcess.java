/* Copyright 2023-2025 www.flowlong.com
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
package test.process;

import com.flowlong.bpm.engine.entity.Process;
import org.junit.Before;
import org.junit.Test;
import test.TestLongBase;

import java.util.HashMap;
import java.util.Map;

/**
 * @author hubin
 * @since 1.0
 */
public class TestProcess extends TestLongBase {

    @Before
    public void before() {
        this.deployByResource("test/task/simple/process.long");
    }

    @Test
    public void test() {
        Process process = engine.processService().getProcessById(processId);
        System.out.println("output 1=" + process);
        process = engine.processService().getProcessByVersion(process.getName(), process.getVersion());
        System.out.println("output 2=" + process);
        Map<String, Object> args = new HashMap<>();
        args.put("task1.operator", "1");
        engine.startInstanceById(processId, "1", args);
        engine.processService().undeploy(processId);
        //engine.startInstanceById(processId, "1", args);
    }
}
