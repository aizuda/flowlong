/*
 * Copyright 2023-2025 Licensed under the Dual Licensing
 * website: https://aizuda.com
 */
package com.aizuda.bpm.engine.entity;

import com.aizuda.bpm.engine.FlowConstants;
import com.aizuda.bpm.engine.ProcessModelCache;
import com.aizuda.bpm.engine.assist.Assert;
import com.aizuda.bpm.engine.assist.DateUtils;
import com.aizuda.bpm.engine.core.Execution;
import com.aizuda.bpm.engine.core.FlowCreator;
import com.aizuda.bpm.engine.core.FlowLongContext;
import com.aizuda.bpm.engine.core.enums.FlowState;
import com.aizuda.bpm.engine.model.ModelHelper;
import com.aizuda.bpm.engine.model.NodeModel;
import com.aizuda.bpm.engine.model.ProcessModel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

/**
 * 流程定义实体类
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
@ToString
public class FlwProcess extends FlowEntity implements ProcessModelCache {
    /**
     * 流程定义 key 唯一标识
     */
    protected String processKey;
    /**
     * 流程定义名称
     */
    protected String processName;
    /**
     * 流程图标地址
     */
    protected String processIcon;
    /**
     * 流程定义类型（预留字段）
     */
    protected String processType;
    /**
     * 流程版本
     */
    protected Integer processVersion;
    /**
     * 当前流程的实例url（一般为流程第一步的url）
     * 该字段可以直接打开流程申请的表单
     */
    protected String instanceUrl;
    /**
     * 备注说明
     */
    protected String remark;
    /**
     * 使用范围 0，全员 1，指定人员（业务关联） 2，均不可提交
     */
    protected Integer useScope;
    /**
     * 流程状态 0，不可用 1，可用 2，历史版本
     */
    protected Integer processState;
    /**
     * 流程模型定义JSON内容
     */
    protected String modelContent;
    /**
     * 排序
     */
    protected Integer sort;

    public void setFlowState(FlowState flowState) {
        this.processState = flowState.getValue();
    }

    @Override
    public String modelCacheKey() {
        return FlowConstants.processCacheKey + this.id;
    }

    public static FlwProcess of(FlowCreator flowCreator, ProcessModel processModel, int processVersion, String jsonString) {
        FlwProcess process = new FlwProcess();
        process.setProcessVersion(processVersion);
        process.setFlowState(FlowState.active);
        process.setProcessKey(processModel.getKey());
        process.setProcessName(processModel.getName());
        process.setInstanceUrl(processModel.getInstanceUrl());
        process.setUseScope(0);
        process.setSort(0);
        process.setFlowCreator(flowCreator);
        process.setCreateTime(DateUtils.getCurrentDate());
        return process.formatModelContent(jsonString);
    }

    /**
     * 执行开始模型
     *
     * @param flowLongContext 流程引擎上下文
     * @param flowCreator     流程实例任务创建者
     * @param function        流程执行对象处理函数
     * @return 流程实例
     */
    public Optional<FlwInstance> executeStartModel(FlowLongContext flowLongContext, FlowCreator flowCreator, Function<NodeModel, Execution> function) {
        FlwInstance flwInstance = null;
        if (null != this.modelContent) {
            NodeModel nodeModel = this.model().getNodeConfig();
            Assert.isNull(nodeModel, "流程定义[processName=" + this.processName + ", processVersion=" + this.processVersion + "]没有开始节点");
            Assert.isFalse(flowLongContext.getTaskActorProvider().isAllowed(nodeModel, flowCreator), "No permission to execute");
            Assert.isTrue(ModelHelper.checkNodeModel(nodeModel) > 0, "process nodeModel config error");
            // 回调执行创建实例
            Execution execution = function.apply(nodeModel);
            // 重新渲染逻辑节点
            nodeModel = execution.getProcessModel().getNodeConfig();
            // 创建首个审批任务
            flowLongContext.createTask(execution, nodeModel);
            // 当前执行实例
            flwInstance = execution.getFlwInstance();
        }
        return Optional.ofNullable(flwInstance);
    }

    /**
     * 流程状态验证
     *
     * @return 流程定义实体
     */
    public FlwProcess checkState() {
        if (Objects.equals(0, this.processState)) {
            Assert.illegal("指定的流程定义[id=" + this.id + ",processVersion=" + this.processVersion + "]为非活动状态");
        }
        return this;
    }

    /**
     * 格式化 JSON 模型内容
     *
     * @param modelContent JSON 模型内容
     * @return 流程定义实体
     */
    public FlwProcess formatModelContent(String modelContent) {
        return setModelContent2Json(FlowLongContext.fromJson(modelContent, ProcessModel.class));
    }

    /**
     * 设置 JSON 模型内容
     *
     * @param processModel 模型内容
     * @return 流程定义实体
     */
    public FlwProcess setModelContent2Json(ProcessModel processModel) {
        this.modelContent = FlowLongContext.toJson(processModel);
        return this;
    }

    /**
     * 下一个流程版本
     *
     * @return 下一个流程版本
     */
    public int nextProcessVersion() {
        return processVersion + 1;
    }
}
