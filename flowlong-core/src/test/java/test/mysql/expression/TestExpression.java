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
import org.junit.jupiter.api.Test;
import test.mysql.MysqlTest;

import java.util.HashMap;
import java.util.Map;

/**
 * 测试表达式解析引擎
 *
 * @author ximu
 */
public class TestExpression extends MysqlTest {


    @Test
    public void test() {
        // 部署流程
        processId = this.deployByResource("test/task/simple.long");
        // 表达式解析引擎 spring-test-mysql.xml
        // Spel表达式 解析器
        testSpelExpression();
        // Juel表达式 解析器
        //testJuelExpression();
    }

    public void testSpelExpression() {
        String expressionFormat = "('this is param: ').concat(#param)";
        // 获取流程使用的解析器
        Expression expression = flowLongEngine.getContext().getExpression();
        Map<String, Object> args = new HashMap<>();
        args.put("param", "value");
        // 解析
        String eval = expression.eval(String.class, expressionFormat, args);
        // 输出
        System.out.println("Spel 解析结果:" + eval);
    }

    public void testJuelExpression() {
        String expressionFormat = "this is param: ${param}";
        // 获取流程使用的解析器
        Expression expression = flowLongEngine.getContext().getExpression();
        Map<String, Object> args = new HashMap<>();
        args.put("param", "value");
        // 解析
        String eval = expression.eval(String.class, expressionFormat, args);
        // 输出
        System.out.println("Juel 解析结果:" + eval);
    }

}
