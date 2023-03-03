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
package com.flowlong.bpm.engine.model;

import com.flowlong.bpm.engine.core.FlowLongContext;
import com.flowlong.bpm.engine.core.Execution;
import com.flowlong.bpm.engine.handler.FlowLongHandler;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * 实例模型基本元素
 *
 * <p>
 * 尊重知识产权，CV 请保留版权，爱组搭 http://aizuda.com 出品
 * </p>
 *
 * @author hubin
 * @since 1.0
 */
@Getter
@Setter
public class BaseElement implements Serializable {
    /**
     * 元素名称
     */
    private String name;
    /**
     * 显示名称
     */
    private String displayName;

    /**
     * 将执行对象execution交给具体的处理器处理
     *
     * @param handler         处理器 {@see FlowLongHandler}
     * @param flowLongContext 流程引擎上下文
     * @param execution       执行对象
     */
    protected void fire(FlowLongHandler handler, FlowLongContext flowLongContext, Execution execution) {
        handler.handle(flowLongContext, execution);
    }

}
