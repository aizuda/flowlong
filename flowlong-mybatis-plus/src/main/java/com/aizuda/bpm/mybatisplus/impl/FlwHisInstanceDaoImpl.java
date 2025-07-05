/*
 * Copyright 2023-2025 Licensed under the Dual Licensing
 * website: https://aizuda.com
 */
package com.aizuda.bpm.mybatisplus.impl;

import com.aizuda.bpm.engine.dao.FlwHisInstanceDao;
import com.aizuda.bpm.engine.entity.FlwHisInstance;
import com.aizuda.bpm.mybatisplus.mapper.FlwHisInstanceMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;

import java.util.List;
import java.util.Optional;

/**
 * 历史流程实例数据访问层接口实现类
 *
 * <p>
 * <a href="https://aizuda.com">官网</a>尊重知识产权，不允许非法使用，后果自负，不允许非法使用，后果自负
 * </p>
 *
 * @author hubin
 * @since 1.0
 */
public class FlwHisInstanceDaoImpl implements FlwHisInstanceDao {
    private final FlwHisInstanceMapper hisInstanceMapper;

    public FlwHisInstanceDaoImpl(FlwHisInstanceMapper hisInstanceMapper) {
        this.hisInstanceMapper = hisInstanceMapper;
    }

    @Override
    public boolean insert(FlwHisInstance hisInstance) {
        return hisInstanceMapper.insert(hisInstance) > 0;
    }

    @Override
    public boolean deleteById(Long id) {
        return hisInstanceMapper.deleteById(id) > 0;
    }

    @Override
    public boolean deleteByProcessId(Long processId) {
        return hisInstanceMapper.delete(Wrappers.<FlwHisInstance>lambdaQuery()
                .eq(FlwHisInstance::getProcessId, processId)) > 0;
    }

    @Override
    public boolean updateById(FlwHisInstance hisInstance) {
        return hisInstanceMapper.updateById(hisInstance) > 0;
    }

    @Override
    public FlwHisInstance selectById(Long id) {
        return hisInstanceMapper.selectById(id);
    }

    private Optional<List<FlwHisInstance>> ofNullable(List<FlwHisInstance> hisInstances) {
        return Optional.ofNullable(null == hisInstances || hisInstances.isEmpty() ? null : hisInstances);
    }

    @Override
    public Optional<List<FlwHisInstance>> selectListByProcessId(Long processId) {
        return this.ofNullable(hisInstanceMapper.selectList(Wrappers.<FlwHisInstance>lambdaQuery()
                .eq(FlwHisInstance::getProcessId, processId)));
    }

    @Override
    public Optional<List<FlwHisInstance>> selectListByParentInstanceId(Long parentInstanceId) {
        return this.ofNullable(hisInstanceMapper.selectList(Wrappers.<FlwHisInstance>lambdaQuery()
                .eq(FlwHisInstance::getParentInstanceId, parentInstanceId)));
    }

    @Override
    public Optional<List<FlwHisInstance>> selectListByBusinessKey(String businessKey) {
        return this.ofNullable(hisInstanceMapper.selectList(Wrappers.<FlwHisInstance>lambdaQuery()
                .eq(FlwHisInstance::getBusinessKey, businessKey)));
    }
}
