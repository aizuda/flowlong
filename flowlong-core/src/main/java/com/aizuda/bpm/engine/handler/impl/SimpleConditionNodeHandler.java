/*
 * Copyright 2023-2025 Licensed under the Dual Licensing
 * website: https://aizuda.com
 */
package com.aizuda.bpm.engine.handler.impl;

import com.aizuda.bpm.engine.FlowConstants;
import com.aizuda.bpm.engine.FlowDataTransfer;
import com.aizuda.bpm.engine.FlowLongExpression;
import com.aizuda.bpm.engine.assist.Assert;
import com.aizuda.bpm.engine.assist.ObjectUtils;
import com.aizuda.bpm.engine.core.Execution;
import com.aizuda.bpm.engine.core.FlowLongContext;
import com.aizuda.bpm.engine.handler.ConditionNodeHandler;
import com.aizuda.bpm.engine.handler.FlowAiHandler;
import com.aizuda.bpm.engine.model.ConditionNode;
import com.aizuda.bpm.engine.model.NodeModel;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 默认流程执行条件处理器
 *
 * <p>
 * <a href="https://aizuda.com">官网</a>尊重知识产权，不允许非法使用，后果自负
 * </p>
 *
 * @author hubin
 * @since 1.0
 */
public class SimpleConditionNodeHandler implements ConditionNodeHandler {
    private static SimpleConditionNodeHandler conditionNodeHandler;

    public static SimpleConditionNodeHandler getInstance() {
        if (null == conditionNodeHandler) {
            synchronized (SimpleConditionNodeHandler.class) {
                conditionNodeHandler = new SimpleConditionNodeHandler();
            }
        }
        return conditionNodeHandler;
    }

    @Override
    public Optional<ConditionNode> getConditionNode(FlowLongContext flowLongContext, Execution execution, NodeModel nodeModel) {
        // 判断条件节点
        Optional<ConditionNode> cnOpt = this.getConditionNode(flowLongContext, execution, nodeModel, nodeModel.getConditionNodes());
        assertIllegal(!cnOpt.isPresent());
        return cnOpt;
    }

    public Optional<ConditionNode> getConditionNode(FlowLongContext flowLongContext, Execution execution,
                                                    NodeModel nodeModel, List<ConditionNode> conditionNodes) {
        this.assertConditionNodes(conditionNodes);

        // 查找匹配条件节点
        Optional<ConditionNode> conditionNodeOptional = this.matchConditionNode(flowLongContext, execution, nodeModel, conditionNodes);
        if (conditionNodeOptional.isPresent()) {
            return conditionNodeOptional;
        }

        // 未发现满足条件分支，使用无条件分支
        return defaultConditionNode(conditionNodes);
    }

    public Optional<ConditionNode> matchConditionNode(FlowLongContext flowLongContext, Execution execution,
                                                      NodeModel nodeModel, List<ConditionNode> conditionNodes) {

        // 1. 根据指定条件节点选择
        String conditionNodeKey = FlowDataTransfer.get(FlowConstants.processSpecifyConditionNodeKey);
        if (null != conditionNodeKey) {
            // 清理指定节点参数
            FlowDataTransfer.removeByKey(FlowConstants.processSpecifyConditionNodeKey);

            // 找到指定条件节点
            Optional<ConditionNode> conditionNodeKeyOptional = conditionNodes.stream().filter(t -> Objects.equals(t.getNodeKey(), conditionNodeKey)).findFirst();
            if (conditionNodeKeyOptional.isPresent()) {
                return conditionNodeKeyOptional;
            }
        }

        // 获取节点执行参数
        Map<String, Object> args = this.getArgs(flowLongContext, execution, nodeModel);

        // 2. AI 智能路由决策（优先于规则判断）
        if (null != nodeModel.getCallAi()) {
            FlowAiHandler flowAiHandler = flowLongContext.getFlowAiHandler();
            if (null != flowAiHandler) {
                String aiDecidedNodeKey = flowAiHandler.decideRoute(flowLongContext, execution, nodeModel, args);
                if (null != aiDecidedNodeKey) {
                    Optional<ConditionNode> aiNodeOptional = conditionNodes.stream()
                            .filter(t -> Objects.equals(t.getNodeKey(), aiDecidedNodeKey))
                            .findFirst();
                    if (aiNodeOptional.isPresent()) {
                        return aiNodeOptional;
                    }
                }
            }
        }

        // 3. 根据正则条件节点选择
        FlowLongExpression flowLongExpression = flowLongContext.checkFlowLongExpression();
        return conditionNodes.stream().sorted(Comparator.comparing(ConditionNode::getPriorityLevel))
                // 执行表单式判断，匹配执行节点
                .filter(t -> flowLongExpression.eval(t.getConditionList(), args))
                .findFirst();
    }

