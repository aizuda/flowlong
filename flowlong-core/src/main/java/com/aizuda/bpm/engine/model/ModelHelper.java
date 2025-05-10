/*
 * Copyright 2023-2025 Licensed under the Dual Licensing
 * website: https://aizuda.com
 */
package com.aizuda.bpm.engine.model;

import com.aizuda.bpm.engine.FlowConstants;
import com.aizuda.bpm.engine.FlowDataTransfer;
import com.aizuda.bpm.engine.assist.Assert;
import com.aizuda.bpm.engine.assist.ObjectUtils;
import com.aizuda.bpm.engine.core.Execution;
import com.aizuda.bpm.engine.core.FlowLongContext;
import com.aizuda.bpm.engine.core.enums.NodeSetType;
import com.aizuda.bpm.engine.core.enums.TaskType;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * 流程模型辅助类
 *
 * <p>
 * <a href="https://aizuda.com">官网</a>尊重知识产权，不允许非法使用，后果自负
 * </p>
 *
 * @author hubin
 * @since 1.0
 */
public class ModelHelper {

    /**
     * 构建流程模型
     * <p>确保已经实现JSON解析处理器接口</p>
     *
     * @param jsonModel 流程模型JSON格式
     * @return 流程模型
     */
    public static ProcessModel buildProcessModel(String jsonModel) {
        ProcessModel pm = FlowLongContext.fromJson(jsonModel, ProcessModel.class);
        pm.buildParentNode(pm.getNodeConfig());
        return pm;
    }

    /**
     * 动态获取下一个节点
     *
     * @param flowLongContext 流程上下文 {@link FlowLongContext}
     * @param execution       流程执行对象 {@link Execution}
     * @param rootNodeModel   根节点
     * @param currentNodeKey  当前节点
     * @return 下一个节点集合
     */
    public static List<NodeModel> getNextChildNodes(FlowLongContext flowLongContext, Execution execution, NodeModel rootNodeModel, String currentNodeKey) {
        NodeModel currentNodeModel = rootNodeModel.getNode(currentNodeKey);
        if (currentNodeModel.approvalOrMajor()) {
            // 审批节点
            NodeModel childNode = currentNodeModel.getChildNode();
            if (null == childNode) {
                // 子节点不存在，可能是结束节点
                return getChildNode(flowLongContext, execution, rootNodeModel, currentNodeModel);
            }

            // 获取下一个待执行子节点
            return getNextChildNodes(flowLongContext, execution, rootNodeModel, childNode);
        }
        return null;
    }

    private static List<NodeModel> getNextChildNodes(FlowLongContext flowLongContext, Execution execution, NodeModel rootNodeModel, NodeModel childNode) {
        List<NodeModel> nextNodes = new ArrayList<>();
        if (childNode.conditionNode()) {
            // 条件节点
            flowLongContext.getFlowConditionHandler().getConditionNode(flowLongContext, execution, childNode)
                    // 添加执行条件节点
                    .ifPresent(t -> {
                        NodeModel _childNode = t.getChildNode();
                        if (null != _childNode) {
                            nextNodes.add(_childNode);
                        } else if (null != childNode.getChildNode()) {
                            // 默认条件，找下一个审批节点
                            nextNodes.addAll(getNextChildNodes(flowLongContext, execution, rootNodeModel, childNode.getChildNode()));
                        }
                    });
        } else if (childNode.parallelNode()) {
            // 并行节点
            childNode.getParallelNodes().forEach(t -> {
                NodeModel _childNode = t.getChildNode();
                if (null != _childNode) {
                    nextNodes.add(_childNode);
                }
            });
        } else if (childNode.inclusiveNode()) {
            // 包容节点
            flowLongContext.getFlowConditionHandler().getInclusiveNodes(flowLongContext, execution, childNode).ifPresent(optList -> {
                if (Objects.equals(1, optList.size()) && null == optList.get(0).getChildNode()) {
                    // 获取包容分支子节点
                    nextNodes.add(childNode.getChildNode());
                } else {
                    // 添加执行条件节点
                    optList.forEach(t -> nextNodes.add(t.getChildNode()));
                }
            });
        } else if (childNode.routeNode()) {
            // 路由节点
            Optional<ConditionNode> opt = flowLongContext.getFlowConditionHandler().getRouteNode(flowLongContext, execution, childNode);
            if (opt.isPresent()) {
                // 添加执行条件节点
                nextNodes.add(rootNodeModel.getNode(opt.get().getNodeKey()));
            } else if (null != childNode.getChildNode()) {
                // 获取路由分支子节点
                nextNodes.addAll(getNextChildNodes(flowLongContext, execution, rootNodeModel, childNode.getChildNode()));
            }
        } else if (TaskType.timer.eq(childNode.getType()) || TaskType.trigger.eq(childNode.getType()) || TaskType.callProcess.eq(childNode.getType())) {
            // 定时器任务或者触发器，添加当前节点（用于提示）
            nextNodes.add(childNode);
            // 找下一个审批节点（可能存在自选设置）
            nextNodes.addAll(getNextChildNodes(flowLongContext, execution, rootNodeModel, childNode.getChildNode()));
        } else if (!TaskType.end.eq(childNode.getType())) {
            // 普通节点
            nextNodes.add(childNode);
        }
        return nextNodes;
    }

