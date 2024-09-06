/*
 * Copyright 2023-2025 Licensed under the AGPL License
 */
package com.aizuda.bpm.mybatisplus.impl;

import com.aizuda.bpm.engine.dao.FlwInstanceDao;
import com.aizuda.bpm.engine.entity.FlwInstance;
import com.aizuda.bpm.mybatisplus.mapper.FlwInstanceMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;

import java.util.List;
import java.util.Optional;

/**
 * 流程实例数据访问层接口实现类
 *
 * <p>
 * 尊重知识产权，不允许非法使用，后果自负，不允许非法使用，后果自负
 * </p>
 *
 * @author hubin
 * @since 1.0
 */
public class FlwInstanceDaoImpl implements FlwInstanceDao {
    private final FlwInstanceMapper instanceMapper;

    public FlwInstanceDaoImpl(FlwInstanceMapper instanceMapper) {
        this.instanceMapper = instanceMapper;
    }

    @Override
    public boolean insert(FlwInstance instance) {
        return instanceMapper.insert(instance) > 0;
    }

    @Override
    public boolean deleteById(Long id) {
        return instanceMapper.deleteById(id) > 0;
    }

    @Override
    public boolean deleteByProcessId(Long processId) {
        return instanceMapper.delete(Wrappers.<FlwInstance>lambdaQuery()
                .eq(FlwInstance::getProcessId, processId)) > 0;
    }

    @Override
    public boolean updateById(FlwInstance instance) {
        return instanceMapper.updateById(instance) > 0;
    }

    @Override
    public Long selectCountByParentInstanceId(Long parentInstanceId) {
        return instanceMapper.selectCount(Wrappers.<FlwInstance>lambdaQuery().eq(FlwInstance::getParentInstanceId, parentInstanceId));
    }

    @Override
    public FlwInstance selectById(Long id) {
        return instanceMapper.selectById(id);
    }

    @Override
    public Optional<List<FlwInstance>> selectListByParentInstanceId(Long parentInstanceId) {
        return Optional.ofNullable(instanceMapper.selectList(Wrappers.<FlwInstance>lambdaQuery()
                .eq(FlwInstance::getParentInstanceId, parentInstanceId)));
    }
}
