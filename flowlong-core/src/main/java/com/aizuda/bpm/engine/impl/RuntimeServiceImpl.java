/*
 * Copyright 2023-2025 Licensed under the Dual Licensing
 * website: https://aizuda.com
 */
package com.aizuda.bpm.engine.impl;

import com.aizuda.bpm.engine.*;
import com.aizuda.bpm.engine.assist.Assert;
import com.aizuda.bpm.engine.assist.DateUtils;
import com.aizuda.bpm.engine.core.Execution;
import com.aizuda.bpm.engine.core.FlowCreator;
import com.aizuda.bpm.engine.core.FlowLongContext;
import com.aizuda.bpm.engine.core.enums.InstanceEventType;
import com.aizuda.bpm.engine.core.enums.InstanceState;
import com.aizuda.bpm.engine.core.enums.TaskEventType;
import com.aizuda.bpm.engine.core.enums.TaskType;
import com.aizuda.bpm.engine.dao.FlwExtInstanceDao;
import com.aizuda.bpm.engine.dao.FlwHisInstanceDao;
import com.aizuda.bpm.engine.dao.FlwInstanceDao;
import com.aizuda.bpm.engine.entity.*;
import com.aizuda.bpm.engine.listener.InstanceListener;
import com.aizuda.bpm.engine.model.ConditionNode;
import com.aizuda.bpm.engine.model.ModelHelper;
import com.aizuda.bpm.engine.model.NodeModel;
import com.aizuda.bpm.engine.model.ProcessModel;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * 流程实例运行业务类
 *
 * <p>
 * <a href="https://aizuda.com">官网</a>尊重知识产权，不允许非法使用，后果自负
 * </p>
 *
 * @author hubin
 * @since 1.0
 */
public class RuntimeServiceImpl implements RuntimeService {
    protected final InstanceListener instanceListener;
    protected final FlowLongIdGenerator flowLongIdGenerator;
    protected final QueryService queryService;
    protected final TaskService taskService;
    protected final FlwInstanceDao instanceDao;
    protected final FlwHisInstanceDao hisInstanceDao;
    protected final FlwExtInstanceDao extInstanceDao;

    public RuntimeServiceImpl(InstanceListener instanceListener, FlowLongIdGenerator flowLongIdGenerator, QueryService queryService, TaskService taskService,
                              FlwInstanceDao instanceDao, FlwHisInstanceDao hisInstanceDao, FlwExtInstanceDao extInstanceDao) {
        this.instanceListener = instanceListener;
        this.flowLongIdGenerator = flowLongIdGenerator;
        this.queryService = queryService;
        this.taskService = taskService;
        this.instanceDao = instanceDao;
        this.hisInstanceDao = hisInstanceDao;
        this.extInstanceDao = extInstanceDao;
    }

    /**
     * 创建活动实例
     */
    @Override
    public FlwInstance createInstance(FlwProcess flwProcess, FlowCreator flowCreator, Map<String, Object> args, NodeModel nodeModel,
                                      boolean saveAsDraft, Supplier<FlwInstance> supplier) {
        FlwInstance flwInstance = null;
        if (null != supplier) {
            flwInstance = supplier.get();
        }
        if (null == flwInstance) {
            flwInstance = new FlwInstance();
        }
        flwInstance.setCreateTime(DateUtils.getCurrentDate());
        flwInstance.setFlowCreator(flowCreator);
        flwInstance.setCurrentNodeName(nodeModel.getNodeName());
        flwInstance.setCurrentNodeKey(nodeModel.getNodeKey());
        flwInstance.setLastUpdateBy(flwInstance.getCreateBy());
        flwInstance.setLastUpdateTime(flwInstance.getCreateTime());
        flwInstance.setProcessId(flwProcess.getId());
        flwInstance.putAllVariable(args);

        // 重新加载流程模型
        ModelHelper.reloadProcessModel(flwProcess.model(), t -> flwProcess.setModelContent2Json(t.cleanParentNode()));

        // 保存实例
        this.saveInstance(flwInstance, flwProcess, saveAsDraft, flowCreator);
        return flwInstance;
    }

