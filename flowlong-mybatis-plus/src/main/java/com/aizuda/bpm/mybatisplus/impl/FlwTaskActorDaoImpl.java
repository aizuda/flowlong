/*
 * Copyright 2023-2025 Licensed under the AGPL License
 */
package com.aizuda.bpm.mybatisplus.impl;

import com.aizuda.bpm.engine.dao.FlwTaskActorDao;
import com.aizuda.bpm.engine.entity.FlwTaskActor;
import com.aizuda.bpm.mybatisplus.mapper.FlwTaskActorMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;

import java.util.List;

/**
 * 任务参与者数据访问层接口实现类
 *
 * <p>
 * 尊重知识产权，不允许非法使用，后果自负，不允许非法使用，后果自负
 * </p>
 *
 * @author hubin
 * @since 1.0
 */
public class FlwTaskActorDaoImpl implements FlwTaskActorDao {
    private final FlwTaskActorMapper taskActorMapper;

    public FlwTaskActorDaoImpl(FlwTaskActorMapper taskActorMapper) {
        this.taskActorMapper = taskActorMapper;
    }

    @Override
    public boolean insert(FlwTaskActor taskActor) {
        return taskActorMapper.insert(taskActor) > 0;
    }

    @Override
    public boolean deleteById(Long id) {
        return taskActorMapper.deleteById(id) > 0;
    }

    @Override
    public boolean deleteBatchIds(List<Long> ids) {
        return taskActorMapper.deleteBatchIds(ids) > 0;
    }

    @Override
    public boolean deleteByTaskId(Long taskId) {
        return taskActorMapper.delete(Wrappers.<FlwTaskActor>lambdaQuery()
                .eq(FlwTaskActor::getTaskId, taskId)) > 0;
    }

    @Override
    public boolean deleteByInstanceIds(List<Long> instanceIds) {
        return taskActorMapper.delete(Wrappers.<FlwTaskActor>lambdaQuery()
                .in(FlwTaskActor::getInstanceId, instanceIds)) > 0;
    }

    @Override
    public boolean deleteByTaskIdAndWeight(Long taskId, int weight) {
        return taskActorMapper.delete(Wrappers.<FlwTaskActor>lambdaQuery()
                .eq(FlwTaskActor::getTaskId, taskId)
                .eq(FlwTaskActor::getWeight, weight)) > 0;
    }

    @Override
    public boolean deleteByTaskIdAndActorIds(Long taskId, List<String> actorIds) {
        return taskActorMapper.delete(Wrappers.<FlwTaskActor>lambdaQuery()
                .eq(FlwTaskActor::getTaskId, taskId)
                .in(FlwTaskActor::getActorId, actorIds)) > 0;
    }

    @Override
    public boolean updateById(FlwTaskActor taskActor) {
        return taskActorMapper.updateById(taskActor) > 0;
    }

    @Override
    public List<FlwTaskActor> selectListByInstanceId(Long instanceId) {
        return taskActorMapper.selectList(Wrappers.<FlwTaskActor>lambdaQuery()
                .eq(FlwTaskActor::getInstanceId, instanceId));
    }

    @Override
    public List<FlwTaskActor> selectListByTaskId(Long taskId) {
        return taskActorMapper.selectList(Wrappers.<FlwTaskActor>lambdaQuery()
                .eq(FlwTaskActor::getTaskId, taskId));
    }

    @Override
    public List<FlwTaskActor> selectListByTaskIds(List<Long> taskIds) {
        return taskActorMapper.selectList(Wrappers.<FlwTaskActor>lambdaQuery()
                .in(FlwTaskActor::getTaskId, taskIds));
    }

    @Override
    public List<FlwTaskActor> selectListByTaskIdAndActorId(Long taskId, String actorId) {
        return taskActorMapper.selectList(Wrappers.<FlwTaskActor>lambdaQuery()
                .eq(FlwTaskActor::getTaskId, taskId)
                .eq(FlwTaskActor::getActorId, actorId));
    }

    @Override
    public Long selectCountByTaskIdAndActorId(Long taskId, String actorId) {
        return taskActorMapper.selectCount(Wrappers.<FlwTaskActor>lambdaQuery()
                .eq(FlwTaskActor::getTaskId, taskId)
                .eq(FlwTaskActor::getActorId, actorId));
    }
}
