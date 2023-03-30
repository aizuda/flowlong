package com.flowlong.bpm.engine;

import com.flowlong.bpm.engine.assist.ObjectUtils;
import lombok.Getter;
import lombok.Setter;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Getter
@Setter
public class ModelElement {
    private Element element;

    public ModelElement(Element element) {
        this.element = element;
    }

    public String getAttribute(String name) {
        String value = element.getAttribute(name);
        return ObjectUtils.isNotEmpty(value) ? value : null;
    }


    /**
     * 从element元素查找所有tagName指定的子节点元素集合
     *
     * @param tagName 标签名称
     * @return
     */
    public List<ModelElement> elements(String tagName) {
        if (element == null || !element.hasChildNodes()) {
            return Collections.emptyList();
        }
        List<ModelElement> elements = new ArrayList<>();
        for (Node child = element.getFirstChild(); child != null; child = child.getNextSibling()) {
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                Element childElement = (Element) child;
                String childTagName = childElement.getNodeName();
                if (tagName.equals(childTagName)) {
                    elements.add(new ModelElement(childElement));
                }
            }
        }
        return elements;
    }

    public String getNodeName() {
        return element.getNodeName();
    }

    public NodeList getElementsByTagName(String name) {
        return element.getElementsByTagName(name);
    }

    public NodeList getChildNodes() {
        return element.getChildNodes();
    }
}
