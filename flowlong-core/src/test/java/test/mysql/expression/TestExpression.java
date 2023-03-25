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
package test.mysql.expression;

import com.flowlong.bpm.engine.Expression;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import test.mysql.MysqlTest;

import java.util.HashMap;

/**
 * 测试表达式解析引擎
 *
 * @author ximu
 */
public class TestExpression extends MysqlTest {

    @Test
    public void test() {
        // 获取流程使用的解析器
        Expression expression = flowLongEngine.getContext().getExpression();

        // 值替换
        Assertions.assertEquals("this is param: value", expression.eval(String.class,
                "('this is param: ').concat(#param)", new HashMap<String, Object>() {{
                    put("param", "value");
                }}));

        // 三元运算
        TestObject testObject = TestObject.of("Hi", 6000);
        Assertions.assertEquals("大于", expression.eval(String.class,
                "#testObject.total > 1000 ? '大于' : '小于'", new HashMap<String, Object>() {{
                    put("testObject", testObject);
                }}));

        // 字符串连接
        Assertions.assertEquals("Hi FlowLong", expression.eval(String.class,
                "#testObject.title + ' FlowLong'", new HashMap<String, Object>() {{
                    put("testObject", testObject);
                }}));
    }
}
