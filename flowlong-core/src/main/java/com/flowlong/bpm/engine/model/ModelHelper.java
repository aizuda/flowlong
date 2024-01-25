/*
 * Copyright 2023-2025 Licensed under the AGPL License
 */
package com.flowlong.bpm.engine.model;

import java.util.*;
import java.util.function.BiConsumer;

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
        if (parentNode.isConditionNode()) {
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
     * 获取节点 Map 格式列表
     *
     * @param rootNode   模型根节点
     * @param biConsumer 模型节点处理消费者
     */
    public static List<Map<String, Object>> getNodeMapList(NodeModel rootNode, BiConsumer<Map<String, Object>, NodeModel> biConsumer) {
        List<Map<String, Object>> nodeMapList = new ArrayList<>();
        nodeMapList.add(getNodeMap(rootNode, biConsumer));
        int i = 0;
        buildNodeModel(nodeMapList, rootNode, i, biConsumer);
        return nodeMapList;
    }

    private static void buildNodeModel(List<Map<String, Object>> nodeMapList, NodeModel rootNode, int i,
                                BiConsumer<Map<String, Object>, NodeModel> biConsumer) {
        List<ConditionNode> conditionNodes = rootNode.getConditionNodes();
        if (null != conditionNodes) {
            // 处理条件节点
            Map<String, Object> nodeMap = new HashMap<>();
            nodeMap.put("conditionNode", 1);
            int j = 0;
            i = i + 1;
            List<Map<String, Object>> conditionNodeMapList = new ArrayList<>();
            for (ConditionNode conditionNode : conditionNodes) {
                // 添加条件节点
                Map<String, Object> conditionNodeMap = new HashMap<>();
                j = j + 1;
                conditionNodeMap.put("index", i + "_" + j);// 索引位置
                conditionNodeMap.put("name", conditionNode.getNodeName());
                conditionNodeMap.put("type", conditionNode.getType());
                conditionNodeMap.put("priorityLevel", conditionNode.getPriorityLevel());
                List<Map<String, Object>> conditionChildNodeMapList = new ArrayList<>();

                // 递归条件子节点
                NodeModel conditionChildNode = conditionNode.getChildNode();
                if (null != conditionChildNode) {
                    conditionChildNodeMapList.add(getNodeMap(conditionChildNode, biConsumer));
                    buildNodeModel(conditionChildNodeMapList, conditionChildNode, i, biConsumer);
                }
                conditionNodeMap.put("childNode", conditionChildNodeMapList);
                conditionNodeMapList.add(conditionNodeMap);
            }
            nodeMap.put("conditionNodeList", conditionNodeMapList);
            nodeMapList.add(nodeMap);
        } else {
            // 递归子节点
            NodeModel childNode = rootNode.getChildNode();
            if (null != childNode) {
                if (!Objects.equals(4, childNode.getType())) {
                    nodeMapList.add(getNodeMap(childNode, biConsumer));
                }
                buildNodeModel(nodeMapList, childNode, i, biConsumer);
            }
        }
    }

    private static Map<String, Object> getNodeMap(NodeModel nodeModel, BiConsumer<Map<String, Object>, NodeModel> biConsumer) {
        Map<String, Object> nodeMap = new HashMap<>();
        nodeMap.put("conditionNode", 0);
        nodeMap.put("name", nodeModel.getNodeName());// 节点名称
        nodeMap.put("type", nodeModel.getType());// 节点类型
        nodeMap.put("setType", nodeModel.getSetType());// 审核人类型
        List<NodeAssignee> nodeAssigneeList = null;
        if (Objects.equals(1, nodeModel.getSetType())) {
            nodeAssigneeList = nodeModel.getNodeUserList();
        } else if (Objects.equals(3, nodeModel.getSetType())) {
            nodeAssigneeList = nodeModel.getNodeRoleList();
        }
        nodeMap.put("nodeAssigneeList", nodeAssigneeList);// 审核人
        nodeMap.put("setType", nodeModel.getSetType());// 审核人类型
        nodeMap.put("callProcessKey", nodeModel.getCallProcessKey());// 调用外部流程定义 key 唯一标识
        nodeMap.put("selectMode", nodeModel.getSelectMode());// 发起人自选类型
        nodeMap.put("userSelectFlag", nodeModel.getUserSelectFlag());// 允许发起人自选抄送人
        nodeMap.put("examineMode", nodeModel.getExamineMode());// 多人审批时审批方式
        if (null != biConsumer) {
            // 自定义处理消费者
            biConsumer.accept(nodeMap, nodeModel);
        }
        return nodeMap;
    }
}
