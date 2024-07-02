/*
 * Copyright 2023-2025 Licensed under the AGPL License
 */
package com.aizuda.bpm.mybatisplus.impl;

import com.aizuda.bpm.engine.dao.FlwTaskDao;
import com.aizuda.bpm.engine.entity.FlwTask;
import com.aizuda.bpm.mybatisplus.mapper.FlwTaskMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;

import java.util.Date;
import java.util.List;

/**
 * 任务数据访问层接口实现类
 *
 * <p>
 * 尊重知识产权，不允许非法使用，后果自负，不允许非法使用，后果自负
 * </p>
 *
 * @author hubin
 * @since 1.0
 */
public class FlwTaskDaoImpl implements FlwTaskDao {
    private final FlwTaskMapper taskMapper;

    public FlwTaskDaoImpl(FlwTaskMapper taskMapper) {
        this.taskMapper = taskMapper;
    }

    @Override
    public boolean insert(FlwTask flwTask) {
        return taskMapper.insert(flwTask) > 0;
    }

    @Override
    public boolean deleteById(Long id) {
        return taskMapper.deleteById(id) > 0;
    }

    @Override
    public boolean deleteByInstanceIds(List<Long> instanceIds) {
        return taskMapper.delete(Wrappers.<FlwTask>lambdaQuery().in(FlwTask::getInstanceId, instanceIds)) > 0;
    }

    @Override
    public boolean deleteBatchIds(List<Long> ids) {
        return taskMapper.deleteBatchIds(ids) > 0;
    }

    @Override
    public boolean updateById(FlwTask flwTask) {
        return taskMapper.updateById(flwTask) > 0;
    }

    @Override
    public FlwTask selectById(Long id) {
        return taskMapper.selectById(id);
    }

    @Override
    public Long selectCountByParentTaskId(Long parentTaskId) {
        return taskMapper.selectCount(Wrappers.<FlwTask>lambdaQuery()
                .eq(FlwTask::getParentTaskId, parentTaskId));
    }

    @Override
    public List<FlwTask> selectListByInstanceId(Long instanceId) {
        return taskMapper.selectList(Wrappers.<FlwTask>lambdaQuery()
                .eq(FlwTask::getInstanceId, instanceId));
    }

    @Override
    public List<FlwTask> selectListByInstanceIdAndTaskName(Long instanceId, String taskName) {
        return taskMapper.selectList(Wrappers.<FlwTask>lambdaQuery()
                .eq(FlwTask::getInstanceId, instanceId)
                .eq(FlwTask::getTaskName, taskName));
    }

    @Override
    public List<FlwTask> selectListByInstanceIdAndTaskNames(Long instanceId, List<String> taskNames) {
        return taskMapper.selectList(Wrappers.<FlwTask>lambdaQuery()
                .eq(FlwTask::getInstanceId, instanceId)
                .in(FlwTask::getTaskName, taskNames));
    }

    @Override
    public List<FlwTask> selectListTimeoutOrRemindTasks(Date currentDate) {
        return taskMapper.selectList(Wrappers.<FlwTask>lambdaQuery()
                .le(FlwTask::getExpireTime, currentDate)
                .or().le(FlwTask::getRemindTime, currentDate));
    }

    @Override
    public List<FlwTask> selectListByParentTaskId(Long parentTaskId) {
        return taskMapper.selectList(Wrappers.<FlwTask>lambdaQuery()
                .eq(FlwTask::getParentTaskId, parentTaskId));
    }

    @Override
    public List<FlwTask> selectListByParentTaskIds(List<Long> parentTaskIds) {
        return taskMapper.selectList(Wrappers.<FlwTask>lambdaQuery()
                .in(FlwTask::getParentTaskId, parentTaskIds));
    }
}
