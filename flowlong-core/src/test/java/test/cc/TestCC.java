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
package test.cc;

import com.flowlong.bpm.engine.entity.Instance;
import test.TestLongBase;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * @author hubin
 * @since 1.0
 */
public class TestCC extends TestLongBase {

    @Before
    public void before() {
        this.deployByResource("test/cc/process.long");
    }

    @Test
    public void test() {
        Map<String, Object> args = new HashMap<>();
        args.put("task1.operator", new String[]{"1"});
        Instance instance = engine.startInstanceByName("simple", 0, "2", args);
        engine.runtimeService().createCCInstance(instance.getId(), "test");
//		engine.runtimeService().updateCCStatus("b0fcc08da45d4e88819d9c287917b525", "test");
//		engine.runtimeService().deleteCCInstance("01b960b9d5df4be7b8565b9f64bc1856", "test");
    }
}