    @Override
    public Optional<ConditionNode> getRouteNode(FlowLongContext flowLongContext, Execution execution, NodeModel nodeModel) {
        // 判断路由节点
        return this.getConditionNode(flowLongContext, execution, nodeModel, nodeModel.getRouteNodes());
    }

    public Map<String, Object> getArgs(FlowLongContext flowLongContext, Execution execution, NodeModel nodeModel) {
        Map<String, Object> args = execution.getArgs();
        return ObjectUtils.isEmpty(args) ? Collections.emptyMap() : args;
    }

    public Optional<ConditionNode> defaultConditionNode(List<ConditionNode> conditionNodes) {
        return conditionNodes.stream().filter(t -> ObjectUtils.isEmpty(t.getConditionList())).findFirst();
    }

    public void assertConditionNodes(List<ConditionNode> conditionNodes) {
        assertIllegal(null == conditionNodes || conditionNodes.isEmpty());
    }

    public void assertIllegal(boolean illegal) {
        Assert.illegal(illegal, "Not found executable ConditionNode");
    }

    @Override
    public Optional<List<ConditionNode>> getInclusiveNodes(FlowLongContext flowLongContext, Execution execution, NodeModel nodeModel) {
        final List<ConditionNode> inclusiveNodes = nodeModel.getInclusiveNodes();
        this.assertConditionNodes(inclusiveNodes);

        // 获取节点执行参数
        Map<String, Object> args = this.getArgs(flowLongContext, execution, nodeModel);

        // 1. AI 智能包容分支决策（优先于规则判断）
        if (null != nodeModel.getCallAi()) {
            FlowAiHandler flowAiHandler = flowLongContext.getFlowAiHandler();
            if (null != flowAiHandler) {
                List<String> aiDecidedNodeKeys = flowAiHandler.decideInclusiveRoutes(flowLongContext, execution, nodeModel, args);
                if (ObjectUtils.isNotEmpty(aiDecidedNodeKeys)) {
                    // 根据 AI 决策的 NodeKey 列表筛选包容节点
                    List<ConditionNode> aiNodes = inclusiveNodes.stream()
                            .filter(t -> aiDecidedNodeKeys.contains(t.getNodeKey()))
                            .collect(Collectors.toList());
                    if (!aiNodes.isEmpty()) {
                        return Optional.of(aiNodes);
                    }
                }
            }
        }

        // 2. 根据 SpEL 条件表达式选择
        FlowLongExpression flowLongExpression = flowLongContext.checkFlowLongExpression();
        List<ConditionNode> cnsOpt = inclusiveNodes.stream()
                .filter(t -> flowLongExpression.eval(t.getConditionList(), args))
                .collect(Collectors.toList());

        // 3. 未发现满足条件分支，使用默认分支
        if (ObjectUtils.isEmpty(cnsOpt)) {
            cnsOpt = Collections.singletonList(defaultConditionNode(inclusiveNodes).get());
        }
        return Optional.of(cnsOpt);
    }
}
