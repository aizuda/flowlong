/*
 * Copyright 2023-2025 Licensed under the Dual Licensing
 * website: https://aizuda.com
 */
package com.aizuda.bpm.engine.model;

import com.aizuda.bpm.engine.FlowConstants;
import com.aizuda.bpm.engine.FlowDataTransfer;
import com.aizuda.bpm.engine.ModelInstance;
import com.aizuda.bpm.engine.TaskTrigger;
import com.aizuda.bpm.engine.assist.Assert;
import com.aizuda.bpm.engine.assist.ObjectUtils;
import com.aizuda.bpm.engine.core.Execution;
import com.aizuda.bpm.engine.core.FlowLongContext;
import com.aizuda.bpm.engine.core.enums.*;
import com.aizuda.bpm.engine.entity.FlwProcess;
import com.aizuda.bpm.engine.entity.FlwTaskActor;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * JSON BPM 节点
 *
 * <p>
 * <a href="https://aizuda.com">官网</a>尊重知识产权，不允许非法使用，后果自负
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
     * 节点 key
     */
    private String nodeKey;
    /**
     * 调用外部流程 {@link FlwProcess}
     * <p>
     * 实际业务存储格式 processId:processName 流程ID名称冒号拼接内容
     * </p>
     * <p>
     * 不存在冒号为 processKey 内容用于测试等场景
     * </p>
     */
    private String callProcess;
    /**
     * 是否异步调用【例如：子流程该参数为 true 则为异步子流程】
     */
    private Boolean callAsync;
    /**
     * 任务关联的表单url
     */
    private String actionUrl;
    /**
     * 节点类型 {@link TaskType}
     * <p>
     * -1，结束节点 0，发起人 1，审批人 2，抄送人 3，条件审批 4，条件分支 5，办理子流程 6，定时器任务 7，触发器任务 8，并发分支 9，包容分支
     * </p>
     * <p>
     * 23，路由分支 30，自动通过 31，自动拒绝
     * </p>
     */
    private Integer type;
    /**
     * 审核人类型 {@link NodeSetType}
     * <p>
     * 1，指定成员 2，主管 3，角色 4，发起人自选 5，发起人自己 6，连续多级主管 7，部门 8，指定候选人
     * </p>
     */
    private Integer setType;
    /**
     * 审核分配到任务的处理者，过 setType 区分个人角色或部门
     */
    private List<NodeAssignee> nodeAssigneeList;
    /**
     * 节点候选人或角色等
     */
    private NodeCandidate nodeCandidate;
    /**
     * 指定主管层级
     */
    private Integer examineLevel;

    /**
     * 多人审批时审批方式 {@link PerformType}
     * <p>
     * 0，发起 1，按顺序依次审批 2，会签 (可同时审批，每个人必须审批通过) 3，或签 (有一人审批通过即可) 4，票签 (总权重大于 50% 表示通过) <br/>
     * 5， 6，定时器 7，触发器 8， 9，抄送
     * </p>
     */
    private Integer examineMode;
    /**
     * 分组（角色、部门）审批策略，默认 0，认领审批 1，全部人员参与审批
     */
    private Integer groupStrategy;
    /**
     * 自定义连续主管审批层级
     */
    private Integer directorLevel;
    /**
     * 连续主管审批方式
     * <p>
     * 0，直到最上级主管 1，自定义审批终点
     * </p>
     */
    private Integer directorMode;
    /**
     * 发起人自选类型
     * <p>
     * 1，自选一个人 2，自选多个人 3，自选角色
     * </p>
     */
    private Integer selectMode;
    /**
     * 超时自动审批
     */
    private Boolean termAuto;
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
     * 审批类型 1，人工审批 2，自动通过 3，自动拒绝
     */
    private Integer typeOfApprove;
    /**
     * 通过权重（ 所有分配任务权重之和大于该值即通过，默认 50 ）
     */
    private Integer passWeight;
    /**
     * 驳回策略 1，驳回到发起人，2，驳回到上一节点，3，驳回到指定节点 4，终止审批流程 5，驳回到模型父节点
     */
    private Integer rejectStrategy;
    /**
     * 驳回重新审批策略 1，继续往下执行 2，回到上一个节点
     */
    private Integer rejectStart;
    /**
     * 条件节点列表
     */
    private List<ConditionNode> conditionNodes;
    /**
     * 并行节点
     * <p>相当于并行网关</p>
     */
    private List<ConditionNode> parallelNodes;
    /**
     * 包容节点
     * <p>相当于包容网关</p>
     */
    private List<ConditionNode> inclusiveNodes;
    /**
     * 路由节点
     * <p>setType设置为 10 生效，根据对应条件自动跳转到指定路由节点位置</p>
     */
    private List<ConditionNode> routeNodes;
    /**
     * 审批提醒
     * <p>
     * 具体提醒时间加载 extendConfig 配置 remindTime 属性
     * </p>
     */
    private Boolean remind;
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
     * 允许审批节点手动创建抄送任务
     */
    private Boolean allowCc;
    /**
     * 审批人与提交人为同一人时 {@link NodeApproveSelf}
     * <p>
     * 0，由发起人对自己审批 1，自动跳过 2，转交给直接上级审批 3，转交给部门负责人审批
     * </p>
     */
    private Integer approveSelf;
    /**
     * 扩展配置，用于存储表单权限、操作权限 等控制参数配置
     * <p>
     * 定时器任务：自定义参数 time 触发时间
     * </p>
     * <p>
     * 例如：一小时后触发 {"time": "1:h"} 单位【 d 天 h 时 m 分 】
     * </p>
     * <p>
     * 发起后一小时三十分后触发 {"time": "01:30:00"}
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
    /**
     * 触发器类型 1，立即执行 2，延迟执行
     */
    private Integer triggerType;
    /**
     * 延时处理类型 1，固定时长 2，自动计算 具体时间 extendConfig 配置 time 属性
     * <p>
     * 固定时长 "time": "1:m"
     * </p>
     * <p>
     * 自动计算 "time": "17:02:53"
     * </p>
     */
    private Integer delayType;

    /**
     * 是否保存权重
     * <p>兼容模型设计错误导致权重误存</p>
     */
    public boolean saveWeight() {
        // 票签，保留权重
        return Objects.equals(4, this.examineMode);
    }

    /**
     * 执行节点
     *
     * @param flowLongContext 流程引擎上下文
     * @param execution       执行对象
     * @return 执行结果 true 成功 false 失败
     */
    @Override
    public boolean execute(FlowLongContext flowLongContext, Execution execution) {

        if (TaskType.conditionBranch.eq(this.type)) {
            /*
             * 执行条件分支
             */
            flowLongContext.getFlowConditionHandler()
                    .getConditionNode(flowLongContext, execution, this)
                    .ifPresent(t -> this.executeConditionNode(flowLongContext, execution, t));
            return true;
        }

        if (TaskType.parallelBranch.eq(this.type)) {
            /*
             * 执行并行分支
             */
            for (ConditionNode parallelNode : parallelNodes) {
                parallelNode.getChildNode().execute(flowLongContext, execution);
            }
            return true;
        }

        if (TaskType.inclusiveBranch.eq(this.type)) {
            /*
             * 执行包容分支
             */
            Optional<List<ConditionNode>> cnOpt = flowLongContext.getFlowConditionHandler()
                    .getInclusiveNodes(flowLongContext, execution, this);
            if (cnOpt.isPresent()) {
                List<ConditionNode> cnList = cnOpt.get();
                int j = cnList.size();
                for (int i = 0; i < j; i++) {
                    if (i + 1 == j) {
                        // 标记最后一个满足条件节点
                        FlowDataTransfer.put(FlowConstants.processLastConditionNode, 1);
                    }
                    // 执行满足条件分支
                    this.executeConditionNode(flowLongContext, execution, cnList.get(i));
                }
            }
            return true;
        }

        if (TaskType.routeBranch.eq(this.type)) {
            /*
             * 执行路由分支
             */
            Optional<ConditionNode> routeNodeOptional = flowLongContext.getFlowConditionHandler()
                    .getRouteNode(flowLongContext, execution, this);
            if (routeNodeOptional.isPresent()) {
                // 自动跳转到指定节点
                execution.getEngine().executeJumpTask(execution.getFlwTask().getId(), routeNodeOptional.get().getNodeKey(),
                        execution.getFlowCreator(), execution.getArgs(), TaskType.routeJump);
            } else {
                // 执行子节点逻辑
                NodeModel _childNode = this.getChildNode();
                if (null != _childNode) {
                    _childNode.execute(flowLongContext, execution);
                }
            }
            return true;
        }

        /*
         * 执行 1、审批任务 2、创建抄送 5、办理子流程 6、定时器任务 7、触发器任务
         */
        if (TaskType.approval.eq(this.type) || TaskType.cc.eq(this.type)
                || TaskType.callProcess.eq(this.type) || TaskType.timer.eq(this.type)
                || TaskType.trigger.eq(this.type)) {

            // 创建任务
            flowLongContext.createTask(execution, this);
        }

        /*
         * 执行【自动通过】结束流程
         */
        else if (TaskType.autoPass.eq(this.type)) {
            return execution.endInstance(this, InstanceState.autoPass);
        }

        /*
         * 执行【自动拒绝】结束流程
         */
        else if (TaskType.autoReject.eq(this.type)) {
            return execution.endInstance(this, InstanceState.autoReject);
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
     * 执行条件节点分支
     *
     * @param flowLongContext {@link FlowLongContext}
     * @param execution       {@link Execution}
     * @param conditionNode   {@link ConditionNode}
     */
    public void executeConditionNode(FlowLongContext flowLongContext, Execution execution, ConditionNode conditionNode) {
        NodeModel childNode = conditionNode.getChildNode();
        if (null == childNode) {
            // 当前条件节点无执行节点，进入当前执行条件节点的下一个节点
            childNode = this.getChildNode();
        }
        if (null != childNode) {
            childNode.execute(flowLongContext, execution);
        } else {
            // 查看是否存在其他的节点 fix https://gitee.com/aizuda/flowlong/issues/I9O8GV
            this.nextNode().ifPresent(nodeModel -> nodeModel.execute(flowLongContext, execution));
        }
    }

    /**
     * 获取process定义的指定节点key的节点模型
     *
     * @param nodeKey 节点key
     * @return 模型节点
     */
    public NodeModel getNode(String nodeKey) {
        if (Objects.equals(this.nodeKey, nodeKey)) {
            return this;
        }

        // 条件分支
        NodeModel fromConditionNode = this.getFromConditionNodes(nodeKey, conditionNodes);
        if (fromConditionNode != null) {
            return fromConditionNode;
        }

        // 并行分支
        NodeModel fromParallelNode = this.getFromConditionNodes(nodeKey, parallelNodes);
        if (fromParallelNode != null) {
            return fromParallelNode;
        }

        // 包容分支
        NodeModel fromInclusiveNode = this.getFromConditionNodes(nodeKey, inclusiveNodes);
        if (fromInclusiveNode != null) {
            return fromInclusiveNode;
        }

        // 条件节点中没有找到 那么去它的同级子节点中继续查找
        if (null != childNode) {
            return childNode.getNode(nodeKey);
        }
        return null;
    }

    /**
     * 从条件节点中获取节点
     *
     * @param nodeKey        节点 key
     * @param conditionNodes 条件节点模型列表
     * @return 模型节点
     */
    private NodeModel getFromConditionNodes(String nodeKey, List<ConditionNode> conditionNodes) {
        if (null != conditionNodes) {
            for (ConditionNode conditionNode : conditionNodes) {
                NodeModel conditionChildNode = conditionNode.getChildNode();
                if (null != conditionChildNode) {
                    NodeModel nodeModel = conditionChildNode.getNode(nodeKey);
                    if (null != nodeModel) {
                        return nodeModel;
                    }
                }
            }
        }
        return null;
    }

    /**
     * 下一个执行节点
     *
     * @return 模型节点
     */
    public Optional<NodeModel> nextNode() {
        return nextNode(null);
    }

    /**
     * 下一个执行节点
     *
     * @param currentTask 当前任务
     * @return 模型节点
     */
    public Optional<NodeModel> nextNode(List<String> currentTask) {
        NodeModel nextNode = this.getChildNode();
        if (null == nextNode) {
            // 如果当前节点完成，并且该节点为条件节点，找到主干执行节点继续执行
            nextNode = ModelHelper.findNextNode(this, currentTask);
        }
        return Optional.ofNullable(nextNode);
    }

    /**
     * 是否异步调用
     */
    public boolean callAsync() {
        return null != callAsync && callAsync;
    }

    /**
     * 获取父审批节点
     *
     * @return 模型节点
     */
    public NodeModel parentApprovalNode() {
        NodeModel parentNode = this.getParentNode();
        if (parentNode.approvalOrMajor()) {
            // 父节点为审批节点或主办发起节点
            return parentNode;
        }

        // 继续找父审批节点
        return parentNode.parentApprovalNode();
    }

    /**
     * 判断是否为审批或主办节点
     *
     * @return true 是 false 否
     */
    public boolean approvalOrMajor() {
        return TaskType.approval.eq(type) || TaskType.major.eq(type);
    }

    /**
     * 判断是否为条件节点
     *
     * @return true 是 false 否
     */
    public boolean conditionNode() {
        return TaskType.conditionNode.eq(type) || TaskType.conditionBranch.eq(type);
    }

    /**
     * 判断是否为抄送节点
     *
     * @return true 是 false 否
     */
    public boolean ccNode() {
        return TaskType.cc.eq(type);
    }

    /**
     * 判断是否为并行节点
     *
     * @return true 是 false 否
     */
    public boolean parallelNode() {
        return TaskType.parallelBranch.eq(type);
    }

    /**
     * 判断是否为包容节点
     *
     * @return true 是 false 否
     */
    public boolean inclusiveNode() {
        return TaskType.inclusiveBranch.eq(type);
    }

    /**
     * 判断是否为路由节点
     *
     * @return true 是 false 否
     */
    public boolean routeNode() {
        return TaskType.routeBranch.eq(type);
    }

    /**
     * 判断是否为全部人员参与审批分组策略
     *
     * @return true 是 false 否
     */
    public boolean allJoinGroupStrategy() {
        return Objects.equals(1, groupStrategy);
    }

    /**
     * 判断是否为调用子流程节点
     *
     * @return true 是 false 否
     */
    public boolean callProcessNode() {
        return TaskType.callProcess.eq(type);
    }

    /**
     * 判断是否为结束节点
     *
     * @return true 是 false 否
     */
    public boolean endNode() {
        return TaskType.end.eq(type);
    }

    public NodeAssignee nextNodeAssignee(Execution execution, String assigneeId) {
        boolean findTaskActor = false;
        NodeAssignee nextNodeAssignee = null;
        List<NodeAssignee> nodeAssigneeList = this.getNodeAssigneeList();
        if (ObjectUtils.isEmpty(nodeAssigneeList) || allJoinGroupStrategy()) {
            /*
             * 模型未设置处理人、分组策略全部人员参与审批，那么需要获取自定义参与者
             */
            List<FlwTaskActor> taskActors = execution.getProviderTaskActors(this);
            if (ObjectUtils.isNotEmpty(taskActors)) {
                for (FlwTaskActor taskActor : taskActors) {
                    if (findTaskActor) {
                        // 找到下一个执行人
                        nextNodeAssignee = NodeAssignee.of(taskActor);
                        break;
                    }

                    // 判断找到当前任务实际办理人
                    if (Objects.equals(taskActor.getActorId(), assigneeId)) {
                        findTaskActor = true;
                    }
                }
            }
        } else {
            /*
             * 模型中去找下一个执行者
             */
            for (NodeAssignee nodeAssignee : nodeAssigneeList) {
                if (findTaskActor) {
                    // 找到下一个执行人
                    nextNodeAssignee = nodeAssignee;
                    break;
                }
                if (Objects.equals(nodeAssignee.getId(), assigneeId)) {
                    findTaskActor = true;
                }
            }
        }
        return nextNodeAssignee;
    }

    /**
     * 获取所有父节点条件节点子节点key列表
     */
    public List<String> parentConditionNodeKeys() {
        return ModelHelper.getParentConditionNodeKeys(this);
    }

    /**
     * 执行触发器
     *
     * @param execution {@link Execution}
     * @param supplier  执行默认触发器执行函数
     */
    public boolean executeTrigger(Execution execution, Supplier<Boolean> supplier, Function<Execution, Boolean> callAsync) {
        boolean callSupplier = true;
        boolean flag = false;
        Map<String, Object> extendConfig = this.getExtendConfig();
        if (null != extendConfig) {
            Object _trigger = extendConfig.get("trigger");
            if (null != _trigger) {
                try {
                    callSupplier = false;
                    Class<?> triggerClass = Class.forName((String) _trigger);
                    if (TaskTrigger.class.isAssignableFrom(triggerClass)) {
                        TaskTrigger taskTrigger = (TaskTrigger) ObjectUtils.newInstance(triggerClass);
                        flag = taskTrigger.execute(this, execution, callAsync);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        // 使用默认触发器
        if (null != supplier && callSupplier) {
            flag = supplier.get();
        }
        Assert.isFalse(flag, "trigger execute error");
        return flag;
    }

    /**
     * 判断抄送任务是否允许执行下一个节点
     */
    public boolean ccExecNextNode(NodeModel ccNextNode) {
        boolean _exec = true;
        // 非直属子节点情况判断分支问题
        if (!Objects.equals(this.nodeKey, ccNextNode.getParentNode().getNodeKey())) {
            // 直接父节点为并行分支或包容分支情况处理
            NodeModel parentNode = this.getParentNode();
            if (TaskType.parallelBranch.eq(parentNode.getType())) {
                // 并行分支
                List<ConditionNode> parallelNodes = parentNode.getParallelNodes();
                if (null != parallelNodes) {
                    // 最后一个并行节点执行下一步
                    NodeModel parallelChildNode = parallelNodes.get(parallelNodes.size() - 1).getChildNode();
                    if (null != parallelChildNode) {
                        _exec = Objects.equals(this.nodeKey, parallelChildNode.getNodeKey());
                    }
                }
            } else if (TaskType.inclusiveBranch.eq(parentNode.getType())) {
                // 包容分支，为最后一个满足条件节点执行下一步
                _exec = Objects.equals(1, FlowDataTransfer.get(FlowConstants.processLastConditionNode));
                if (_exec) {
                    FlowDataTransfer.removeByKey(FlowConstants.processLastConditionNode);
                }
            }
        }
        return _exec;
    }

    /**
     * 克隆基本信息
     * <p>不包含节点信息内容，避免循环递归序列化异常</p>
     *
     * @return 节点模型
     */
    public NodeModel cloneBaseInfo() {
        NodeModel nodeModel = new NodeModel();
        nodeModel.setNodeName(this.nodeName);
        nodeModel.setNodeKey(this.nodeKey);
        nodeModel.setCallProcess(this.callProcess);
        nodeModel.setActionUrl(this.actionUrl);
        nodeModel.setType(this.type);
        nodeModel.setSetType(this.setType);
        nodeModel.setNodeAssigneeList(this.nodeAssigneeList);
        nodeModel.setNodeCandidate(this.nodeCandidate);
        nodeModel.setExamineLevel(this.examineLevel);
        nodeModel.setExamineMode(this.examineMode);
        nodeModel.setDirectorLevel(this.directorLevel);
        nodeModel.setDirectorMode(this.directorMode);
        nodeModel.setSelectMode(this.selectMode);
        nodeModel.setTermAuto(this.termAuto);
        nodeModel.setTerm(this.term);
        nodeModel.setTermMode(this.termMode);
        nodeModel.setTypeOfApprove(this.typeOfApprove);
        nodeModel.setPassWeight(this.passWeight);
        nodeModel.setRejectStrategy(this.rejectStrategy);
        nodeModel.setRejectStart(this.rejectStart);
        nodeModel.setRemind(this.remind);
        nodeModel.setAllowSelection(this.allowSelection);
        nodeModel.setAllowTransfer(this.allowTransfer);
        nodeModel.setAllowAppendNode(this.allowAppendNode);
        nodeModel.setAllowRollback(this.allowRollback);
        nodeModel.setApproveSelf(this.approveSelf);
        nodeModel.setExtendConfig(this.extendConfig);
        nodeModel.setTriggerType(this.triggerType);
        nodeModel.setDelayType(this.delayType);
        return nodeModel;
    }
}
