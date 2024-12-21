/*
 * Copyright 2023-2025 Licensed under the Dual Licensing
 * website: https://aizuda.com
 */
package com.aizuda.bpm.mybatisplus.impl;

import com.aizuda.bpm.engine.dao.FlwHisTaskActorDao;
import com.aizuda.bpm.engine.entity.FlwHisTaskActor;
import com.aizuda.bpm.engine.entity.FlwTaskActor;
import com.aizuda.bpm.mybatisplus.mapper.FlwHisTaskActorMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;

import java.util.List;

/**
 * 历史任务参与者数据访问层接口实现类
 *
 * <p>
 * <a href="https://aizuda.com">官网</a>尊重知识产权，不允许非法使用，后果自负，不允许非法使用，后果自负
 * </p>
 *
 * @author hubin
 * @since 1.0
 */
public class FlwHisTaskActorDaoImpl implements FlwHisTaskActorDao {
    private final FlwHisTaskActorMapper hisTaskActorMapper;

    public FlwHisTaskActorDaoImpl(FlwHisTaskActorMapper hisTaskActorMapper) {
        this.hisTaskActorMapper = hisTaskActorMapper;
    }

    @Override
    public boolean insert(FlwHisTaskActor hisTaskActor) {
        return hisTaskActorMapper.insert(hisTaskActor) > 0;
    }

    @Override
    public boolean deleteByInstanceIds(List<Long> instanceIds) {
        return hisTaskActorMapper.delete(Wrappers.<FlwHisTaskActor>lambdaQuery()
                .in(FlwHisTaskActor::getInstanceId, instanceIds)) > 0;
    }

    @Override
    public boolean deleteByTaskId(Long taskId) {
        return hisTaskActorMapper.delete(Wrappers.<FlwHisTaskActor>lambdaQuery()
                .eq(FlwHisTaskActor::getTaskId, taskId)) > 0;
    }

    @Override
    public List<FlwHisTaskActor> selectListByTaskId(Long taskId) {
        return hisTaskActorMapper.selectList(Wrappers.<FlwHisTaskActor>lambdaQuery()
                .eq(FlwHisTaskActor::getTaskId, taskId));
    }

    @Override
    public List<FlwHisTaskActor> selectListByTaskIds(List<Long> taskIds) {
        return hisTaskActorMapper.selectList(Wrappers.<FlwHisTaskActor>lambdaQuery()
                .in(FlwHisTaskActor::getTaskId, taskIds));
    }

    @Override
    public List<FlwHisTaskActor> selectListByTaskIdAndActorId(Long taskId, String actorId) {
        return hisTaskActorMapper.selectList(Wrappers.<FlwHisTaskActor>lambdaQuery()
                .eq(FlwHisTaskActor::getTaskId, taskId)
                .eq(FlwTaskActor::getActorId, actorId));
    }
}
