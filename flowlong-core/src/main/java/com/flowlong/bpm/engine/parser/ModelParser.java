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

import com.flowlong.bpm.engine.assist.XmlUtils;
import com.flowlong.bpm.engine.core.FlowLongContext;
import com.flowlong.bpm.engine.exception.FlowLongException;
import com.flowlong.bpm.engine.ModelElement;
import com.flowlong.bpm.engine.model.NodeModel;
import com.flowlong.bpm.engine.model.ProcessModel;
import com.flowlong.bpm.engine.model.TransitionModel;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.function.Function;

/**
 * 流程定义xml文件的模型解析器
 *
 * <p>
 * 尊重知识产权，CV 请保留版权，爱组搭 http://aizuda.com 出品
 * </p>
 *
 * @author hubin
 * @since 1.0
 */
public class ModelParser {

    /**
     * 流程文件字节码解析为流程模型
     *
     * @param bytes 流程定义字节
     */
    public static ProcessModel parse(byte[] bytes) {
        return ModelParser.parse(bytes, element -> {
            NodeParser nodeParser = FlowLongContext.getNodeParser(element.getNodeName());
            return null == nodeParser ? null : nodeParser.parse(element);
        });
    }

    /**
     * 流程文件字节码解析为流程模型
     *
     * @param bytes         流程定义字节
     * @param parseFunction 节点解析处理函数
     */
    public static ProcessModel parse(byte[] bytes, Function<ModelElement, NodeModel> parseFunction) {
        DocumentBuilder documentBuilder = XmlUtils.createDocumentBuilder();
        try {
            Document doc = documentBuilder.parse(new ByteArrayInputStream(bytes));
            ModelElement element = new ModelElement(doc.getDocumentElement());
            ProcessModel processModel = new ProcessModel();
            processModel.setName(element.getAttribute(NodeParser.ATTR_NAME));
            processModel.setDisplayName(element.getAttribute(NodeParser.ATTR_DISPLAY_NAME));
            processModel.setExpireTime(element.getAttribute(NodeParser.ATTR_EXPIRE_TIME));
            processModel.setInstanceUrl(element.getAttribute(NodeParser.ATTR_INSTANCE_URL));
            processModel.setInstanceNoClass(element.getAttribute(NodeParser.ATTR_INSTANCE_NO_CLASS));
            NodeList nodeList = element.getChildNodes();
            int nodeSize = nodeList.getLength();
            for (int i = 0; i < nodeSize; i++) {
                Node node = nodeList.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    NodeModel nodeModel = parseFunction.apply(new ModelElement((Element) node));
                    if (null == nodeModel) {
                        throw new FlowLongException("Unknown node: " + node.getNodeName());
                    }
                    processModel.getNodes().add(nodeModel);
                }
            }

            // 循环节点模型，构造变迁输入、输出的 source、target
            for (NodeModel node : processModel.getNodes()) {
                for (TransitionModel transition : node.getOutputs()) {
                    String to = transition.getTo();
                    for (NodeModel node2 : processModel.getNodes()) {
                        if (to.equalsIgnoreCase(node2.getName())) {
                            node2.getInputs().add(transition);
                            transition.setTarget(node2);
                        }
                    }
                }
            }
            return processModel;
        } catch (SAXException e) {
            e.printStackTrace();
            throw new FlowLongException(e);
        } catch (IOException e) {
            throw new FlowLongException(e);
        }
    }
}
