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

import com.flowlong.bpm.engine.model.FieldModel;
import com.flowlong.bpm.engine.model.NodeModel;
import com.flowlong.bpm.engine.model.TaskModel;
import com.flowlong.bpm.engine.parser.AbstractNodeParser;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;

/**
 * 任务节点解析类
 *
 * <p>
 * 尊重知识产权，CV 请保留版权，爱组搭 http://aizuda.com 出品
 * </p>
 *
 * @author hubin
 * @since 1.0
 */
public class TaskParser extends AbstractNodeParser {

    @Override
    public NodeModel parse(Element element) {
        TaskModel task = this.parse(new TaskModel(), element);
        task.setForm(element.getAttribute(ATTR_FORM));
        task.setAssignee(element.getAttribute(ATTR_ASSIGNEE));
        task.setExpireTime(element.getAttribute(ATTR_EXPIRE_TIME));
        task.setAutoExecute(element.getAttribute(ATTR_AUTO_EXECUTE));
        task.setReminderTime(element.getAttribute(ATTR_REMINDER_TIME));
        task.setReminderRepeat(element.getAttribute(ATTR_REMINDER_REPEAT));
        task.setPerformType(element.getAttribute(ATTR_PERFORM_TYPE));
        task.setTaskType(element.getAttribute(ATTR_TASK_TYPE));
        task.setAssignmentHandler(element.getAttribute(ATTR_ASSIGNEE_HANDLER));
        NodeList fieldList = element.getElementsByTagName(ATTR_FIELD);
        List<FieldModel> fields = new ArrayList<FieldModel>();
        for (int i = 0; i < fieldList.getLength(); i++) {
            Element item = (Element) fieldList.item(i);
            FieldModel fieldModel = new FieldModel();
            fieldModel.setName(item.getAttribute(ATTR_NAME));
            fieldModel.setDisplayName(item.getAttribute(ATTR_DISPLAY_NAME));
            fieldModel.setType(item.getAttribute(ATTR_TYPE));
            NodeList attrList = item.getElementsByTagName(ATTR_ATTR);
            for (int j = 0; j < attrList.getLength(); j++) {
                Node attr = attrList.item(j);
                fieldModel.addAttr(((Element) attr).getAttribute(ATTR_NAME),
                        ((Element) attr).getAttribute(ATTR_VALUE));
            }
            fields.add(fieldModel);
        }
        task.setFields(fields);
        return task;
    }
}
