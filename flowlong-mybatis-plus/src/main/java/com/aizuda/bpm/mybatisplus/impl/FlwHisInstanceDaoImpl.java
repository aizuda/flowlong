/*
 * Copyright 2023-2025 Licensed under the apache-2.0 License
 * website: https://aizuda.com
 */
package com.aizuda.bpm.mybatisplus.impl;

import com.aizuda.bpm.engine.dao.FlwHisInstanceDao;
import com.aizuda.bpm.engine.entity.FlwHisInstance;
import com.aizuda.bpm.mybatisplus.mapper.FlwHisInstanceMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;

import java.util.List;

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

    @Override
    public List<FlwHisInstance> selectListByProcessId(Long processId) {
        return hisInstanceMapper.selectList(Wrappers.<FlwHisInstance>lambdaQuery()
                .eq(FlwHisInstance::getProcessId, processId));
    }
}
