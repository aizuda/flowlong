/*
 * Copyright 2023-2025 Licensed under the apache-2.0 License
 * website: https://aizuda.com
 */
package com.aizuda.bpm.mybatisplus.impl;

import com.aizuda.bpm.engine.dao.FlwExtInstanceDao;
import com.aizuda.bpm.engine.entity.FlwExtInstance;
import com.aizuda.bpm.mybatisplus.mapper.FlwExtInstanceMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;

/**
 * 扩展流程实例数据访问层接口实现类
 *
 * <p>
 * <a href="https://aizuda.com">官网</a>尊重知识产权，不允许非法使用，后果自负，不允许非法使用，后果自负
 * </p>
 *
 * @author hubin
 * @since 1.0
 */
public class FlwExtInstanceDaoImpl implements FlwExtInstanceDao {
    private final FlwExtInstanceMapper extInstanceMapper;

    public FlwExtInstanceDaoImpl(FlwExtInstanceMapper extInstanceMapper) {
        this.extInstanceMapper = extInstanceMapper;
    }

    @Override
    public boolean insert(FlwExtInstance extInstance) {
        return extInstanceMapper.insert(extInstance) > 0;
    }

    @Override
    public boolean deleteByProcessId(Long processId) {
        return extInstanceMapper.delete(Wrappers.<FlwExtInstance>lambdaQuery()
                .eq(FlwExtInstance::getProcessId, processId)) > 0;
    }

    @Override
    public boolean updateById(FlwExtInstance extInstance) {
        return extInstanceMapper.updateById(extInstance) > 0;
    }

    @Override
    public FlwExtInstance selectById(Long id) {
        return extInstanceMapper.selectById(id);
    }
}
