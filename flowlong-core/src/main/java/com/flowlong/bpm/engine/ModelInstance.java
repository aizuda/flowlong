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
package com.flowlong.bpm.engine;

import com.flowlong.bpm.engine.core.Execution;
import com.flowlong.bpm.engine.core.FlowLongContext;

/**
 * 模型实例接口
 *
 * <p>
 * 尊重知识产权，CV 请保留版权，爱组搭 http://aizuda.com 出品
 * </p>
 *
 * @author hubin
 * @since 1.0
 */
public interface ModelInstance {
    /**
     * 类型：普通任务
     */
    String PERFORM_TYPE_ANY = "ANY";

    /**
     * 类型：参与者fork任务
     */
    String PERFORM_TYPE_ALL = "ALL";

    /**
     * 类型：参与者会签百分比
     */
    String PERFORM_TYPE_PERCENTAGE = "PERCENTAGE";

    /**
     * 类型：主办任务
     */
    String TASK_TYPE_MAJOR = "major";

    /**
     * 类型：协办任务
     */
    String TASK_TYPE_ASSIST = "assist";

    /**
     * 执行流程元素
     *
     * @param flowLongContext 流程引擎上下文
     * @param execution       执行对象
     */
    void execute(FlowLongContext flowLongContext, Execution execution);
}