    /**
     * 根据流程实例ID获取流程实例模型
     *
     * @param instanceId 流程实例ID
     * @return {@link ProcessModel}
     */
    @Override
    public ProcessModel getProcessModelByInstanceId(Long instanceId) {
        return FlwExtInstance.cacheProcessModelById(instanceId, () -> {
            FlwExtInstance fri = extInstanceDao.selectById(instanceId);
            Assert.isNull(fri, "The process instance model does not exist.");
            return fri.model(true);
        });
    }

    @Override
    public boolean addVariable(Long instanceId, Map<String, Object> args, Function<FlwInstance, FlwInstance> function) {
        FlwInstance flwInstance = instanceDao.selectById(instanceId);
        Assert.isNull(flwInstance, "not found instance");
        FlwInstance fi = function.apply(flwInstance);
        fi.setId(instanceId);
        Map<String, Object> data = flwInstance.variableToMap();
        data.putAll(args);
        fi.putAllVariable(data);
        return instanceDao.updateById(fi);
    }

    /**
     * 删除活动流程实例数据，更新历史流程实例的状态、结束时间
     */
    @Override
    public boolean endInstance(Execution execution, Long instanceId, NodeModel endNode, InstanceState instanceState) {
        FlwInstance flwInstance = instanceDao.selectById(instanceId);
        if (null != flwInstance) {
            instanceDao.deleteById(instanceId);
            hisInstanceDao.updateById(this.getFlwHisInstance(instanceId, endNode, flwInstance, instanceState));
            // 流程实例监听器通知
            InstanceEventType iet = InstanceEventType.end;
            if (instanceState == InstanceState.autoPass) {
                iet = InstanceEventType.autoComplete;
            } else if (instanceState == InstanceState.autoReject) {
                iet = InstanceEventType.autoReject;
            }
            this.instanceNotify(iet, () -> hisInstanceDao.selectById(instanceId), endNode, execution.getFlowCreator());

            /*
             * 实例为子流程，重启动父流程任务
             */
            if (null != flwInstance.getParentInstanceId()) {
                // 结束调用外部流程任务
                taskService.endCallProcessTask(flwInstance.getProcessId(), flwInstance.getId());

                // 重启父流程实例
                FlwInstance pfi = instanceDao.selectById(flwInstance.getParentInstanceId());
                execution.setFlwInstance(flwInstance);
                execution.setParentFlwInstance(pfi);
                execution.restartProcessInstance(pfi.getProcessId(), pfi.getCurrentNodeKey());
            }
        }
        return true;
    }

    protected FlwHisInstance getFlwHisInstance(Long instanceId, NodeModel endNode, FlwInstance flwInstance, InstanceState instanceState) {
        FlwHisInstance his = new FlwHisInstance();
        his.setId(instanceId);
        String currentNodeName = instanceState.name();
        String currentNodeKey = instanceState.name();
        if (null != endNode) {
            NodeModel childNode = endNode.getChildNode();
            if (null == childNode || TaskType.end.ne(childNode.getType())) {
                childNode = endNode;
            }
            // 记录结束节点
            currentNodeName = childNode.getNodeName();
            currentNodeKey = childNode.getNodeKey();
        }
        his.setPriority(flwInstance.getPriority());
        his.setInstanceNo(flwInstance.getInstanceNo());
        his.setBusinessKey(flwInstance.getBusinessKey());
        his.setVariable(flwInstance.getVariable());
        his.setCurrentNodeName(currentNodeName);
        his.setCurrentNodeKey(currentNodeKey);
        his.setExpireTime(flwInstance.getExpireTime());
        his.setCreateTime(flwInstance.getCreateTime());
        his.setLastUpdateBy(flwInstance.getLastUpdateBy());
        his.setLastUpdateTime(flwInstance.getLastUpdateTime());
        his.calculateDuration();
        return his.instanceState(instanceState);
    }

    protected void instanceNotify(InstanceEventType eventType, Supplier<FlwHisInstance> supplier, NodeModel nodeModel, FlowCreator flowCreator) {
        if (null != instanceListener) {
            instanceListener.notify(eventType, supplier, nodeModel, flowCreator);
        }
    }

