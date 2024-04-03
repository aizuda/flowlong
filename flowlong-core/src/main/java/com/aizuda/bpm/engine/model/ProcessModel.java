/*
 * Copyright 2023-2025 Licensed under the AGPL License
 */
package com.aizuda.bpm.engine.model;

import com.aizuda.bpm.engine.assist.Assert;
import com.aizuda.bpm.engine.core.Execution;
import com.aizuda.bpm.engine.core.FlowLongContext;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

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
     * 执行节点模型
     *
     * @param flowLongContext 流程引擎上下文
     * @param execution       流程执行对象
     * @param nodeName        节点名称
     */
    public boolean executeNodeModel(FlowLongContext flowLongContext, Execution execution, String nodeName) {
        Assert.isNull(this, "FlwProcess modelContent cannot be empty");
        NodeModel nodeModel = this.getNode(nodeName);
        Assert.isNull(nodeModel, "流程模型中未发现，流程节点" + nodeName);
        Optional<NodeModel> executeNodeOptional = nodeModel.nextNode();
        if (executeNodeOptional.isPresent()) {
            // 执行流程节点
            NodeModel executeNode = executeNodeOptional.get();
            return executeNode.execute(flowLongContext, execution);
        }

        /*
         * 无执行节点流程结束
         */
        return execution.endInstance(nodeModel);
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
