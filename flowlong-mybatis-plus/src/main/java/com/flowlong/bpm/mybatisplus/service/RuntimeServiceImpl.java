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
import com.flowlong.bpm.engine.core.FlowCreator;
import com.flowlong.bpm.engine.core.enums.EventType;
import com.flowlong.bpm.engine.core.enums.InstanceState;
import com.flowlong.bpm.engine.core.enums.TaskState;
import com.flowlong.bpm.engine.entity.FlwHisInstance;
import com.flowlong.bpm.engine.entity.FlwInstance;
import com.flowlong.bpm.engine.entity.FlwProcess;
import com.flowlong.bpm.engine.entity.FlwTask;
import com.flowlong.bpm.engine.listener.InstanceListener;
import com.flowlong.bpm.engine.model.ProcessModel;
import com.flowlong.bpm.mybatisplus.mapper.FlwHisInstanceMapper;
import com.flowlong.bpm.mybatisplus.mapper.FlwInstanceMapper;

import java.util.List;
import java.util.Map;

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
    private InstanceListener instanceListener;
    private QueryService queryService;
    private TaskService taskService;
    private FlwInstanceMapper instanceMapper;
    private FlwHisInstanceMapper hisInstanceMapper;

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
    public FlwInstance createInstance(FlwProcess process, FlowCreator flowCreator, Map<String, Object> args) {
        FlwInstance flwInstance = new FlwInstance();
        flwInstance.setCreateTime(DateUtils.getCurrentDate());
        flwInstance.setLastUpdateTime(flwInstance.getCreateTime());
        flwInstance.setCreateId(flowCreator.getCreateId());
        flwInstance.setCreateBy(flowCreator.getCreateBy());
        flwInstance.setLastUpdateBy(flwInstance.getCreateBy());
        flwInstance.setProcessId(process.getId());
        ProcessModel model = process.model();
        if (model != null && args != null) {
//            if (ObjectUtils.isNotEmpty(model.getExpireTime())) {
//                instance.setExpireTime(new Date(model.getExpireTime()));
//            }
        }

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
    public void complete(Long instanceId) {
        FlwHisInstance flwHisInstance = new FlwHisInstance();
        flwHisInstance.setId(instanceId);
        flwHisInstance.setInstanceState(InstanceState.complete);
        flwHisInstance.setEndTime(DateUtils.getCurrentDate());
        instanceMapper.deleteById(instanceId);
        hisInstanceMapper.updateById(flwHisInstance);
        // 流程实例监听器通知
        this.instanceNotify(EventType.complete, flwHisInstance);
    }

    protected void instanceNotify(EventType eventType, FlwHisInstance flwHisInstance) {
        if (null != instanceListener) {
            instanceListener.notify(eventType, flwHisInstance);
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
        this.instanceNotify(EventType.create, flwHisInstance);
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
    public void timeout(Long instanceId) {
        this.forceComplete(instanceId, FlowCreator.ADMIN, InstanceState.timeout, EventType.timeout);
    }

    /**
     * 强制终止活动实例,并强制完成活动任务
     *
     * @param instanceId  流程实例ID
     * @param flowCreator 处理人员
     */
    @Override
    public void terminate(Long instanceId, FlowCreator flowCreator) {
        this.forceComplete(instanceId, flowCreator, InstanceState.complete, EventType.complete);
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
            flwHisInstance.setEndTime(DateUtils.getCurrentDate());
            hisInstanceMapper.updateById(flwHisInstance);

            // 删除实例
            instanceMapper.deleteById(instanceId);

            // 流程实例监听器通知
            this.instanceNotify(eventType, flwHisInstance);
        }
    }

    /**
     * 更新活动实例
     */
    @Override
    public void updateInstance(FlwInstance flwInstance) {
        Assert.illegalArgument(null == flwInstance || null == flwInstance.getId(),
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
            flwHisInstances.forEach(t -> {
                // 删除活动任务相关信息
                taskService.cascadeRemoveByInstanceId(t.getId());
                // 删除抄送任务
                // ccInstanceMapper.delete(Wrappers.<CCInstance>lambdaQuery().eq(CCInstance::getInstanceId, t.getId()));

                // 删除代理任务

            });
        }

        // 删除历史实例
        hisInstanceMapper.delete(Wrappers.<FlwHisInstance>lambdaQuery().eq(FlwHisInstance::getProcessId, processId));

        // 删除实例
        instanceMapper.delete(Wrappers.<FlwInstance>lambdaQuery().eq(FlwInstance::getProcessId, processId));
    }

}