    /**
     * 流程实例数据会保存至活动实例表、历史实例表
     *
     * @param flwInstance 流程实例对象
     * @param flwProcess  流程定义对象
     * @param saveAsDraft 暂存草稿
     * @param flowCreator 处理人员
     */
    @Override
    public void saveInstance(FlwInstance flwInstance, FlwProcess flwProcess, boolean saveAsDraft, FlowCreator flowCreator) {
        // 保存流程实例
        flwInstance.setId(flowLongIdGenerator.getId(flwInstance.getId()));
        instanceDao.insert(flwInstance);

        // 保存历史实例设置为活的状态
        FlwHisInstance fhi = FlwHisInstance.of(flwInstance, saveAsDraft ? InstanceState.saveAsDraft : InstanceState.active, false);
        if (hisInstanceDao.insert(fhi)) {

            // 保存扩展流程实例
            extInstanceDao.insert(FlwExtInstance.of(flwInstance, flwProcess));

            // 流程实例监听器通知
            this.instanceNotify(InstanceEventType.start, () -> fhi, null, flowCreator);
        }
    }

    @Override
    public boolean suspendInstanceById(Long instanceId, FlowCreator flowCreator) {
        return this.updateInstanceStateById(instanceId, InstanceState.suspend, flowCreator);
    }

    @Override
    public boolean activeInstanceById(Long instanceId, FlowCreator flowCreator) {
        return this.updateInstanceStateById(instanceId, InstanceState.active, flowCreator);
    }

    /**
     * 根据流程实例ID更新流程实例状态
     *
     * @param instanceId 流程实例ID
     * @param instanceState 流程实例状态
     * @param flowCreator 流程创建者
     */
    protected boolean updateInstanceStateById(Long instanceId, InstanceState instanceState, FlowCreator flowCreator) {
        FlwHisInstance dbFhi = hisInstanceDao.selectById(instanceId);
        if (null != dbFhi) {
            Long parentInstanceId = dbFhi.getParentInstanceId();
            if (null != parentInstanceId) {
                Assert.illegal("Sub processes are not allowed. parentInstanceId=" + parentInstanceId);
            }
            // 挂起当前主流程
            if (this.updateInstanceState(dbFhi, instanceState, flowCreator)) {
                // 子流程挂起
                hisInstanceDao.selectListByParentInstanceId(dbFhi.getParentInstanceId()).ifPresent(t ->
                        t.forEach(f -> this.updateInstanceState(f, instanceState, flowCreator)));
                return true;
            }
        }
        return false;
    }

    /**
     * 更新流程实例状态
     *
     * @param dbFhi 数据库流程实例历史对象
     * @param instanceState 流程实例状态
     * @param flowCreator 流程创建者
     */
    protected boolean updateInstanceState(FlwHisInstance dbFhi, InstanceState instanceState, FlowCreator flowCreator) {
        FlwHisInstance fhi = new FlwHisInstance();
        fhi.setId(dbFhi.getId());
        if (hisInstanceDao.updateById(fhi.instanceState(instanceState))) {
            // 流程实例监听器通知
            this.instanceNotify(InstanceEventType.suspend, () -> dbFhi.instanceState(instanceState), null, flowCreator);
            return true;
        }
        return false;
    }

    @Override
    public boolean reject(Long instanceId, FlwTask currentFlwTask, FlowCreator flowCreator) {
        return this.forceComplete(instanceId, currentFlwTask, flowCreator, InstanceEventType.rejectComplete, InstanceState.reject, TaskEventType.reject);
    }

    @Override
    public boolean revoke(Long instanceId, FlwTask currentFlwTask, FlowCreator flowCreator) {
        return this.forceComplete(instanceId, currentFlwTask, flowCreator, InstanceEventType.revokeComplete, InstanceState.revoke, TaskEventType.revoke);
    }

    @Override
    public boolean timeout(Long instanceId, FlwTask currentFlwTask, FlowCreator flowCreator) {
        return this.forceComplete(instanceId, currentFlwTask, flowCreator, InstanceEventType.timeoutComplete, InstanceState.timeout, TaskEventType.timeout);
    }

    /**
     * 强制终止活动实例,并强制完成活动任务
     *
     * @param instanceId     流程实例ID
     * @param currentFlwTask 当前任务
     * @param flowCreator    处理人员
     */
    @Override
    public boolean terminate(Long instanceId, FlwTask currentFlwTask, FlowCreator flowCreator) {
        return this.forceComplete(instanceId, currentFlwTask, flowCreator, InstanceEventType.rejectComplete, InstanceState.terminate, TaskEventType.terminate);
    }

