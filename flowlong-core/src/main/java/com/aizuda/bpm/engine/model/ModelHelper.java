/*
 * Copyright 2023-2025 Licensed under the AGPL License
 */
package com.aizuda.bpm.engine.model;

import com.aizuda.bpm.engine.assist.ObjectUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 流程模型辅助类
 *
 * <p>
 * 尊重知识产权，不允许非法使用，后果自负
 * </p>
 *
 * @author hubin
 * @since 1.0
 */
public class ModelHelper {

    /**
     * 递归查找下一个执行节点
     *
     * @param nodeModel 当前节点
     * @return 流程节点模型
     */
    public static NodeModel findNextNode(NodeModel nodeModel) {
        NodeModel parentNode = nodeModel.getParentNode();
        if (null == parentNode || Objects.equals(0, parentNode.getType())) {
            // 递归至发起节点，流程结束
            return null;
        }

        // 如果当前节点不是条件分支的子节点、而是条件审批的子节点
        if (parentNode.conditionNode()) {
            NodeModel childNode = parentNode.getChildNode();
            if (null != childNode && !Objects.equals(childNode.getNodeName(), nodeModel.getNodeName())) {
                // 条件执行节点，返回子节点
                return childNode;
            }
        }

        // 往上继续找下一个执行节点
        return findNextNode(parentNode);
    }

    /**
     * 获取所有上一个节点名称
     *
     * @param nodeModel 当前节点
     * @return 所有节点名称
     */
    public static List<String> getAllPreviousNodeNames(NodeModel nodeModel) {
        List<String> nodeNames = new ArrayList<>();
        if (null != nodeModel) {
            NodeModel parentNode = nodeModel.getParentNode();
            if (null != parentNode) {
                if (!parentNode.ccNode()) {
                    // 非抄送节点
                    if (parentNode.conditionNode()) {
                        // 条件节点找子节点
                        nodeNames.addAll(getAllNextNodeNames(parentNode));
                    } else {
                        // 普通节点
                        nodeNames.add(parentNode.getNodeName());
                    }
                }
                // 继续找上一个节点
                nodeNames.addAll(getAllPreviousNodeNames(parentNode));
            }
        }
        // 往上递归需要去重
        return nodeNames.stream().distinct().collect(Collectors.toList());
    }

    /**
     * 获取所有下一个节点名称
     *
     * @param nodeModel 当前节点
     * @return 所有节点名称
     */
    public static List<String> getAllNextNodeNames(NodeModel nodeModel) {
        List<String> nodeNames = new ArrayList<>();
        if (null != nodeModel) {
            if (nodeModel.conditionNode()) {
                List<ConditionNode> conditionNodes = nodeModel.getConditionNodes();
                if (ObjectUtils.isNotEmpty(conditionNodes)) {
                    for (ConditionNode conditionNode : conditionNodes) {
                        // 条件节点分支子节点
                        nodeNames.addAll(getAllNextNodeNames(conditionNode.getChildNode()));
                    }
                }

                // 条件节点子节点
                nodeNames.addAll(getAllNextNodeNames(nodeModel.getChildNode()));
            } else {
                // 普通节点
                nodeNames.add(nodeModel.getNodeName());

                // 找子节点
                NodeModel childNodeModel = nodeModel.getChildNode();
                if (null != childNodeModel) {
                    nodeNames.addAll(getAllNextNodeNames(childNodeModel));
                }
            }
        }
        return nodeNames;
    }

    /**
     * 检查是否存在重复节点名称
     *
     * @param nodeModel 节点模型
     * @return true 重复 false 不重复
     */
    public static boolean checkDuplicateNodeNames(NodeModel nodeModel) {
        List<String> allNextNodeNames = getAllNextNodeNames(nodeModel);
        Set<String> set = new HashSet<>();
        for (String nodeName : allNextNodeNames) {
            if (!set.add(nodeName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 检查条件节点
     *
     * @param nodeModel {@link NodeModel}
     * @return 0，合法情况 1，存在多个条件表达式为空 2，存在多个子节点为空
     */
    public static int checkConditionNode(NodeModel nodeModel) {
        if (null != nodeModel) {
            List<ConditionNode> conditionNodes = nodeModel.getConditionNodes();
            if (ObjectUtils.isEmpty(conditionNodes)) {
                return checkConditionNode(nodeModel.getChildNode());
            }
            int i = 0;
            int j = 0;
            for (ConditionNode conditionNode : conditionNodes) {
                List<List<NodeExpression>> conditionList = conditionNode.getConditionList();
                if (ObjectUtils.isEmpty(conditionList)) {
                    i++;
                }
                if (null == conditionNode.getChildNode()) {
                    j++;
                }
                if (i > 1 || j > 1) {
                    break;
                }
            }
            if (i > 1) {
                // 存在多个条件表达式为空
                return 1;
            }
            if (j > 1) {
                // 存在多个子节点为空
                return 2;
            }
            for (ConditionNode conditionNode : conditionNodes) {
                return checkConditionNode(conditionNode.getChildNode());
            }
        }
        // 合法情况
        return 0;
    }
}
