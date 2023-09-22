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
package test.mysql.config;

import com.flowlong.bpm.engine.*;
import com.flowlong.bpm.engine.core.FlowLongContext;
import com.flowlong.bpm.engine.impl.GeneralAccessStrategy;
import com.flowlong.bpm.solon.adaptive.SolonFlowJsonHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 测试 Mysql 配置文件
 */
@Configuration
public class MysqlConfig {

    @Bean
    public GeneralAccessStrategy accessStrategy() {
        return new GeneralAccessStrategy();
    }

    @Bean
    public FlowLongEngine flowLongEngine(ProcessService processService, QueryService queryService,
                                         RuntimeService runtimeService, TaskService taskService, Expression expression) {
        FlowLongContext flc = new FlowLongContext();
        flc.setProcessService(processService);
        flc.setQueryService(queryService);
        flc.setRuntimeService(runtimeService);
        flc.setTaskService(taskService);
        flc.setExpression(expression);
        // 静态注入 Jackson 解析 JSON 处理器
        FlowLongContext.setFlowJsonHandler(new SolonFlowJsonHandler());
        return flc.build();
    }
}