    /**
     * 强制完成流程实例
     *
     * @param instanceId     流程实例ID
     * @param currentFlwTask 当前任务
     * @param flowCreator    处理人员
     * @param instanceState  流程实例最终状态
     * @param eventType      监听事件类型
     */
    protected boolean forceComplete(Long instanceId, FlwTask currentFlwTask, FlowCreator flowCreator, InstanceEventType instanceEventType,
                                    InstanceState instanceState, TaskEventType eventType) {
        FlwInstance flwInstance = instanceDao.selectById(instanceId);
        if (null == flwInstance) {
            // 流程审批完成，用户执行撤销完成
            if (InstanceEventType.revokeComplete == instanceEventType) {
                FlwHisInstance hisInstance = hisInstanceDao.selectById(instanceId);
                if (null != hisInstance) {
                    FlwHisInstance fhi = new FlwHisInstance();
                    fhi.setId(hisInstance.getId());
                    if (hisInstanceDao.updateById(fhi.instanceState(InstanceState.revoke))) {
                        // 流程实例监听器通知
                        this.instanceNotify(instanceEventType, () -> hisInstance, null, flowCreator);
                        return true;
                    }
                }
            }
            return false;
        }

        final Long parentInstanceId = flwInstance.getParentInstanceId();
        if (null != parentInstanceId) {
            // 找到主流程去执行完成逻辑
            this.forceComplete(parentInstanceId, currentFlwTask, flowCreator, instanceEventType, instanceState, eventType);
        } else {
            // 结束所有子流程实例
            instanceDao.selectListByParentInstanceId(flwInstance.getId()).ifPresent(f -> f.forEach(t ->
                    this.forceCompleteAll(t, currentFlwTask, flowCreator, instanceEventType, instanceState, eventType)));
        }

        // 结束当前流程实例
        this.forceCompleteAll(flwInstance, currentFlwTask, flowCreator, instanceEventType, instanceState, eventType);
        return true;
    }

    /**
     * 强制完成流程所有实例
     */
    protected void forceCompleteAll(FlwInstance flwInstance, FlwTask currentFlwTask, FlowCreator flowCreator, InstanceEventType instanceEventType,
                                    InstanceState instanceState, TaskEventType eventType) {

        // 实例相关任务强制完成
        if (taskService.forceCompleteAllTask(flwInstance.getId(), currentFlwTask, flowCreator, instanceState, eventType)) {

            // 更新历史实例设置状态为终止
            FlwHisInstance flwHisInstance = FlwHisInstance.of(flwInstance, instanceState, true);
            hisInstanceDao.updateById(flwHisInstance);

            // 删除实例
            instanceDao.deleteById(flwInstance.getId());

            // 流程实例监听器通知
            this.instanceNotify(instanceEventType, () -> flwHisInstance, null, flowCreator);
        }
    }

    /**
     * 更新活动实例
     */
    @Override
    public void updateInstance(FlwInstance flwInstance) {
        Assert.illegal(null == flwInstance || null == flwInstance.getId(),
                "instance id cannot be empty");
        instanceDao.updateById(flwInstance);
    }

    @Override
    public boolean updateInstanceModelById(Long instanceId, ProcessModel processModel) {
        // 使缓存失效
        FlowLongContext.invalidateProcessModel(FlowConstants.processInstanceCacheKey + instanceId);

        // 更新流程实例模型
        FlwExtInstance extInstance = new FlwExtInstance();
        extInstance.setId(instanceId);
        extInstance.setModelContent(FlowLongContext.toJson(processModel.cleanParentNode()));
        return extInstanceDao.updateById(extInstance);
    }