    private static List<NodeModel> getChildNode(FlowLongContext flowLongContext, Execution execution, NodeModel rootNodeModel, NodeModel nodeModel) {
        List<NodeModel> nextNodes = new ArrayList<>();
        NodeModel parentNode = nodeModel.getParentNode();
        if (null == parentNode || TaskType.major.eq(parentNode.getType())) {
            // 递归至发起节点，流程结束
            return nextNodes;
        }
        if (parentNode.conditionNode()) {
            NodeModel parentChildNode = parentNode.getChildNode();
            if (null == parentChildNode || Objects.equals(parentChildNode.getNodeKey(), nodeModel.getNodeKey())) {
                // 继续查找上级节点
                return getChildNode(flowLongContext, execution, rootNodeModel, parentNode);
            } else {
                // 条件执行节点，返回子节点
                nextNodes.addAll(getNextChildNodes(flowLongContext, execution, rootNodeModel, parentChildNode));
            }
        } else if (parentNode.parallelNode()) {
            // 并行分支
            nextNodes.add(parentNode.getChildNode());
        } else if (parentNode.inclusiveNode()) {
            // 包容分支
            nextNodes.add(parentNode.getChildNode());
        } else if (parentNode.routeNode()) {
            // 路由分支
            flowLongContext.getFlowConditionHandler().getRouteNode(flowLongContext, execution, parentNode)
                    // 添加执行条件节点
                    .ifPresent(t -> nextNodes.add(parentNode.getNode(t.getNodeKey())));
        }
        return nextNodes;
    }

