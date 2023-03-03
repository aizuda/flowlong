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

import com.flowlong.bpm.engine.assist.ConfigHelper;
import com.flowlong.bpm.engine.assist.StringUtils;
import com.flowlong.bpm.engine.model.NodeModel;
import com.flowlong.bpm.engine.model.SubProcessModel;
import com.flowlong.bpm.engine.parser.AbstractNodeParser;
import org.apache.commons.lang.math.NumberUtils;
import org.w3c.dom.Element;

/**
 * 子流程节点解析类
 *
 * <p>
 * 尊重知识产权，CV 请保留版权，爱组搭 http://aizuda.com 出品
 * </p>
 *
 * @author hubin
 * @since 1.0
 */
public class SubProcessParser extends AbstractNodeParser {

    @Override
    public NodeModel parse(Element element) {
        SubProcessModel model = this.parse(new SubProcessModel(), element);
        model.setProcessName(element.getAttribute(ATTR_PROCESS_NAME));
        String version = element.getAttribute(ATTR_VERSION);
        int ver = 0;
        if (NumberUtils.isNumber(version)) {
            ver = Integer.parseInt(version);
        }
        model.setVersion(ver);
        String form = element.getAttribute(ATTR_FORM);
        if (StringUtils.isNotEmpty(form)) {
            model.setForm(form);
        } else {
            model.setForm(ConfigHelper.getProperty("subprocessurl"));
        }
        return model;
    }
}