    /**
     * 级联删除指定流程实例的所有数据
     * <p>
     * 删除表 flw_his_task_actor, flw_his_task, flw_task_actor, flw_task, flw_his_instance, flw_ext_instance, flw_instance
     * </p>
     *
     * @param processId 流程ID
     */
    @Override
    public void cascadeRemoveByProcessId(Long processId) {
        hisInstanceDao.selectListByProcessId(processId).ifPresent(hisInstances -> {

            // 删除活动任务相关信息
            taskService.cascadeRemoveByInstanceIds(hisInstances.stream().map(FlowEntity::getId).collect(Collectors.toList()));

            // 删除扩展实例
            extInstanceDao.deleteByProcessId(processId);

            // 删除历史实例
            hisInstanceDao.deleteByProcessId(processId);

            // 删除实例
            instanceDao.deleteByProcessId(processId);
        });
    }

    @Override
    public void cascadeRemoveByInstanceId(Long instanceId) {
        // 删除活动任务相关信息
        if (taskService.cascadeRemoveByInstanceIds(Collections.singletonList(instanceId))) {
            // 删除扩展实例
            extInstanceDao.deleteById(instanceId);

            // 删除历史实例
            hisInstanceDao.deleteById(instanceId);

            // 删除实例
            instanceDao.deleteById(instanceId);
        }
    }

    @Override
    public boolean destroyByInstanceId(Long instanceId, Map<String, Object> args) {
        FlwInstance flwInstance = instanceDao.selectById(instanceId);
        if (null == flwInstance) {
            // 不存在返回失败
            return false;
        }

        // 删除活动任务相关信息
        if (taskService.cascadeRemoveByInstanceIds(Collections.singletonList(instanceId))) {

            // 删除实例
            instanceDao.deleteById(instanceId);

            // 更新作废状态
            FlwHisInstance fhi = new FlwHisInstance();
            fhi.setCreateTime(flwInstance.getCreateTime());
            fhi.instanceState(InstanceState.destroy);
            fhi.setVariable(flwInstance.getVariable());
            fhi.putAllVariable(args);
            fhi.setId(instanceId);
            fhi.calculateDuration();
            return hisInstanceDao.updateById(fhi);
        }
        return false;
    }

    @Override
    public void appendNodeModel(Long taskId, NodeModel nodeModel, boolean beforeAfter) {
        FlwTask flwTask = queryService.getTask(taskId);
        FlwExtInstance flwExtInstance = extInstanceDao.selectById(flwTask.getInstanceId());
        final String appendTaskKey = flwTask.getTaskKey();

        ProcessModel processModel = flwExtInstance.model();
        NodeModel selectNode = processModel.getNode(appendTaskKey);
        if (beforeAfter) {
            // 前置追溯父节点
            selectNode = selectNode.getParentNode();
        }

        // 条件分支
        List<ConditionNode> conditionNodes = selectNode.getConditionNodes();
        if (null == conditionNodes) {
            // 并行分支
            conditionNodes = selectNode.getParallelNodes();
            if (null == conditionNodes) {
                // 包容分支
                conditionNodes = selectNode.getInclusiveNodes();
            }
        }
        if (null != conditionNodes) {
            boolean findIt = false;
            NodeModel childNode = selectNode.getChildNode();
            if (null != childNode && Objects.equals(childNode.getNodeKey(), appendTaskKey)) {
                // 为直接子节点情况
                nodeModel.setChildNode(childNode);
                selectNode.setChildNode(nodeModel);
                findIt = true;
            }
            if (!findIt) {
                // 如果直接跟着条件节点，找到分支作为父节点
                for (ConditionNode conditionNode : conditionNodes) {
                    NodeModel conditionChildNode = conditionNode.getChildNode();
                    if (null == conditionChildNode) {
                        continue;
                    }
                    if (Objects.equals(conditionChildNode.getNodeKey(), appendTaskKey)) {
                        nodeModel.setChildNode(conditionChildNode);
                        conditionNode.setChildNode(nodeModel);
                        break;
                    }
                }
            }
        } else {
            // 当前节点即为真实父节点
            nodeModel.setChildNode(selectNode.getChildNode());
            selectNode.setChildNode(nodeModel);
        }

        // 更新最新模型
        FlwExtInstance temp = new FlwExtInstance();
        temp.setId(flwExtInstance.getId());
        temp.setModelContent(FlowLongContext.toJson(processModel.cleanParentNode()));
        Assert.isFalse(extInstanceDao.updateById(temp), "Update FlwExtInstance Failed");

        // 使缓存失效
        FlowLongContext.invalidateProcessModel(flwExtInstance.modelCacheKey());
    }
}
