/*
 *  Copyright 2023-2025 www.flowlong.com
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package test.runner;

import com.flowlong.bpm.engine.access.ScriptRunner;
import com.flowlong.bpm.engine.access.jdbc.JdbcHelper;
import org.junit.Test;

import java.sql.Connection;

/**
 * 测试ScriptRunner工具
 *
 * @author hubin
 * @since 1.0
 */
public class TestScriptRunner {
    @Test
    public void test() {
        try {
            Connection conn = JdbcHelper.getConnection(null);
            ScriptRunner runner = new ScriptRunner(conn, true);
            runner.runScript("db/schema-mysql.sql");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
