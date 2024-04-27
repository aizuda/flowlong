/*
 * Copyright 2023-2025 Licensed under the AGPL License
 */
package com.aizuda.bpm.engine.model;

import com.aizuda.bpm.engine.Expression;
import com.aizuda.bpm.engine.ModelInstance;
import com.aizuda.bpm.engine.assist.Assert;
import com.aizuda.bpm.engine.assist.ObjectUtils;
import com.aizuda.bpm.engine.core.Execution;
import com.aizuda.bpm.engine.core.FlowLongContext;
import com.aizuda.bpm.engine.core.enums.PerformType;
import com.aizuda.bpm.engine.core.enums.TaskType;
import com.aizuda.bpm.engine.entity.FlwProcess;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.*;

/**
 * JSON BPM 节点
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
public class NodeModel implements ModelInstance, Serializable {
    /**
     * 节点名称
     */
    private String nodeName;
    /**
     * 调用外部流程定义 key 唯一标识 {@link FlwProcess}
     */
    private String callProcessKey;
    /**
     * 节点类型
     * <p>
     * -1，结束节点
     * 0，发起人
     * 1，审批人
     * 2，抄送人
     * 3，条件审批
     * 4，条件分支
     * 5，办理子流程
     * 6，定时器任务
     * 7，触发器任务
     * </p>
     */
    private Integer type;
    /**
     * 审核人类型
     * <p>
     * 1，指定成员
     * 2，主管
     * 3，角色
     * 4，发起人自选
     * 5，发起人自己
     * 7，连续多级主管
     * </p>
     */
    private Integer setType;
    /**
     * 审核人成员
     */
    private List<NodeAssignee> nodeUserList;
    /**
     * 审核角色
     */
    private List<NodeAssignee> nodeRoleList;
    /**
     * 审核部门
     */
    private List<NodeAssignee> nodeDeptList;
    /**
     * 指定主管层级
     */
    private Integer examineLevel;
    /**
     * 自定义连续主管审批层级
     */
    private Integer directorLevel;
    /**
     * 发起人自选类型
     * <p>
     * 1，自选一个人
     * 2，自选多个人
     * </p>
     */
    private Integer selectMode;
    /**
     * 审批期限（小时）
     */
    private Integer term;
    /**
     * 审批期限超时后执行类型
     * <p>
     * 0，自动通过 1，自动拒绝
     * </p>
     */
    private Integer termMode;
    /**
     * 多人审批时审批方式 {@link PerformType}
     * <p>
     * 1，按顺序依次审批
     * 2，会签 (可同时审批，每个人必须审批通过)
     * 3，或签 (有一人审批通过即可)
     * 4，票签 (总权重大于 50% 表示通过)
     * </p>
     */
    private Integer examineMode;
    /**
     * 连续主管审批方式
     * <p>
     * 0，直到最上级主管
     * 1，自定义审批终点
     * </p>
     */
    private Integer directorMode;
    /**
     * 通过权重（ 所有分配任务权重之和大于该值即通过，默认 50 ）
     */
    private Integer passWeight;
    /**
     * 条件节点列表
     */
    private List<ConditionNode> conditionNodes;
    /**
     * 允许发起人自选抄送人
     */
    private Boolean allowSelection;
    /**
     * 允许转交
     */
    private Boolean allowTransfer;
    /**
     * 允许加签/减签
     */
    private Boolean allowAppendNode;
    /**
     * 允许回退
     */
    private Boolean allowRollback;
    /**
     * 审批人与提交人为同一人时
     * <p>
     * 0，由发起人对自己审批
     * 1，自动跳过
     * 2，转交给直接上级审批
     * 3，转交给部门负责人审批
     * </p>
     */
    private Integer approveSelf;
    /**
     * 扩展配置，用于存储表单权限、操作权限 等控制参数配置
     * <p>
     * 定时器任务：自定义参数 time 触发时间<br/>
     * 例如：一小时后触发 {"time": "1:h"} 单位【 d 天 h 时 m 分 】<br/>
     *      发起后一小时三十分后触发 {"time": "01:30:00"}
     * </p>
     */
    private Map<String, Object> extendConfig;
    /**
     * 子节点
     */
    private NodeModel childNode;
    /**
     * 父节点，模型 json 不存在该属性、属于逻辑节点
     */
    private NodeModel parentNode;

    @Override
    public boolean execute(FlowLongContext flowLongContext, Execution execution) {
        if (ObjectUtils.isNotEmpty(conditionNodes)) {
            /*
             * 执行条件分支
             */
            Map<String, Object> args = flowLongContext.getConditionArgsHandler().handle(flowLongContext, execution, this);
            Assert.illegal(ObjectUtils.isEmpty(args), "Execution parameter cannot be empty");
            Expression expression = flowLongContext.getExpression();
            Assert.isNull(expression, "Interface Expression not implemented");
            Optional<ConditionNode> conditionNodeOptional = conditionNodes.stream().sorted(Comparator.comparing(ConditionNode::getPriorityLevel))
                    .filter(t -> expression.eval(t.getConditionList(), args)).findFirst();
            if (!conditionNodeOptional.isPresent()) {
                // 未发现满足条件分支，使用无条件分支
                conditionNodeOptional = conditionNodes.stream().filter(t -> ObjectUtils.isEmpty(t.getConditionList())).findFirst();
                Assert.isFalse(conditionNodeOptional.isPresent(), "Not found executable ConditionNode");
            }
            /*
             * 执行创建条件任务
             */
            if (conditionNodeOptional.isPresent()) {
                NodeModel childNode = conditionNodeOptional.get().getChildNode();
                if (null == childNode) {
                    // 当前条件节点无执行节点，进入当前执行条件节点的下一个节点
                    childNode = this.getChildNode();
                }
                if (null != childNode) {
                    childNode.execute(flowLongContext, execution);
                } else {
                    // 不存在任何子节点结束流程
                    execution.endInstance(this);
                    return true;
                }
            }
        }

        /*
         * 执行 1、审批任务 2、创建抄送 5、办理子流程 6、定时器任务
         */
        if (TaskType.approval.eq(this.type) || TaskType.cc.eq(this.type)
                || TaskType.callProcess.eq(this.type)
                || TaskType.timer.eq(this.type)) {
            flowLongContext.createTask(execution, this);
        }

        /*
         * 执行结束流程
         */
        else if (TaskType.end.eq(this.type)) {
            return execution.endInstance(this);
        }

        /*
         * 不存在子节点，不存在其它分支节点，当前执行节点为最后节点 并且当前节点不是审批节点
         * 执行结束流程处理器
         */
        if (null == this.getChildNode() && null == this.getConditionNodes()) {
            if (!this.nextNode().isPresent() && !TaskType.approval.eq(this.type)) {
                execution.endInstance(this);
            }
        }
        return true;
    }

    /**
     * 获取process定义的指定节点名称的节点模型
     *
     * @param nodeName 节点名称
     * @return {@link NodeModel}
     */
    public NodeModel getNode(String nodeName) {
        if (Objects.equals(this.nodeName, nodeName)) {
            return this;
        }
        if (null != conditionNodes) {
            NodeModel fromConditionNode = getFromConditionNodes(nodeName);
            if (fromConditionNode != null) {
                return fromConditionNode;
            }
        }
        // 条件节点中没有找到 那么去它的同级子节点中继续查找
        if (null != childNode) {
            return childNode.getNode(nodeName);
        }
        return null;
    }

    /**
     * 从条件节点中获取节点
     *
     * @param nodeName 节点名称
     * @return {@link NodeModel}
     */
    private NodeModel getFromConditionNodes(String nodeName) {
        for (ConditionNode conditionNode : conditionNodes) {
            NodeModel conditionChildNode = conditionNode.getChildNode();
            if (null != conditionChildNode) {
                NodeModel nodeModel = conditionChildNode.getNode(nodeName);
                if (null != nodeModel) {
                    return nodeModel;
                }
            }
        }
        return null;
    }

    /**
     * 下一个执行节点
     */
    public Optional<NodeModel> nextNode() {
        NodeModel nextNode = this.getChildNode();
        if (null == nextNode) {
            // 如果当前节点完成，并且该节点为条件节点，找到主干执行节点继续执行
            nextNode = ModelHelper.findNextNode(this);
        }
        return Optional.ofNullable(nextNode);
    }

    /**
     * 判断是否为条件节点
     */
    public boolean conditionNode() {
        return TaskType.conditionNode.eq(type) || TaskType.conditionBranch.eq(type);
    }

    /**
     * 判断是否为抄送节点
     */
    public boolean ccNode() {
        return TaskType.cc.eq(type);
    }
}
