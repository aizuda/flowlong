/*
 * Copyright 2023-2025 Licensed under the AGPL License
 */
package com.aizuda.bpm.engine.model;

import com.aizuda.bpm.engine.FlowConstants;
import com.aizuda.bpm.engine.FlowDataTransfer;
import com.aizuda.bpm.engine.assist.ObjectUtils;
import com.aizuda.bpm.engine.core.enums.NodeSetType;
import com.aizuda.bpm.engine.core.enums.TaskType;

import java.util.*;
import java.util.function.Consumer;
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
     * @param nodeModel   当前节点
     * @param currentTask 当前任务列表
     * @return 流程节点模型
     */
    public static NodeModel findNextNode(NodeModel nodeModel, List<String> currentTask) {
        NodeModel parentNode = nodeModel.getParentNode();
        if (null == parentNode || Objects.equals(0, parentNode.getType())) {
            // 递归至发起节点，流程结束
            return null;
        }

        // 如果当前节点不是条件分支的子节点、而是条件审批的子节点
        if (parentNode.conditionNode()) {
            NodeModel childNode = parentNode.getChildNode();
            if (null != childNode && !Objects.equals(childNode.getNodeKey(), nodeModel.getNodeKey())) {
                // 条件执行节点，返回子节点
                return childNode;
            }
        }

        // 判断当前节点为并行分支或包容分支，需要判断当前并行是否走完
        if (parentNode.parallelNode() || parentNode.inclusiveNode()) {
            // 只是找下一个节点
            if (null == currentTask) {
                return parentNode.getChildNode();
            }
            // 找到另外的分支，看是否列表有执行，有就不能返回 childNode
            if (Collections.disjoint(currentTask, getAllNextConditionNodeKeys(parentNode))) {
                NodeModel childNode = parentNode.getChildNode();
                if (null != childNode && Objects.equals(childNode.getNodeKey(), nodeModel.getNodeKey())) {
                    // 父节点的子节点是当前节点，执行结束
                    return null;
                }
                // 分支执行结束，执行子节点
                return childNode;
            }

            // 分支未执行完
            return null;
        }

        // 往上继续找下一个执行节点
        return findNextNode(parentNode, currentTask);
    }

    /**
     * 获取所有上一个节点key，不包含抄送节点
     *
     * @param nodeModel 当前节点
     * @return 所有节点key
     */
    public static List<String> getAllPreviousNodeKeys(NodeModel nodeModel) {
        List<String> getNodeKeys = getAllParentNodeKeys(nodeModel.getNodeKey(), nodeModel.getParentNode());
        // 往上递归需要去重
        return getNodeKeys.stream().distinct().collect(Collectors.toList());
    }

    private static List<String> getAllParentNodeKeys(String currentNodeKey, NodeModel nodeModel) {
        List<String> nodeKeys = new ArrayList<>();
        if (null != nodeModel) {
            if (!nodeModel.ccNode()) {
                // 非抄送节点
                if (nodeModel.conditionNode()) {
                    // 条件节点找子节点
                    nodeKeys.addAll(getAllConditionNodeKeys(currentNodeKey, nodeModel));
                } else {
                    // 普通节点
                    nodeKeys.add(nodeModel.getNodeKey());
                }
            }
            // 继续找上一个节点
            nodeKeys.addAll(getAllParentNodeKeys(currentNodeKey, nodeModel.getParentNode()));
        }
        return nodeKeys;
    }

    private static List<String> getAllConditionNodeKeys(String currentNodeKey, NodeModel nodeModel) {
        List<String> nodeKeys = new ArrayList<>();
        if (null != nodeModel) {
            List<ConditionNode> conditionNodes = nodeModel.getConditionNodes();
            if (ObjectUtils.isNotEmpty(conditionNodes)) {
                for (ConditionNode conditionNode : conditionNodes) {
                    NodeModel childNodeMode = conditionNode.getChildNode();
                    if (null != childNodeMode) {
                        if (childNodeMode.conditionNode()) {
                            // 条件路由继续往下找
                            nodeKeys.addAll(getAllConditionNodeKeys(currentNodeKey, childNodeMode));
                        } else {
                            // 其它节点找子节点，必须包含当前节点的子节点分支
                            List<String> allNextNodeKeys = getAllNextConditionNodeKeys(childNodeMode);
                            if (allNextNodeKeys.contains(currentNodeKey)) {
                                List<String> legalNodeKeys = new ArrayList<>();
                                for (String t : allNextNodeKeys) {
                                    if (currentNodeKey.equals(t)) {
                                        break;
                                    }
                                    legalNodeKeys.add(t);
                                }
                                nodeKeys.addAll(legalNodeKeys);
                            }
                        }
                    }
                }
            }
        }
        return nodeKeys;
    }

    private static List<String> getAllNextConditionNodeKeys(NodeModel nodeModel) {
        List<String> nodeKeys = new ArrayList<>();
        if (null != nodeModel) {
            if (nodeModel.conditionNode()) {
                List<ConditionNode> conditionNodes = nodeModel.getConditionNodes();
                if (ObjectUtils.isNotEmpty(conditionNodes)) {
                    for (ConditionNode conditionNode : conditionNodes) {
                        // 条件节点分支子节点
                        nodeKeys.addAll(getAllNextConditionNodeKeys(conditionNode.getChildNode()));
                    }
                }

                // 条件节点子节点
                nodeKeys.addAll(getAllNextConditionNodeKeys(nodeModel.getChildNode()));
            } else if (nodeModel.parallelNode()) {
                // 并行节点
                for (NodeModel node : nodeModel.getParallelNodes()) {
                    nodeKeys.addAll(getAllNextConditionNodeKeys(node));
                }
            } else if (nodeModel.inclusiveNode()) {
                // 包容节点
                for (ConditionNode conditionNode : nodeModel.getInclusiveNodes()) {
                    nodeKeys.addAll(getAllNextConditionNodeKeys(conditionNode.getChildNode()));
                }
            } else {
                if (!nodeModel.ccNode()) {
                    // 非抄送节点
                    nodeKeys.add(nodeModel.getNodeKey());
                }

                // 找子节点
                NodeModel childNodeModel = nodeModel.getChildNode();
                if (null != childNodeModel) {
                    nodeKeys.addAll(getAllNextConditionNodeKeys(childNodeModel));
                }
            }
        }
        return nodeKeys;
    }

    /**
     * 获取所有未设置处理人员节点【非发起人自己，只包含 1，审批 2，抄送 节点】
     *
     * @param rootNodeModel 根节点模型
     * @return 所有节点名称
     */
    public static List<NodeModel> getUnsetAssigneeNodes(NodeModel rootNodeModel) {
        List<NodeModel> nodeModels = getRootNodeAllChildNodes(rootNodeModel);
        // 过滤发起和结束节点
        return nodeModels.stream().filter(t -> ObjectUtils.isEmpty(t.getNodeAssigneeList()) && NodeSetType.initiatorThemselves.ne(t.getSetType()) && (TaskType.approval.eq(t.getType()) || TaskType.cc.eq(t.getType()))).collect(Collectors.toList());
    }

    /**
     * 获取根节点下的所有节点类型【 注意，只对根节点查找有效！】
     *
     * @param rootNodeModel 根节点模型
     * @return 所有节点信息
     */
    public static List<NodeModel> getRootNodeAllChildNodes(NodeModel rootNodeModel) {
        List<NodeModel> nodeModels = new ArrayList<>();
        if (null != rootNodeModel) {
            if (rootNodeModel.conditionNode()) {
                List<ConditionNode> conditionNodes = rootNodeModel.getConditionNodes();
                if (ObjectUtils.isNotEmpty(conditionNodes)) {
                    for (ConditionNode conditionNode : conditionNodes) {
                        // 条件节点分支子节点
                        nodeModels.addAll(getRootNodeAllChildNodes(conditionNode.getChildNode()));
                    }
                }

                // 条件节点子节点
                nodeModels.addAll(getRootNodeAllChildNodes(rootNodeModel.getChildNode()));
            } else if (rootNodeModel.parallelNode()) {
                // 并行节点
                for (NodeModel nodeModel : rootNodeModel.getParallelNodes()) {
                    nodeModels.addAll(getRootNodeAllChildNodes(nodeModel));
                }
            } else if (rootNodeModel.inclusiveNode()) {
                // 包容节点
                for (ConditionNode conditionNode : rootNodeModel.getInclusiveNodes()) {
                    nodeModels.addAll(getRootNodeAllChildNodes(conditionNode.getChildNode()));
                }
            } else {
                // 普通节点
                nodeModels.add(rootNodeModel);

                // 找子节点
                NodeModel childNodeModel = rootNodeModel.getChildNode();
                if (null != childNodeModel) {
                    nodeModels.addAll(getRootNodeAllChildNodes(childNodeModel));
                }
            }
        }
        return nodeModels;
    }

    /**
     * 检查是否存在重复节点名称
     *
     * @param rootNodeModel 根节点模型
     * @return true 重复 false 不重复
     */
    public static boolean checkDuplicateNodeKeys(NodeModel rootNodeModel) {
        List<NodeModel> allNextNodes = getRootNodeAllChildNodes(rootNodeModel);
        Set<String> set = new HashSet<>();
        for (NodeModel nextNode : allNextNodes) {
            if (!set.add(nextNode.getNodeKey())) {
                return true;
            }
        }
        return false;
    }

    /**
     * 检查条件节点
     *
     * @param nodeModel {@link NodeModel}
     * @return 0，合法情况 1，存在多个条件表达式为空 2，存在多个条件子节点为空 3，存在条件节点KEY重复
     */
    public static int checkConditionNode(NodeModel nodeModel) {
        if (null != nodeModel) {
            List<ConditionNode> conditionNodes = nodeModel.getConditionNodes();
            if (ObjectUtils.isEmpty(conditionNodes)) {
                return checkConditionNode(nodeModel.getChildNode());
            }
            int i = 0;
            int j = 0;
            Set<String> nodeKeys = new HashSet<>();
            for (ConditionNode conditionNode : conditionNodes) {
                if (!nodeKeys.add(conditionNode.getNodeKey())) {
                    // 存在节点KEY重复
                    return 3;
                }
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

    /**
     * 检查是否存在审批节点
     *
     * @param rootNodeModel 根节点模型
     * @return true 存在 false 不存在
     */
    public static boolean checkExistApprovalNode(NodeModel rootNodeModel) {
        List<NodeModel> allNextNodes = getRootNodeAllChildNodes(rootNodeModel);
        return allNextNodes.stream().anyMatch(t -> TaskType.approval.eq(t.getType()));
    }

    /**
     * 生成节点KEY规则（flk + 时间戳）
     *
     * @return 节点KEY
     */
    public static String generateNodeKey() {
        return "flk" + System.currentTimeMillis();
    }

    /**
     * 获取动态分配处理人员
     *
     * @param rootNodeModel 根节点模型
     * @return 动态分配处理人员
     */
    public static Map<String, DynamicAssignee> getAssigneeMap(NodeModel rootNodeModel) {
        return getRootNodeAllChildNodes(rootNodeModel).stream().collect(Collectors.toMap(NodeModel::getNodeKey, DynamicAssignee::ofNodeModel));
    }

    /**
     * 获取指定节点KEY模型信息
     *
     * @param nodeKey       节点 KEY
     * @param rootNodeModel 根节点模型
     * @return JSON BPM 节点
     */
    public static NodeModel getNodeModel(String nodeKey, NodeModel rootNodeModel) {
        return getRootNodeAllChildNodes(rootNodeModel).stream().filter(e -> Objects.equals(nodeKey, e.getNodeKey())).findFirst().orElse(null);
    }

    /**
     * 重新加载流程模型
     *
     * @param processModel 流程模型
     * @param consumer     流程模型消费
     */
    public static void reloadProcessModel(ProcessModel processModel, Consumer<ProcessModel> consumer) {
        Map<String, Object> modelData = FlowDataTransfer.get(FlowConstants.processDynamicAssignee);
        if (ObjectUtils.isNotEmpty(modelData)) {

            // 追加动态分配处理人员
            modelData.forEach((key, value) -> {
                if (value instanceof DynamicAssignee) {
                    NodeModel thisNodeModel = processModel.getNode(key);
                    if (null != thisNodeModel) {
                        DynamicAssignee dynamicAssignee = (DynamicAssignee) value;
                        thisNodeModel.setNodeAssigneeList(dynamicAssignee.getAssigneeList());
                    }
                }
            });

            // 清理父节点
            processModel.cleanParentNode(processModel.getNodeConfig());

            // 更新模型
            consumer.accept(processModel);
        }
    }
}
