/*
 * Copyright 2023-2025 Licensed under the Dual Licensing
 * website: https://aizuda.com
 */
package com.aizuda.bpm.mybatisplus.impl;

import com.aizuda.bpm.engine.dao.FlwHisTaskDao;
import com.aizuda.bpm.engine.entity.FlwHisTask;
import com.aizuda.bpm.mybatisplus.mapper.FlwHisTaskMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * 历史任务数据访问层接口实现类
 *
 * <p>
 * <a href="https://aizuda.com">官网</a>尊重知识产权，不允许非法使用，后果自负，不允许非法使用，后果自负
 * </p>
 *
 * @author hubin
 * @since 1.0
 */
public class FlwHisTaskDaoImpl implements FlwHisTaskDao {
    private final FlwHisTaskMapper hisTaskMapper;

    public FlwHisTaskDaoImpl(FlwHisTaskMapper hisTaskMapper) {
        this.hisTaskMapper = hisTaskMapper;
    }

    @Override
    public boolean insert(FlwHisTask hisTask) {
        return hisTaskMapper.insert(hisTask) > 0;
    }

    @Override
    public boolean deleteById(Long id) {
        return hisTaskMapper.deleteById(id) > 0;
    }

    @Override
    public boolean deleteByInstanceIds(List<Long> instanceIds) {
        return hisTaskMapper.delete(Wrappers.<FlwHisTask>lambdaQuery()
                .in(FlwHisTask::getInstanceId, instanceIds)) > 0;
    }

    @Override
    public boolean updateById(FlwHisTask hisTask) {
        return hisTaskMapper.updateById(hisTask) > 0;
    }

    @Override
    public FlwHisTask selectById(Long id) {
        return hisTaskMapper.selectById(id);
    }

    @Override
    public List<FlwHisTask> selectListByInstanceIdAndTaskName(Long instanceId, String taskName) {
        return hisTaskMapper.selectList(Wrappers.<FlwHisTask>lambdaQuery()
                .eq(FlwHisTask::getInstanceId, instanceId)
                .eq(FlwHisTask::getTaskName, taskName));
    }

    @Override
    public Optional<List<FlwHisTask>> selectListByInstanceId(Long instanceId) {
        return Optional.ofNullable(hisTaskMapper.selectList(Wrappers.<FlwHisTask>lambdaQuery()
                .eq(FlwHisTask::getInstanceId, instanceId)));
    }

    @Override
    public List<FlwHisTask> selectListByCallProcessIdAndCallInstanceId(Long callProcessId, Long callInstanceId) {
        return hisTaskMapper.selectList(Wrappers.<FlwHisTask>lambdaQuery()
                .eq(FlwHisTask::getCallProcessId, callProcessId)
                .eq(FlwHisTask::getCallInstanceId, callInstanceId));
    }

    @Override
    public List<FlwHisTask> selectListByParentTaskId(Long parentTaskId) {
        return hisTaskMapper.selectList(Wrappers.<FlwHisTask>lambdaQuery()
                .eq(FlwHisTask::getParentTaskId, parentTaskId));
    }

    @Override
    public Collection<FlwHisTask> selectListByInstanceIdAndTaskNameAndParentTaskId(Long instanceId, String taskName, Long parentTaskId) {
        return hisTaskMapper.selectList(Wrappers.<FlwHisTask>lambdaQuery()
                .eq(FlwHisTask::getInstanceId, instanceId)
                .eq(FlwHisTask::getTaskName, taskName)
                .eq(FlwHisTask::getParentTaskId, parentTaskId));
    }
}
