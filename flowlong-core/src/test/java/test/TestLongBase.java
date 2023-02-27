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
package test;

import com.flowlong.bpm.engine.FlowLongEngine;
import com.flowlong.bpm.engine.QueryService;
import com.flowlong.bpm.engine.core.FlowLongContext;
import com.mysql.cj.jdbc.MysqlDataSource;

/**
 * 测试辅助基类，提供execute的递归方法及LongEngine实例
 *
 * @author hubin
 * @since 1.0
 */
public class TestLongBase {
    protected String processId;
    protected FlowLongEngine engine = getEngine();
    protected QueryService queryService = engine.queryService();

    protected FlowLongEngine getEngine() {
        MysqlDataSource ds = new MysqlDataSource();
        ds.setURL("jdbc:mysql://地址:3306/flowlong?characterEncoding=utf8&useSSL=false");
        ds.setUser("root");
        ds.setPassword("密码");
        return new FlowLongContext().build(ds);
    }

    protected void deployByResource(String resourceName) {
        this.processId = engine.processService().deployByResource(resourceName);
    }
}
