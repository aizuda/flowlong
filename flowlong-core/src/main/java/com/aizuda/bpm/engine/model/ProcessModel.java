/*
 * Copyright 2023-2025 Licensed under the AGPL License
 */
package com.aizuda.bpm.engine.model;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

/**
 * JSON BPM 模型
 *
 * <p>
 * 尊重知识产权，不允许非法使用，后果自负
 * </p>
 *
 * @author hubin
 * @since 1.0
 */
@Getter
@Setter
public class ProcessModel implements Serializable {
    /**
     * 节点名称
     */
    private String name;
    /**
     * 流程 key
     */
    private String key;
    /**
     * 实例地址
     */
    private String instanceUrl;
    /**
     * 节点信息
     */
    private NodeModel nodeConfig;

    /**
     * 获取process定义的指定节点名称的节点模型
     *
     * @param nodeName 节点名称
     * @return {@link NodeModel}
     */
    public NodeModel getNode(String nodeName) {
        return null == nodeConfig ? null : nodeConfig.getNode(nodeName);
    }

    /**
     * 构建父节点
     *
     * @param rootNode 根节点
     */
    public void buildParentNode(NodeModel rootNode) {
        List<ConditionNode> conditionNodes = rootNode.getConditionNodes();
        if (null != conditionNodes) {
            for (ConditionNode conditionNode : conditionNodes) {
                NodeModel conditionChildNode = conditionNode.getChildNode();
                if (null != conditionChildNode) {
                    conditionChildNode.setParentNode(rootNode);
                    this.buildParentNode(conditionChildNode);
                }
            }
        }
        NodeModel childNode = rootNode.getChildNode();
        if (null != childNode) {
            childNode.setParentNode(rootNode);
            this.buildParentNode(childNode);
        }
    }

    /**
     * 清理父节点关系
     */
    public void cleanParentNode(NodeModel rootNode) {
        rootNode.setParentNode(null);
        // 清理条件节点
        List<ConditionNode> conditionNodes = rootNode.getConditionNodes();
        if (null != conditionNodes) {
            for (ConditionNode conditionNode : conditionNodes) {
                NodeModel conditionChildNode = conditionNode.getChildNode();
                if (null != conditionChildNode) {
                    this.cleanParentNode(conditionChildNode);
                }
            }
        }
        // 清理子节点
        NodeModel childNode = rootNode.getChildNode();
        if (null != childNode) {
            childNode.setParentNode(null);
            this.cleanParentNode(childNode);
        }
    }
}
