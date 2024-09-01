/*
 * Copyright 2023-2025 Licensed under the AGPL License
 */
package com.aizuda.bpm.engine.impl;

import com.aizuda.bpm.engine.FlowConstants;
import com.aizuda.bpm.engine.QueryService;
import com.aizuda.bpm.engine.RuntimeService;
import com.aizuda.bpm.engine.TaskService;
import com.aizuda.bpm.engine.assist.Assert;
import com.aizuda.bpm.engine.assist.DateUtils;
import com.aizuda.bpm.engine.assist.ObjectUtils;
import com.aizuda.bpm.engine.core.Execution;
import com.aizuda.bpm.engine.core.FlowCreator;
import com.aizuda.bpm.engine.core.FlowLongContext;
import com.aizuda.bpm.engine.core.enums.EventType;
import com.aizuda.bpm.engine.core.enums.InstanceState;
import com.aizuda.bpm.engine.dao.FlwExtInstanceDao;
import com.aizuda.bpm.engine.dao.FlwHisInstanceDao;
import com.aizuda.bpm.engine.dao.FlwInstanceDao;
import com.aizuda.bpm.engine.entity.*;
import com.aizuda.bpm.engine.listener.InstanceListener;
import com.aizuda.bpm.engine.model.ConditionNode;
import com.aizuda.bpm.engine.model.ModelHelper;
import com.aizuda.bpm.engine.model.NodeModel;
import com.aizuda.bpm.engine.model.ProcessModel;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * 流程实例运行业务类
 *
 * <p>
 * 尊重知识产权，不允许非法使用，后果自负
 * </p>
 *
 * @author hubin
 * @since 1.0
 */
public class RuntimeServiceImpl implements RuntimeService {
    private final InstanceListener instanceListener;
    private final QueryService queryService;
    private final TaskService taskService;
    private final FlwInstanceDao instanceDao;
    private final FlwHisInstanceDao hisInstanceDao;
    private final FlwExtInstanceDao extInstanceDao;

