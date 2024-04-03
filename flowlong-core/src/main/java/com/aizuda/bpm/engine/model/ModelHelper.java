/*
 * Copyright 2023-2025 Licensed under the AGPL License
 */
package com.aizuda.bpm.engine.model;

import com.aizuda.bpm.engine.assist.ObjectUtils;

import java.util.*;
import java.util.function.BiConsumer;
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
                        nodeNames.addAll(getAllConditionNodeNames(parentNode));
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

    private static List<String> getAllConditionNodeNames(NodeModel nodeModel) {
        List<String> nodeNames = new ArrayList<>();
        if (null != nodeModel) {
            List<ConditionNode> conditionNodes = nodeModel.getConditionNodes();
            if (ObjectUtils.isNotEmpty(conditionNodes)) {
                for (ConditionNode conditionNode : conditionNodes) {
                    NodeModel childNodeMode = conditionNode.getChildNode();
                    if (null != childNodeMode) {
                        if (childNodeMode.conditionNode()) {
                            // 条件路由继续往下找
                            nodeNames.addAll(getAllConditionNodeNames(childNodeMode));
                        } else {
                            // 其它节点找子节点
                            nodeNames.addAll(getAllNextNodeNames(childNodeMode));
                        }
                    }
                }
            }
        }
        return nodeNames;
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
                // 条件节点找子节点
                nodeNames.addAll(getAllConditionNodeNames(nodeModel));
            } else {
                if (!nodeModel.ccNode()) {
                    // 普通节点
                    nodeNames.add(nodeModel.getNodeName());
                }

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
        nodeMap.put("callProcessKey", nodeModel.getCallProcessKey());// 调用外部流程定义 key 唯一标识
        nodeMap.put("type", nodeModel.getType());// 节点类型
        nodeMap.put("setType", nodeModel.getSetType());// 审核人类型
        nodeMap.put("nodeUserList", nodeModel.getNodeUserList());// 审核用户
        nodeMap.put("nodeRoleList", nodeModel.getNodeRoleList());// 审角色
        nodeMap.put("examineLevel", nodeModel.getExamineLevel());// 指定主管层级
        nodeMap.put("directorLevel", nodeModel.getDirectorLevel());// 自定义连续主管审批层级
        nodeMap.put("selectMode", nodeModel.getSelectMode());// 发起人自选类型
        nodeMap.put("termAuto", nodeModel.getTermAuto());// 审批期限超时自动审批
        nodeMap.put("term", nodeModel.getTerm());// 审批期限
        nodeMap.put("termMode", nodeModel.getTermMode());// 审批期限超时后执行类型
        nodeMap.put("examineMode", nodeModel.getExamineMode());// 多人审批时审批方式
        nodeMap.put("directorMode", nodeModel.getDirectorMode());// 连续主管审批方式
        nodeMap.put("passWeight", nodeModel.getPassWeight());// 通过权重
        nodeMap.put("allowSelection", nodeModel.getAllowSelection());// 允许发起人自选抄送人
        nodeMap.put("allowTransfer", nodeModel.getAllowTransfer());// 允许转交
        nodeMap.put("allowAppendNode", nodeModel.getAllowAppendNode());// 允许加签/减签
        nodeMap.put("allowRollback", nodeModel.getAllowRollback());// 允许回退
        nodeMap.put("approveSelf", nodeModel.getApproveSelf());// 审批人与提交人为同一人时
        nodeMap.put("extendConfig", nodeModel.getExtendConfig());// 扩展配置
        if (null != biConsumer) {
            // 自定义处理消费者
            biConsumer.accept(nodeMap, nodeModel);
        }
        return nodeMap;
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
}
