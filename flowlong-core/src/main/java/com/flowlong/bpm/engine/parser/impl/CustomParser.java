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
package com.flowlong.bpm.engine.parser.impl;

import com.flowlong.bpm.engine.model.CustomModel;
import com.flowlong.bpm.engine.ModelElement;
import com.flowlong.bpm.engine.model.NodeModel;
import com.flowlong.bpm.engine.parser.AbstractNodeParser;

/**
 * 自定义节点解析器
 *
 * <p>
 * 尊重知识产权，CV 请保留版权，爱组搭 http://aizuda.com 出品
 * </p>
 *
 * @author hubin
 * @since 1.0
 */
public class CustomParser extends AbstractNodeParser {

    @Override
    public NodeModel parse(ModelElement element) {
        CustomModel custom = this.parse(new CustomModel(), element);
        custom.setClazz(element.getAttribute(ATTR_CLAZZ));
        custom.setMethodName(element.getAttribute(ATTR_METHOD_NAME));
        custom.setArgs(element.getAttribute(ATTR_ARGS));
        custom.setVar(element.getAttribute(ATTR_VAR));
        return custom;
    }
}