    public RuntimeServiceImpl(InstanceListener instanceListener, QueryService queryService, TaskService taskService,
                              FlwInstanceDao instanceDao, FlwHisInstanceDao hisInstanceDao, FlwExtInstanceDao extInstanceDao) {
        this.instanceListener = instanceListener;
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
    public FlwInstance createInstance(FlwProcess flwProcess, FlowCreator flowCreator, Map<String, Object> args, NodeModel nodeModel, Supplier<FlwInstance> supplier) {
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
        flwInstance.setMapVariable(args);

        // 重新加载流程模型
        ModelHelper.reloadProcessModel(flwProcess.model(), flwProcess::setModelContent2Json);

        // 保存实例
        this.saveInstance(flwInstance, flwProcess, flowCreator);
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
        FlwExtInstance flwExtInstance = extInstanceDao.selectById(instanceId);
        Assert.isNull(flwExtInstance, "The process instance model does not exist.");
        return flwExtInstance.model();
    }

    /**
     * 向活动实例临时添加全局变量数据
     *
     * @param instanceId 实例id
     * @param args       变量数据
     */
    @Override
    public void addVariable(Long instanceId, Map<String, Object> args) {
        FlwInstance flwInstance = instanceDao.selectById(instanceId);
        Map<String, Object> data = flwInstance.variableToMap();
        data.putAll(args);
        FlwInstance temp = new FlwInstance();
        temp.setId(instanceId);
        temp.setMapVariable(data);
        instanceDao.updateById(temp);
    }


    /**
     * 删除活动流程实例数据，更新历史流程实例的状态、结束时间
     */
    @Override
    public boolean endInstance(Execution execution, Long instanceId, NodeModel endNode) {
        FlwInstance flwInstance = instanceDao.selectById(instanceId);
        if (null != flwInstance) {
            FlwHisInstance his = new FlwHisInstance();
            his.setId(instanceId);
            InstanceState instanceState = InstanceState.complete;
            his.setInstanceState(instanceState);
            if (null != endNode) {
                his.setCurrentNodeName(endNode.getNodeName());
                his.setCurrentNodeKey(endNode.getNodeKey());
            } else {
                his.setCurrentNodeName(instanceState.name());
                his.setCurrentNodeKey(instanceState.name());
            }
            his.setCreateTime(flwInstance.getCreateTime());
            his.setLastUpdateBy(flwInstance.getLastUpdateBy());
            his.setLastUpdateTime(flwInstance.getLastUpdateTime());
            his.calculateDuration();
            instanceDao.deleteById(instanceId);
            hisInstanceDao.updateById(his);
            // 流程实例监听器通知
            this.instanceNotify(EventType.end, () -> hisInstanceDao.selectById(instanceId), execution.getFlowCreator());

            /*
             * 实例为子流程，重启动父流程任务
             */
            if (null != flwInstance.getParentInstanceId()) {
                // 重启父流程实例
                FlwInstance parentFlwInstance = instanceDao.selectById(flwInstance.getParentInstanceId());
                execution.setFlwInstance(parentFlwInstance);
                execution.restartProcessInstance(parentFlwInstance.getProcessId(), parentFlwInstance.getCurrentNodeKey());

                // 结束调用外部流程任务
                taskService.endCallProcessTask(flwInstance.getProcessId(), flwInstance.getId());
            }
        }
        return true;
    }

    protected void instanceNotify(EventType eventType, Supplier<FlwHisInstance> supplier, FlowCreator flowCreator) {
        if (null != instanceListener) {
            instanceListener.notify(eventType, supplier, null, flowCreator);
        }
    }

    /**
     * 流程实例数据会保存至活动实例表、历史实例表
     *
     * @param flwInstance 流程实例对象
     * @param flwProcess  流程定义对象
     * @param flowCreator 处理人员
     */
    @Override
    public void saveInstance(FlwInstance flwInstance, FlwProcess flwProcess, FlowCreator flowCreator) {
        // 保存流程实例
        instanceDao.insert(flwInstance);

        // 保存历史实例设置为活的状态
        FlwHisInstance flwHisInstance = FlwHisInstance.of(flwInstance, InstanceState.active);
        hisInstanceDao.insert(flwHisInstance);

        // 保存扩展流程实例
        extInstanceDao.insert(FlwExtInstance.of(flwInstance, flwProcess));

        // 流程实例监听器通知
        this.instanceNotify(EventType.start, () -> flwHisInstance, flowCreator);
    }

    @Override
    public void reject(Long instanceId, FlowCreator flowCreator) {
        this.forceComplete(instanceId, flowCreator, InstanceState.reject, EventType.reject);
    }

    @Override
    public void revoke(Long instanceId, FlowCreator flowCreator) {
        this.forceComplete(instanceId, flowCreator, InstanceState.revoke, EventType.revoke);
    }

    @Override
    public void timeout(Long instanceId, FlowCreator flowCreator) {
        this.forceComplete(instanceId, flowCreator, InstanceState.timeout, EventType.timeout);
    }

    /**
     * 强制终止活动实例,并强制完成活动任务
     *
     * @param instanceId  流程实例ID
     * @param flowCreator 处理人员
     */
    @Override
    public void terminate(Long instanceId, FlowCreator flowCreator) {
        this.forceComplete(instanceId, flowCreator, InstanceState.terminate, EventType.terminate);
    }

    /**
     * 强制完成流程实例
     *
     * @param instanceId    流程实例ID
     * @param flowCreator   处理人员
     * @param instanceState 流程实例最终状态
     * @param eventType     监听事件类型
     */
    protected void forceComplete(Long instanceId, FlowCreator flowCreator,
                                 InstanceState instanceState, EventType eventType) {
        FlwInstance flwInstance = instanceDao.selectById(instanceId);
        if (null == flwInstance) {
            return;
        }

        final Long parentInstanceId = flwInstance.getParentInstanceId();
        if (null != parentInstanceId) {
            // 找到主流程去执行完成逻辑
            this.forceComplete(parentInstanceId, flowCreator, instanceState, eventType);
        } else {
            // 结束所有子流程实例
            instanceDao.selectListByParentInstanceId(flwInstance.getId()).ifPresent(f -> f.forEach(t ->
                    this.forceCompleteAll(t, flowCreator, instanceState, eventType)));
        }

        // 结束当前流程实例
        this.forceCompleteAll(flwInstance, flowCreator, instanceState, eventType);
    }

    /**
     * 强制完成流程所有实例
     */
    protected void forceCompleteAll(FlwInstance flwInstance, FlowCreator flowCreator,
                                    InstanceState instanceState, EventType eventType) {

        // 实例相关任务强制完成
        taskService.forceCompleteAllTask(flwInstance.getId(), flowCreator, instanceState, eventType);

        // 更新历史实例设置状态为终止
        FlwHisInstance flwHisInstance = FlwHisInstance.of(flwInstance, instanceState);
        hisInstanceDao.updateById(flwHisInstance);

        // 删除实例
        instanceDao.deleteById(flwInstance.getId());

        // 流程实例监听器通知
        this.instanceNotify(eventType, () -> flwHisInstance, flowCreator);
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
    public boolean updateInstanceModelById(Long id, ProcessModel processModel) {
        // 使缓存失效
        FlowLongContext.invalidateProcessModel(FlowConstants.processInstanceCacheKey + id);

        // 更新流程实例模型
        FlwExtInstance extInstance = new FlwExtInstance();
        extInstance.setId(id);
        extInstance.setModelContent(FlowLongContext.toJson(processModel));
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
        List<FlwHisInstance> flwHisInstances = hisInstanceDao.selectListByProcessId(processId);
        if (ObjectUtils.isNotEmpty(flwHisInstances)) {
            // 删除活动任务相关信息
            taskService.cascadeRemoveByInstanceIds(flwHisInstances.stream().map(FlowEntity::getId).collect(Collectors.toList()));

            // 删除扩展实例
            extInstanceDao.deleteByProcessId(processId);

            // 删除历史实例
            hisInstanceDao.deleteByProcessId(processId);

            // 删除实例
            instanceDao.deleteByProcessId(processId);
        }
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
        if (null != selectNode.getConditionNodes()) {
            // 如果直接跟着条件节点，找到分支作为父节点
            for (ConditionNode conditionNode : selectNode.getConditionNodes()) {
                NodeModel conditionChildNode = conditionNode.getChildNode();
                if (Objects.equals(conditionChildNode.getNodeKey(), appendTaskKey)) {
                    nodeModel.setChildNode(conditionChildNode);
                    conditionNode.setChildNode(nodeModel);
                    break;
                }
            }
        } else {
            // 当前节点即为真实父节点
            nodeModel.setChildNode(selectNode.getChildNode());
            selectNode.setChildNode(nodeModel);
        }

        // 清理父节点关系
        processModel.cleanParentNode(processModel.getNodeConfig());

        // 更新最新模型
        FlwExtInstance temp = new FlwExtInstance();
        temp.setId(flwExtInstance.getId());
        temp.setModelContent(FlowLongContext.toJson(processModel));
        Assert.isFalse(extInstanceDao.updateById(temp), "Update FlwExtInstance Failed");

        // 使缓存失效
        FlowLongContext.invalidateProcessModel(flwExtInstance.modelCacheKey());
    }
}
