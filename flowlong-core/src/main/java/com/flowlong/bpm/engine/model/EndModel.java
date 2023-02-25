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

import com.flowlong.bpm.engine.core.FlowLongContext;
import com.flowlong.bpm.engine.core.Execution;
import com.flowlong.bpm.engine.handler.impl.EndProcessHandler;

import java.util.Collections;
import java.util.List;

/**
 * 结束节点end元素
 *
 * @author hubin
 * @since 1.0
 */
public class EndModel extends NodeModel {

    @Override
    protected void run(FlowLongContext flowLongContext, Execution execution) {
        fire(new EndProcessHandler(), flowLongContext, execution);
    }

    /**
     * 结束节点无输出变迁
     */
    public List<TransitionModel> getOutputs() {
        return Collections.emptyList();
    }
}