    /**
     * 递归查找下一个执行节点
     *
     * @param nodeModel   当前节点
     * @param currentTask 当前任务列表
     * @return 流程节点模型
     */
    public static NodeModel findNextNode(NodeModel nodeModel, List<String> currentTask) {
        NodeModel parentNode = nodeModel.getParentNode();
        if (null == parentNode || TaskType.major.eq(parentNode.getType())) {
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
     * 获取所有上一个节点key，只包含发起节点和审批节点（非直接所在条件分支排除在外）
     *
     * @param nodeModel 当前节点
     * @return 所有节点key
     */
    public static List<String> getAllPreviousNodeKeys(NodeModel nodeModel) {
        List<String> nodeKeys = new ArrayList<>();
        List<NodeModel> allParentNodeModels = getAllParentNodeModels(nodeModel);
        if (!allParentNodeModels.isEmpty()) {
            for (NodeModel parentNodeModel : allParentNodeModels) {
                Integer type = parentNodeModel.getType();
                if (TaskType.major.eq(type) || TaskType.approval.eq(type)) {
                    // 发起或审批节点
                    nodeKeys.add(parentNodeModel.getNodeKey());
                }
            }
        }
        return nodeKeys;
    }

    /**
     * 获取当前节点的所有父节点模型
     *
     * @param nodeModel 当前节点模型
     * @return 所有父节点模型
     */
    private static List<NodeModel> getAllParentNodeModels(NodeModel nodeModel) {
        List<NodeModel> nodeModels = new ArrayList<>();
        NodeModel parentNodeModel = nodeModel.getParentNode();
        if (null != parentNodeModel) {
            nodeModels.add(parentNodeModel);
            if (TaskType.major.eq(parentNodeModel.getType())) {
                return nodeModels;
            }
            // 继续往上递归
            List<NodeModel> pnmList = getAllParentNodeModels(parentNodeModel);
            if (!pnmList.isEmpty()) {
                nodeModels.addAll(pnmList);
            }
        }
        return nodeModels;
    }

    /**
     * 获取所有下一个节点key，递归所有子节点
     *
     * @param nodeModel 当前节点模型
     * @return 所有节点key
     */
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
     * 获取根节点下的所有节点模型【 注意，只对根节点查找有效！】
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
     * 检查节点模型，检测节点模型需要构建父节点
     *
     * @param rootNodeModel 根节点模型
     * @return 0，正常 1，存在重复节点KEY 2，自动通过节点配置错误 3，自动拒绝节点配置错误
     * 4，路由节点必须配置错误（未配置路由分支） 5，子流程节点配置错误（未选择子流程）
     */
    public static int checkNodeModel(NodeModel rootNodeModel) {
        List<NodeModel> allNextNodes = getRootNodeAllChildNodes(rootNodeModel);
        Set<String> set = new HashSet<>();
        for (NodeModel nextNode : allNextNodes) {
            if (!set.add(nextNode.getNodeKey())) {
                // 节点KEY重复
                return 1;
            }
            if (TaskType.autoPass.eq(nextNode.getType())) {
                if (!inConditionNode(nextNode) || null != nextNode.getChildNode()) {
                    // 自动通过节点配置错误
                    return 2;
                }
            } else if (TaskType.autoReject.eq(nextNode.getType())) {
                if (!inConditionNode(nextNode) || null != nextNode.getChildNode()) {
                    // 自动拒绝节点配置错误
                    return 3;
                }
            } else if (nextNode.routeNode() && ObjectUtils.isEmpty(nextNode.getRouteNodes())) {
                // 路由节点必须配置错误（未配置路由分支）
                return 4;
            } else if (nextNode.callProcessNode() && ObjectUtils.isEmpty(nextNode.getCallProcess())) {
                // 子流程节点配置错误（未选择子流程）
                return 5;
            }
        }
        // 正确模型
        return 0;
    }

    /**
     * 判断节点是否在条件节点中
     *
     * @param nodeModel {@link NodeModel}
     * @return true 是 false 否
     */
    public static boolean inConditionNode(NodeModel nodeModel) {
        if (null != nodeModel) {
            NodeModel parentNode = nodeModel.getParentNode();
            if (null != parentNode) {
                if (parentNode.conditionNode()) {
                    return true;
                }
                return inConditionNode(parentNode);
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
     * @param flowLongContext 流程上下文
     * @param flwInstanceId   流程实例ID
     * @param processModel    流程模型
     */
    public static void reloadProcessModel(FlowLongContext flowLongContext, Long flwInstanceId, ProcessModel processModel) {
        // 重新加载流程模型内容
        reloadProcessModel(processModel, t -> {

            // 更新流程模型
            boolean ok = flowLongContext.getRuntimeService().updateInstanceModelById(flwInstanceId, t);
            Assert.isFalse(ok, "Failed to update process model content");

            // 重新构建父节点
            t.buildParentNode(processModel.getNodeConfig());
        });
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

            // 更新模型
            consumer.accept(processModel);

            // 删除动态分配处理人员参数
            FlowDataTransfer.removeByKey(FlowConstants.processDynamicAssignee);
        }
    }

    /**
     * 获取当前已使用的节点key列表
     *
     * @param flowLongContext 流程上下文 {@link FlowLongContext}
     * @param execution       流程执行对象 {@link Execution}
     * @param rootNodeModel   模型根节点 {@link NodeModel}
     * @param currentNodeKey  当前所在节点
     * @return 当前已使用的节点key列表
     */
    public static List<String> getAllUsedNodeKeys(FlowLongContext flowLongContext, Execution execution, NodeModel rootNodeModel, String currentNodeKey) {
        List<String> currentUsedNodeKeys = new ArrayList<>();
        if (null != rootNodeModel) {
            String nodeKey = rootNodeModel.getNodeKey();
            if (Objects.equals(currentNodeKey, nodeKey)) {
                // 找到执行最后一个节点直接结束
                currentUsedNodeKeys.add(nodeKey);
            } else {
                // 处理完成节点
                if (rootNodeModel.conditionNode()) {
                    // 条件节点
                    List<ConditionNode> conditionNodes = rootNodeModel.getConditionNodes();
                    if (ObjectUtils.isNotEmpty(conditionNodes)) {
                        // 找到对应节点
                        flowLongContext.getFlowConditionHandler().getConditionNode(flowLongContext, execution, rootNodeModel).ifPresent(t -> {
                            // 添加执行条件节点
                            currentUsedNodeKeys.add(t.getNodeKey());

                            // 条件节点分支子节点
                            getChildAllUsedNodeKeys(currentUsedNodeKeys, flowLongContext, execution, t.getChildNode(), currentNodeKey);
                        });
                    }

                    // 条件节点子节点
                    getChildAllUsedNodeKeys(currentUsedNodeKeys, flowLongContext, execution, rootNodeModel.getChildNode(), currentNodeKey);
                } else if (rootNodeModel.parallelNode()) {
                    // 并行节点
                    int flag = 0;
                    List<String> pnAllKeys = new ArrayList<>();
                    for (NodeModel nodeModel : rootNodeModel.getParallelNodes()) {
                        List<String> pnKeys = new ArrayList<>();
                        // 添加执行条件节点
                        pnKeys.add(nodeModel.getNodeKey());

                        // 条件节点分支子节点
                        pnKeys.addAll(getAllUsedNodeKeys(flowLongContext, execution, nodeModel, currentNodeKey));

                        // 判断如果包含当前节点则添加到已使用的节点中
                        if (pnKeys.contains(currentNodeKey)) {
                            flag = 1;
                            currentUsedNodeKeys.addAll(pnKeys);
                            break;
                        } else {
                            pnAllKeys.addAll(pnKeys);
                        }
                    }

                    // 如果不包含当前节点则添加到已使用的节点中
                    if (Objects.equals(0, flag)) {
                        currentUsedNodeKeys.addAll(pnAllKeys);
                    }

                    // 条件节点子节点
                    getChildAllUsedNodeKeys(currentUsedNodeKeys, flowLongContext, execution, rootNodeModel.getChildNode(), currentNodeKey);
                } else if (rootNodeModel.inclusiveNode()) {
                    // 包容节点
                    flowLongContext.getFlowConditionHandler().getInclusiveNodes(flowLongContext, execution, rootNodeModel).ifPresent(conditionNodes -> {
                        for (ConditionNode conditionNode : conditionNodes) {
                            // 添加执行条件节点
                            currentUsedNodeKeys.add(conditionNode.getNodeKey());

                            // 条件节点分支子节点
                            currentUsedNodeKeys.addAll(getAllUsedNodeKeys(flowLongContext, execution, conditionNode.getChildNode(), currentNodeKey));

                            // 已经找到当前节点，忽略其它分支
                            if (currentUsedNodeKeys.contains(currentNodeKey)) {
                                break;
                            }
                        }
                    });

                    // 条件节点子节点
                    getChildAllUsedNodeKeys(currentUsedNodeKeys, flowLongContext, execution, rootNodeModel.getChildNode(), currentNodeKey);
                } else if (rootNodeModel.routeNode()) {
                    // 路由节点
                    currentUsedNodeKeys.add(rootNodeModel.getNodeKey());
                    Optional<ConditionNode> opt = flowLongContext.getFlowConditionHandler().getRouteNode(flowLongContext, execution, rootNodeModel);
                    if (opt.isPresent()) {
                        // 添加执行条件节点
                        currentUsedNodeKeys.add(opt.get().getNodeKey());
                    } else if (null != rootNodeModel.getChildNode()) {
                        // 获取路由分支子节点
                        currentUsedNodeKeys.addAll(getAllUsedNodeKeys(flowLongContext, execution, rootNodeModel.getChildNode(), currentNodeKey));
                    }
                } else {

                    // 普通节点
                    currentUsedNodeKeys.add(nodeKey);

                    // 找子节点
                    NodeModel childNodeModel = rootNodeModel.getChildNode();
                    if (null != childNodeModel) {
                        getChildAllUsedNodeKeys(currentUsedNodeKeys, flowLongContext, execution, childNodeModel, currentNodeKey);
                    }
                }
            }
        }
        return currentUsedNodeKeys;
    }

    /**
     * 获取已使用所有的子节点key列表
     */
    public static void getChildAllUsedNodeKeys(List<String> currentUsedNodeKeys, FlowLongContext flowLongContext,
                                               Execution execution, NodeModel rootNodeModel, String currentNodeKey) {
        if (!currentUsedNodeKeys.contains(currentNodeKey)) {
            currentUsedNodeKeys.addAll(getAllUsedNodeKeys(flowLongContext, execution, rootNodeModel, currentNodeKey));
        }
    }
}
