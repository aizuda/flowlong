/*
 * Copyright 2023-2025 Licensed under the Dual Licensing
 * website: https://aizuda.com
 */
package com.aizuda.bpm.engine.handler.impl;

import com.aizuda.bpm.engine.FlowLongExpression;
import com.aizuda.bpm.engine.FlowConstants;
import com.aizuda.bpm.engine.FlowDataTransfer;
import com.aizuda.bpm.engine.assist.Assert;
import com.aizuda.bpm.engine.assist.ObjectUtils;
import com.aizuda.bpm.engine.core.Execution;
import com.aizuda.bpm.engine.core.FlowLongContext;
import com.aizuda.bpm.engine.handler.ConditionNodeHandler;
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
        Optional<ConditionNode> conditionNodeOptional = this.matchConditionNode(flowLongContext, execution, nodeModel.getConditionNodes());
        if (conditionNodeOptional.isPresent()) {
            return conditionNodeOptional;
        }

        // 未发现满足条件分支，使用无条件分支
        return defaultConditionNode(nodeModel.getConditionNodes());
    }

    public Optional<ConditionNode> matchConditionNode(FlowLongContext flowLongContext, Execution execution, List<ConditionNode> conditionNodes) {

        // 根据指定条件节点选择
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

        // 根据正则条件节点选择
        Map<String, Object> args = this.getArgs(flowLongContext, execution);
        FlowLongExpression flowLongExpression = flowLongContext.checkFlowLongExpression();
        return conditionNodes.stream().sorted(Comparator.comparing(ConditionNode::getPriorityLevel))
                .filter(t -> flowLongExpression.eval(t.getConditionList(), args)).findFirst();
    }

    @Override
    public Optional<ConditionNode> getRouteNode(FlowLongContext flowLongContext, Execution execution, NodeModel nodeModel) {
        return this.matchConditionNode(flowLongContext, execution, nodeModel.getRouteNodes());
    }

    public Map<String, Object> getArgs(FlowLongContext flowLongContext, Execution execution) {
        Map<String, Object> args = execution.getArgs();
        return ObjectUtils.isEmpty(args) ? Collections.emptyMap() : args;
    }

    public Optional<ConditionNode> defaultConditionNode(List<ConditionNode> conditionNodes) {
        Optional<ConditionNode> cnOpt = conditionNodes.stream().filter(t -> ObjectUtils.isEmpty(t.getConditionList())).findFirst();
        Assert.isFalse(cnOpt.isPresent(), "Not found executable ConditionNode");
        return cnOpt;
    }

    @Override
    public Optional<List<ConditionNode>> getInclusiveNodes(FlowLongContext flowLongContext, Execution execution, NodeModel nodeModel) {
        List<ConditionNode> inclusiveNodes = nodeModel.getInclusiveNodes();

        // 根据正则条件节点选择
        FlowLongExpression flowLongExpression = flowLongContext.checkFlowLongExpression();
        Map<String, Object> args = this.getArgs(flowLongContext, execution);
        List<ConditionNode> cnsOpt = inclusiveNodes.stream().filter(t -> flowLongExpression.eval(t.getConditionList(), args)).collect(Collectors.toList());
        if (ObjectUtils.isEmpty(cnsOpt)) {
            cnsOpt = Collections.singletonList(defaultConditionNode(inclusiveNodes).get());
        }
        return Optional.of(cnsOpt);
    }
}
