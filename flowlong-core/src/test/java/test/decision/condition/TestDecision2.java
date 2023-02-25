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
package test.decision.condition;

import com.flowlong.bpm.engine.entity.Instance;
import test.TestLongBase;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * 测试决策分支流程2：使用transition的expr属性决定后置路线
 *
 * @author hubin
 * @since 1.0
 */
public class TestDecision2 extends TestLongBase {
    @Before
    public void before() {
        this.deployByResource("test/decision/condition/process.long");
    }

    @Test
    public void test() {
        Map<String, Object> args = new HashMap<>();
        args.put("task1.operator", new String[]{"1"});
        args.put("task2.operator", new String[]{"1"});
        args.put("task3.operator", new String[]{"1"});
        args.put("content", 250);
        Instance instance = engine.startInstanceById(processId, "2", args);
        System.out.println(instance);
    }
}
