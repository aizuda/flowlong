/* 
 * Copyright 2023-2025 Licensed under the AGPL License
 */
package com.aizuda.bpm.mybatisplus.service;

import com.aizuda.bpm.engine.QueryService;
import com.aizuda.bpm.engine.entity.FlwHisInstance;
import com.aizuda.bpm.engine.entity.FlwHisTask;
import com.aizuda.bpm.engine.entity.FlwHisTaskActor;
import com.aizuda.bpm.engine.entity.FlwInstance;
import com.aizuda.bpm.engine.entity.FlwTask;
import com.aizuda.bpm.engine.entity.FlwTaskActor;
import com.aizuda.bpm.mybatisplus.mapper.FlwHisInstanceMapper;
import com.aizuda.bpm.mybatisplus.mapper.FlwHisTaskActorMapper;
import com.aizuda.bpm.mybatisplus.mapper.FlwHisTaskMapper;
import com.aizuda.bpm.mybatisplus.mapper.FlwInstanceMapper;
import com.aizuda.bpm.mybatisplus.mapper.FlwTaskActorMapper;
import com.aizuda.bpm.mybatisplus.mapper.FlwTaskMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;

import java.util.List;
import java.util.Optional;

/**
 * 查询服务实现类
 *
 * <p>
 * 尊重知识产权，不允许非法使用，后果自负，不允许非法使用，后果自负
 * </p>
 *
 * @author hubin
 * @since 1.0
 */
public class QueryServiceImpl implements QueryService {
    private final FlwInstanceMapper instanceMapper;
    private final FlwHisInstanceMapper hisInstanceMapper;
    private final FlwTaskMapper taskMapper;
    private final FlwTaskActorMapper taskActorMapper;
    private final FlwHisTaskMapper hisTaskMapper;
    private final FlwHisTaskActorMapper hisTaskActorMapper;

    public QueryServiceImpl(FlwInstanceMapper instanceMapper, FlwHisInstanceMapper hisInstanceMapper,
                            FlwTaskMapper taskMapper, FlwTaskActorMapper taskActorMapper,
                            FlwHisTaskMapper hisTaskMapper, FlwHisTaskActorMapper hisTaskActorMapper) {
        this.instanceMapper = instanceMapper;
        this.hisInstanceMapper = hisInstanceMapper;
        this.taskMapper = taskMapper;
        this.taskActorMapper = taskActorMapper;
        this.hisTaskMapper = hisTaskMapper;
        this.hisTaskActorMapper = hisTaskActorMapper;
    }

    @Override
    public FlwInstance getInstance(Long instanceId) {
        return instanceMapper.selectById(instanceId);
    }

    @Override
    public FlwTask getTask(Long taskId) {
        return taskMapper.selectById(taskId);
    }

    @Override
    public FlwHisInstance getHistInstance(Long instanceId) {
        return hisInstanceMapper.selectById(instanceId);
    }

    @Override
    public FlwHisTask getHistTask(Long taskId) {
        return hisTaskMapper.selectById(taskId);
    }

    @Override
    public Optional<List<FlwHisTask>> getHisTasksByName(Long instanceId, String taskName) {
        return Optional.ofNullable(hisTaskMapper.selectList(Wrappers.<FlwHisTask>lambdaQuery()
                .eq(FlwHisTask::getInstanceId, instanceId)
                .eq(FlwHisTask::getTaskName, taskName)
                .orderByDesc(FlwHisTask::getCreateTime)));
    }

    @Override
    public List<FlwTask> getTasksByInstanceId(Long instanceId) {
        return taskMapper.selectList(Wrappers.<FlwTask>lambdaQuery().eq(FlwTask::getInstanceId, instanceId));
    }

    @Override
    public List<FlwTask> getTasksByInstanceIdAndTaskName(Long instanceId, String taskName) {
        return taskMapper.selectList(Wrappers.<FlwTask>lambdaQuery().eq(FlwTask::getInstanceId, instanceId).eq(FlwTask::getTaskName, taskName));
    }

    @Override
    public Optional<List<FlwTask>> getActiveTasksByInstanceId(Long instanceId) {
        return Optional.ofNullable(taskMapper.selectList(Wrappers.<FlwTask>lambdaQuery().eq(FlwTask::getInstanceId, instanceId)));
    }

    @Override
    public Optional<List<FlwTaskActor>> getActiveTaskActorsByInstanceId(Long instanceId) {
        return Optional.ofNullable(taskActorMapper.selectList(Wrappers.<FlwTaskActor>lambdaQuery().eq(FlwTaskActor::getInstanceId, instanceId)));
    }

    @Override
    public List<FlwTaskActor> getTaskActorsByTaskId(Long taskId) {
        return taskActorMapper.selectList(Wrappers.<FlwTaskActor>lambdaQuery().eq(FlwTaskActor::getTaskId, taskId));
    }

    @Override
    public List<FlwHisTaskActor> getHistoryTaskActorsByTaskId(Long taskId) {
        return hisTaskActorMapper.selectList(Wrappers.<FlwHisTaskActor>lambdaQuery().eq(FlwHisTaskActor::getTaskId, taskId));
    }

    @Override
    public List<FlwTask> getActiveTasks(Long instanceId, List<String> taskNames) {
        return taskMapper.selectList(Wrappers.<FlwTask>lambdaQuery()
                .eq(FlwTask::getInstanceId, instanceId)
                .in(FlwTask::getTaskName, taskNames));
    }

    @Override
    public Optional<List<FlwHisTask>> getHisTasksByInstanceId(Long instanceId) {
        return Optional.ofNullable(hisTaskMapper.selectList(Wrappers.<FlwHisTask>lambdaQuery()
                .eq(FlwHisTask::getInstanceId, instanceId)
                .orderByDesc(FlwHisTask::getFinishTime)
                .orderByDesc(FlwHisTask::getId)));
    }

}
