/*
 * 爱组搭 http://aizuda.com 低代码组件化开发平台
 * ------------------------------------------
 * 受知识产权保护，请勿删除版权申明
 */
package com.flowlong.bpm.engine.model;

import com.flowlong.bpm.engine.Expression;
import com.flowlong.bpm.engine.ModelInstance;
import com.flowlong.bpm.engine.assist.Assert;
import com.flowlong.bpm.engine.assist.ObjectUtils;
import com.flowlong.bpm.engine.core.Execution;
import com.flowlong.bpm.engine.core.FlowLongContext;
import com.flowlong.bpm.engine.handler.impl.CreateTaskHandler;
import lombok.Getter;
import lombok.Setter;

import java.util.*;

/**
 * 爱组搭 http://aizuda.com
 * ----------------------------------------
 * JSON BPM 节点
 *
 * @author 青苗
 * @since 2023-03-17
 */
@Getter
@Setter
public class NodeModel implements ModelInstance {
    /**
     * 节点名称
     */
    private String nodeName;
    /**
     * 节点类型
     * <p>
     * 0，发起人
     * 1，审批人
     * 2，抄送人
     * 3，条件审批
     * 4，条件分支
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
     * 审批期限超时自动审批
     */
    private Boolean termAuto;
    /**
     * 审批期限
     */
    private Integer term;
    /**
     * 审批期限超时后执行类型
     */
    private Integer termMode;
    /**
     * 多人审批时审批方式 {@link com.flowlong.bpm.engine.core.enums.PerformType}
     * <p>
     * 1，按顺序依次审批
     * 2，会签 (可同时审批，每个人必须审批通过)
     * 3，或签 (有一人审批通过即可)
     * </p>
     */
    private Integer examineMode;
    /**
     * 连续主管审批方式
     * <p>
     * 1，直到最上级主管
     * 2，自定义审批终点
     * </p>
     */
    private Integer directorMode;
    /**
     * 条件节点列表
     */
    private List<ConditionNode> conditionNodes;
    /**
     * 允许发起人自选抄送人
     */
    private Boolean userSelectFlag;
    /**
     * 子节点
     */
    private NodeModel childNode;
    /**
     * 父节点，模型 json 不存在该属性、属于逻辑节点
     */
    private NodeModel parentNode;

    @Override
    public void execute(FlowLongContext flowLongContext, Execution execution) {
        if (ObjectUtils.isNotEmpty(this.conditionNodes)) {
            /**
             * 执行条件分支
             */
            Map<String, Object> args = execution.getArgs();
            Assert.illegalArgument(ObjectUtils.isEmpty(args), "Execution parameter cannot be empty");
            Expression expression = flowLongContext.getExpression();
            Assert.isNull(expression, "Interface Expression not implemented");
            Optional<ConditionNode> conditionNodeOptional = conditionNodes.stream().sorted(Comparator.comparing(ConditionNode::getPriorityLevel))
                    .filter(t -> {
                        // 执行条件分支
                        final String expr = t.getExpr();
                        boolean result = true;
                        if (null != expr) {
                            try {
                                result = expression.eval(Boolean.class, expr, args);
                            } catch (Throwable e) {
                                result = false;
                                e.printStackTrace();
                            }
                        }
                        return result;
                    }).findFirst();
            Assert.isFalse(conditionNodeOptional.isPresent(), "Not found executable ConditionNode");
            /**
             * 执行创建条件任务
             */
            this.createTask(conditionNodeOptional.get().getChildNode(), flowLongContext, execution);
        }

        /**
         * 执行创建抄送、审批任务
         */
        if (Objects.equals(2, this.type) || Objects.equals(1, this.type)) {
            this.createTask(flowLongContext, execution);
        }
    }

    public void createTask(FlowLongContext flowLongContext, Execution execution) {
        this.createTask(this, flowLongContext, execution);
    }

    protected void createTask(NodeModel nodeModel, FlowLongContext flowLongContext, Execution execution) {
        new CreateTaskHandler(nodeModel).handle(flowLongContext, execution);
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
     * 判断是否为条件节点
     */
    public boolean isConditionNode() {
        return 3 == type || 4 == type;
    }
}
