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
package test.task.interceptor;

import com.flowlong.bpm.engine.FlowLongInterceptor;
import com.flowlong.bpm.engine.core.FlowLongContext;
import com.flowlong.bpm.engine.core.Execution;
import com.flowlong.bpm.engine.entity.Task;
import lombok.extern.slf4j.Slf4j;

/**
 * @author hubin
 * @since 1.0
 */
@Slf4j
public class LocalTaskInterceptorFlow implements FlowLongInterceptor {

    @Override
    public void intercept(FlowLongContext flowLongContext, Execution execution) {
        if (log.isInfoEnabled()) {
            log.info("LocalTaskInterceptorFlow start...");
            for (Task task : execution.getTasks()) {
                StringBuffer buffer = new StringBuffer(100);
                buffer.append("创建任务[标识=").append(task.getId());
                buffer.append(",名称=").append(task.getDisplayName());
                buffer.append(",创建时间=").append(task.getCreateTime());
                buffer.append(",参与者={");
                if (task.getActorIds() != null) {
                    for (String actor : task.getActorIds()) {
                        buffer.append(actor).append(";");
                    }
                }
                buffer.append("}]");
                log.info(buffer.toString());
            }
            log.info("LocalTaskInterceptorFlow finish...");
        }
    }

}
