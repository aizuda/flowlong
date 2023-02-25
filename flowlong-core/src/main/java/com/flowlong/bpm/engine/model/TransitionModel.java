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
package com.flowlong.bpm.engine.model;

import com.flowlong.bpm.engine.ModelInstance;
import com.flowlong.bpm.engine.core.FlowLongContext;
import com.flowlong.bpm.engine.core.Execution;
import com.flowlong.bpm.engine.handler.impl.CreateTaskHandler;
import com.flowlong.bpm.engine.handler.impl.StartSubProcessHandler;
import lombok.Getter;
import lombok.Setter;

/**
 * 变迁定义transition元素
 *
 * @author hubin
 * @since 1.0
 */
@Getter
@Setter
public class TransitionModel extends BaseElement implements ModelInstance {
    /**
     * 变迁的源节点引用
     */
    private NodeModel source;
    /**
     * 变迁的目标节点引用
     */
    private NodeModel target;
    /**
     * 变迁的目标节点name名称
     */
    private String to;
    /**
     * 变迁的条件表达式，用于decision
     */
    private String expr;
    /**
     * 转折点图形数据
     */
    private String g;
    /**
     * 描述便宜位置
     */
    private String offset;
    /**
     * 当前变迁路径是否可用
     */
    private boolean enabled = false;

    @Override
    public void execute(FlowLongContext flowLongContext, Execution execution) {
        if (!enabled) return;
        if (target instanceof TaskModel) {
            //如果目标节点模型为TaskModel，则创建task
            fire(new CreateTaskHandler((TaskModel) target), flowLongContext, execution);
        } else if (target instanceof SubProcessModel) {
            //如果目标节点模型为SubProcessModel，则启动子流程
            fire(new StartSubProcessHandler((SubProcessModel) target), flowLongContext, execution);
        } else {
            //如果目标节点模型为其它控制类型，则继续由目标节点执行
            target.execute(flowLongContext, execution);
        }
    }
}
