/*
 * 爱组搭 http://aizuda.com 低代码组件化开发平台
 * ------------------------------------------
 * 受知识产权保护，请勿删除版权申明
 */
package com.flowlong.bpm.engine.model;

import com.flowlong.bpm.engine.ModelInstance;
import com.flowlong.bpm.engine.core.Execution;
import com.flowlong.bpm.engine.core.FlowLongContext;
import com.flowlong.bpm.engine.handler.impl.CreateTaskHandler;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Objects;

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
     * </p>
     */
    private Integer type;
    /**
     * 审核人类型
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
     * 多人审批时审批方式
     */
    private Integer examineMode;
    /**
     * 连续主管审批方式
     */
    private Integer directorMode;
    /**
     * 条件节点列表
     */
    private List<ConditionNode> conditionNodes;
    /**
     * 子节点
     */
    private NodeModel childNode;

    @Override
    public void execute(FlowLongContext flowLongContext, Execution execution) {
        new CreateTaskHandler(this).handle(flowLongContext, execution);
    }

    /**
     * 获取process定义的指定节点名称的节点模型
     *
     * @param nodeName 节点名称
     * @return {@link NodeModel}
     */
    public NodeModel getNode(String nodeName) {
        if (null != conditionNodes) {
            for (ConditionNode conditionNode : conditionNodes) {
                if (Objects.equals(nodeName, conditionNode.getNodeName())) {
                    return conditionNode.getChildNode();
                }
                NodeModel conditionChildNode = conditionNode.getChildNode();
                if (null != conditionChildNode) {
                    return conditionChildNode.getNode(nodeName);
                }
            }
        }
        if (null != childNode) {
            return childNode.getNode(nodeName);
        }
        return null;
    }


}
