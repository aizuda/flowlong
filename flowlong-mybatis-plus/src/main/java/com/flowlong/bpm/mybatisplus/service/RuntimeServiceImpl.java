/*
 * Copyright 2023-2025 Licensed under the AGPL License
 */
package com.flowlong.bpm.mybatisplus.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.flowlong.bpm.engine.QueryService;
import com.flowlong.bpm.engine.RuntimeService;
import com.flowlong.bpm.engine.TaskService;
import com.flowlong.bpm.engine.assist.Assert;
import com.flowlong.bpm.engine.assist.DateUtils;
import com.flowlong.bpm.engine.assist.ObjectUtils;
import com.flowlong.bpm.engine.core.Execution;
import com.flowlong.bpm.engine.core.FlowCreator;
import com.flowlong.bpm.engine.core.enums.EventType;
import com.flowlong.bpm.engine.core.enums.InstanceState;
import com.flowlong.bpm.engine.core.enums.TaskState;
import com.flowlong.bpm.engine.entity.*;
import com.flowlong.bpm.engine.listener.InstanceListener;
import com.flowlong.bpm.mybatisplus.mapper.FlwHisInstanceMapper;
import com.flowlong.bpm.mybatisplus.mapper.FlwInstanceMapper;

import java.util.List;
import java.util.Map;
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
    private final FlwInstanceMapper instanceMapper;
    private final FlwHisInstanceMapper hisInstanceMapper;

    public RuntimeServiceImpl(InstanceListener instanceListener,
                              QueryService queryService, TaskService taskService, FlwInstanceMapper instanceMapper,
                              FlwHisInstanceMapper hisInstanceMapper) {
        this.instanceListener = instanceListener;
        this.queryService = queryService;
        this.taskService = taskService;
        this.instanceMapper = instanceMapper;
        this.hisInstanceMapper = hisInstanceMapper;
    }

    /**
     * 创建活动实例
     */
    @Override
    public FlwInstance createInstance(FlwProcess process, FlowCreator flowCreator, Map<String, Object> args, String currentNode, Supplier<FlwInstance> supplier) {
        FlwInstance flwInstance = null;
        if (null != supplier) {
            flwInstance = supplier.get();
        }
        if (null == flwInstance) {
            flwInstance = new FlwInstance();
        }
        flwInstance.setCreateTime(DateUtils.getCurrentDate());
        flwInstance.setFlowCreator(flowCreator);
        flwInstance.setCurrentNode(currentNode);
        flwInstance.setLastUpdateBy(flwInstance.getCreateBy());
        flwInstance.setLastUpdateTime(flwInstance.getCreateTime());
        flwInstance.setProcessId(process.getId());
        flwInstance.setVariable(args);
        this.saveInstance(flwInstance);
        return flwInstance;
    }

    /**
     * 向活动实例临时添加全局变量数据
     *
     * @param instanceId 实例id
     * @param args       变量数据
     */
    @Override
    public void addVariable(Long instanceId, Map<String, Object> args) {
        FlwInstance flwInstance = instanceMapper.selectById(instanceId);
        Map<String, Object> data = flwInstance.getVariableMap();
        data.putAll(args);
        FlwInstance temp = new FlwInstance();
        temp.setId(instanceId);
        temp.setVariable(data);
        instanceMapper.updateById(temp);
    }


    /**
     * 删除活动流程实例数据，更新历史流程实例的状态、结束时间
     */
    @Override
    public boolean complete(Execution execution, Long instanceId) {
        FlwInstance flwInstance = instanceMapper.selectById(instanceId);
        if (null != flwInstance) {
            FlwHisInstance his = new FlwHisInstance();
            his.setId(instanceId);
            InstanceState instanceState = InstanceState.complete;
            his.setInstanceState(instanceState);
            his.setCurrentNode(instanceState.name());
            his.setCreateTime(flwInstance.getCreateTime());
            his.setLastUpdateBy(flwInstance.getLastUpdateBy());
            his.setLastUpdateTime(flwInstance.getLastUpdateTime());
            his.calculateDuration();
            instanceMapper.deleteById(instanceId);
            hisInstanceMapper.updateById(his);
            // 流程实例监听器通知
            this.instanceNotify(EventType.complete, () -> hisInstanceMapper.selectById(instanceId));

            /*
             * 实例为子流程，重启动父流程任务
             */
            if (null != flwInstance.getParentInstanceId()) {
                // 重启父流程实例
                FlwInstance parentFlwInstance = instanceMapper.selectById(flwInstance.getParentInstanceId());
                execution.setFlwInstance(parentFlwInstance);
                execution.restartProcessInstance(parentFlwInstance.getProcessId(), parentFlwInstance.getCurrentNode());

                // 结束调用外部流程任务
                taskService.endCallProcessTask(flwInstance.getProcessId(), flwInstance.getId());
            }
        }
        return true;
    }

    protected void instanceNotify(EventType eventType, Supplier<FlwHisInstance> supplier) {
        if (null != instanceListener) {
            instanceListener.notify(eventType, supplier);
        }
    }

    /**
     * 流程实例数据会保存至活动实例表、历史实例表
     *
     * @param flwInstance 流程实例对象
     */
    @Override
    public void saveInstance(FlwInstance flwInstance) {
        // 保存实例
        instanceMapper.insert(flwInstance);

        // 保存历史实例设置为活的状态
        FlwHisInstance flwHisInstance = FlwHisInstance.of(flwInstance, InstanceState.active);
        hisInstanceMapper.insert(flwHisInstance);

        // 流程实例监听器通知
        this.instanceNotify(EventType.create, () -> flwHisInstance);
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
        FlwInstance flwInstance = instanceMapper.selectById(instanceId);
        if (null != flwInstance) {
            // 实例相关任务强制完成
            queryService.getActiveTasksByInstanceId(instanceId).ifPresent(tasks -> {
                for (FlwTask flwTask : tasks) {
                    taskService.executeTask(flwTask.getId(), flowCreator, null,
                            TaskState.of(instanceState), eventType);
                }
            });

            // 更新历史实例设置状态为终止
            FlwHisInstance flwHisInstance = FlwHisInstance.of(flwInstance, instanceState);
            hisInstanceMapper.updateById(flwHisInstance);

            // 删除实例
            instanceMapper.deleteById(instanceId);

            // 流程实例监听器通知
            this.instanceNotify(eventType, () -> flwHisInstance);
        }
    }

    /**
     * 更新活动实例
     */
    @Override
    public void updateInstance(FlwInstance flwInstance) {
        Assert.illegal(null == flwInstance || null == flwInstance.getId(),
                "instance id cannot be empty");
        instanceMapper.updateById(flwInstance);
    }

    /**
     * 级联删除指定流程实例的所有数据
     *
     * @param processId 流程ID
     */
    @Override
    public void cascadeRemoveByProcessId(Long processId) {
        List<FlwHisInstance> flwHisInstances = hisInstanceMapper.selectList(Wrappers.<FlwHisInstance>lambdaQuery()
                .eq(FlwHisInstance::getProcessId, processId));
        if (ObjectUtils.isNotEmpty(flwHisInstances)) {
            // 删除活动任务相关信息
            taskService.cascadeRemoveByInstanceIds(flwHisInstances.stream().map(FlowEntity::getId).collect(Collectors.toList()));

            // 删除历史实例
            hisInstanceMapper.delete(Wrappers.<FlwHisInstance>lambdaQuery().eq(FlwHisInstance::getProcessId, processId));

            // 删除实例
            instanceMapper.delete(Wrappers.<FlwInstance>lambdaQuery().eq(FlwInstance::getProcessId, processId));
        }
    }

}
