/*
 * Copyright 2023-2025 Licensed under the Dual Licensing
 * website: https://aizuda.com
 */
package com.aizuda.bpm.engine.impl;

import com.aizuda.bpm.engine.QueryService;
import com.aizuda.bpm.engine.dao.*;
import com.aizuda.bpm.engine.entity.*;

import java.util.List;
import java.util.Optional;

/**
 * 查询服务实现类
 *
 * <p>
 * <a href="https://aizuda.com">官网</a>尊重知识产权，不允许非法使用，后果自负，不允许非法使用，后果自负
 * </p>
 *
 * @author hubin
 * @since 1.0
 */
public class QueryServiceImpl implements QueryService {
    private final FlwInstanceDao instanceDao;
    private final FlwHisInstanceDao hisInstanceDao;
    private final FlwExtInstanceDao extInstanceDao;
    private final FlwTaskDao taskDao;
    private final FlwTaskActorDao taskActorDao;
    private final FlwHisTaskDao hisTaskDao;
    private final FlwHisTaskActorDao hisTaskActorDao;

    public QueryServiceImpl(FlwInstanceDao instanceDao, FlwHisInstanceDao hisInstanceDao,
                            FlwExtInstanceDao extInstanceDao, FlwTaskDao taskDao, FlwTaskActorDao taskActorDao,
                            FlwHisTaskDao hisTaskDao, FlwHisTaskActorDao hisTaskActorDao) {
        this.instanceDao = instanceDao;
        this.hisInstanceDao = hisInstanceDao;
        this.extInstanceDao = extInstanceDao;
        this.taskDao = taskDao;
        this.taskActorDao = taskActorDao;
        this.hisTaskDao = hisTaskDao;
        this.hisTaskActorDao = hisTaskActorDao;
    }

    @Override
    public FlwInstance getInstance(Long instanceId) {
        return instanceDao.selectById(instanceId);
    }

    @Override
    public FlwTask getTask(Long taskId) {
        return taskDao.selectById(taskId);
    }

    @Override
    public FlwHisInstance getHistInstance(Long instanceId) {
        return hisInstanceDao.selectById(instanceId);
    }

    @Override
    public FlwExtInstance getExtInstance(Long instanceId) {
        return extInstanceDao.selectById(instanceId);
    }

    @Override
    public boolean existActiveSubProcess(Long instanceId) {
        return instanceDao.selectCountByParentInstanceId(instanceId) > 0;
    }

    @Override
    public boolean existActiveTask(Long instanceId) {
        return taskDao.selectCountByInstanceId(instanceId) > 0;
    }

    @Override
    public FlwHisTask getHistTask(Long taskId) {
        return hisTaskDao.selectById(taskId);
    }

    @Override
    public Optional<List<FlwHisTask>> getHisTasksByName(Long instanceId, String taskName) {
        return Optional.ofNullable(hisTaskDao.selectListByInstanceIdAndTaskName(instanceId, taskName));
    }

    @Override
    public List<FlwTask> getTasksByInstanceId(Long instanceId) {
        return taskDao.selectListByInstanceId(instanceId);
    }

    @Override
    public List<FlwTask> getTasksByInstanceIdAndTaskName(Long instanceId, String taskName) {
        return taskDao.selectListByInstanceIdAndTaskName(instanceId, taskName);
    }
    @Override
    public List<FlwTask> getTasksByInstanceIdAndTaskKey(Long instanceId, String taskKey) {
        return taskDao.selectListByInstanceIdAndTaskKey(instanceId, taskKey);
    }

    @Override
    public Optional<List<FlwTaskActor>> getActiveTaskActorsByInstanceId(Long instanceId) {
        return Optional.ofNullable(taskActorDao.selectListByInstanceId(instanceId));
    }

    @Override
    public List<FlwTaskActor> getTaskActorsByTaskId(Long taskId) {
        return taskActorDao.selectListByTaskId(taskId);
    }

    @Override
    public List<FlwTaskActor> getTaskActorsByTaskIdAndActorId(Long taskId, String actorId) {
        return taskActorDao.selectListByTaskIdAndActorId(taskId, actorId);
    }

    @Override
    public List<FlwHisTaskActor> getHisTaskActorsByTaskId(Long taskId) {
        return hisTaskActorDao.selectListByTaskId(taskId);
    }

    @Override
    public List<FlwHisTaskActor> getHisTaskActorsByTaskIdAndActorId(Long taskId, String actorId) {
        return hisTaskActorDao.selectListByTaskIdAndActorId(taskId, actorId);
    }

    @Override
    public List<FlwTask> getActiveTasks(Long instanceId, List<String> taskNames) {
        return taskDao.selectListByInstanceIdAndTaskNames(instanceId, taskNames);
    }

    @Override
    public Optional<List<FlwHisTask>> getHisTasksByInstanceId(Long instanceId) {
        return hisTaskDao.selectListByInstanceId(instanceId);
    }

    @Override
    public Optional<List<FlwInstance>> getSubProcessByInstanceId(Long instanceId) {
        return instanceDao.selectListByParentInstanceId(instanceId);
    }

    @Override
    public Optional<List<FlwInstance>> getInstancesByBusinessKey(String businessKey) {
        return instanceDao.selectListByBusinessKey(businessKey);
    }

    @Override
    public Optional<List<FlwHisInstance>> getHisInstancesByBusinessKey(String businessKey) {
        return hisInstanceDao.selectListByBusinessKey(businessKey);
    }
}
