/*
 * Copyright 2023-2025 Licensed under the apache-2.0 License
 * website: https://aizuda.com
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
 * <a href="https://aizuda.com">官网</a>尊重知识产权，不允许非法使用，后果自负
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
     * 获取process定义的指定节点key的节点模型
     *
     * @param nodeKey 节点key
     * @return {@link NodeModel}
     */
    public NodeModel getNode(String nodeKey) {
        return null == nodeConfig ? null : nodeConfig.getNode(nodeKey);
    }

    /**
     * 构建父节点
     *
     * @param rootNode 根节点
     */
    public void buildParentNode(NodeModel rootNode) {
        // 条件分支
        this.buildParentConditionNodes(rootNode, rootNode.getConditionNodes());

        // 并行分支
        List<NodeModel> parallelNodes = rootNode.getParallelNodes();
        if (null != parallelNodes) {
            for (NodeModel nodeModel : parallelNodes) {
                nodeModel.setParentNode(rootNode);
                this.buildParentNode(nodeModel);
            }
        }

        // 包容分支
        this.buildParentConditionNodes(rootNode, rootNode.getInclusiveNodes());

        // 子节点
        NodeModel childNode = rootNode.getChildNode();
        if (null != childNode) {
            childNode.setParentNode(rootNode);
            this.buildParentNode(childNode);
        }
    }

    /**
     * 构建条件节点的父节点
     *
     * @param rootNode       根节点
     * @param conditionNodes 条件节点
     */
    private void buildParentConditionNodes(NodeModel rootNode, List<ConditionNode> conditionNodes) {
        if (null != conditionNodes) {
            for (ConditionNode conditionNode : conditionNodes) {
                NodeModel conditionChildNode = conditionNode.getChildNode();
                if (null != conditionChildNode) {
                    conditionChildNode.setParentNode(rootNode);
                    this.buildParentNode(conditionChildNode);
                }
            }
        }
    }

    /**
     * 清理父节点关系
     *
     * @param rootNode 根节点
     */
    public void cleanParentNode(NodeModel rootNode) {
        rootNode.setParentNode(null);

        // 清理条件节点
        this.cleanConditionParentNode(rootNode.getConditionNodes());

        // 清理并分支
        List<NodeModel> parallelNodes = rootNode.getParallelNodes();
        if (null != parallelNodes) {
            for (NodeModel nodeModel : parallelNodes) {
                this.cleanParentNode(nodeModel);
            }
        }

        // 清理包容分支
        this.cleanConditionParentNode(rootNode.getInclusiveNodes());

        // 清理子节点
        NodeModel childNode = rootNode.getChildNode();
        if (null != childNode) {
            this.cleanParentNode(childNode);
        }
    }

    /**
     * 清理条件节点的父节点
     *
     * @param conditionNodes 条件节点
     */
    protected void cleanConditionParentNode(List<ConditionNode> conditionNodes) {
        if (null != conditionNodes) {
            for (ConditionNode conditionNode : conditionNodes) {
                NodeModel conditionChildNode = conditionNode.getChildNode();
                if (null != conditionChildNode) {
                    this.cleanParentNode(conditionChildNode);
                }
            }
        }
    }
}
