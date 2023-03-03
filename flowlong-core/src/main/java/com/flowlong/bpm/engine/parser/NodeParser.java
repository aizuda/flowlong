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
package com.flowlong.bpm.engine.parser;

import com.flowlong.bpm.engine.model.NodeModel;
import org.w3c.dom.Element;

/**
 * 节点解析接口
 *
 * <p>
 * 尊重知识产权，CV 请保留版权，爱组搭 http://aizuda.com 出品
 * </p>
 *
 * @author hubin
 * @since 1.0
 */
public interface NodeParser {
    /**
     * 变迁节点名称
     */
    String NODE_TRANSITION = "transition";
    /**
     * 节点属性名称
     */
    String ATTR_NAME = "name";
    String ATTR_DISPLAY_NAME = "displayName";
    String ATTR_INSTANCE_URL = "instanceUrl";
    String ATTR_INSTANCE_NO_CLASS = "instanceNoClass";
    String ATTR_EXPR = "expr";
    String ATTR_HANDLE_CLASS = "handleClass";
    String ATTR_FORM = "form";
    String ATTR_FIELD = "field";
    String ATTR_VALUE = "value";
    String ATTR_ATTR = "attr";
    String ATTR_TYPE = "type";
    String ATTR_ASSIGNEE = "assignee";
    String ATTR_ASSIGNEE_HANDLER = "assignmentHandler";
    String ATTR_PERFORM_TYPE = "performType";
    String ATTR_TASK_TYPE = "taskType";
    String ATTR_TO = "to";
    String ATTR_PROCESS_NAME = "processName";
    String ATTR_VERSION = "version";
    String ATTR_EXPIRE_TIME = "expireTime";
    String ATTR_AUTO_EXECUTE = "autoExecute";
    String ATTR_CALLBACK = "callback";
    String ATTR_REMINDER_TIME = "reminderTime";
    String ATTR_REMINDER_REPEAT = "reminderRepeat";
    String ATTR_CLAZZ = "clazz";
    String ATTR_METHOD_NAME = "methodName";
    String ATTR_ARGS = "args";
    String ATTR_VAR = "var";
    String ATTR_LAYOUT = "layout";
    String ATTR_G = "g";
    String ATTR_OFFSET = "offset";
    String ATTR_PRE_INTERCEPTORS = "preInterceptors";
    String ATTR_POST_INTERCEPTORS = "postInterceptors";

    /**
     * 节点dom元素解析方法，由实现类完成解析
     *
     * @param element dom元素 {@see Element}
     * @return 返回节点模型实现对象 {@see ModeModel}
     */
    NodeModel parse(Element element);

}
