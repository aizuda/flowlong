/*
 * Copyright 2023-2025 Licensed under the Dual Licensing
 * website: https://aizuda.com
 */
package com.aizuda.bpm.mybatisplus.impl;

import com.aizuda.bpm.engine.dao.FlwProcessDao;
import com.aizuda.bpm.engine.entity.FlwProcess;
import com.aizuda.bpm.mybatisplus.mapper.FlwProcessMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;

import java.util.List;

/**
 * 流程定义数据访问层接口实现类
 *
 * <p>
 * <a href="https://aizuda.com">官网</a>尊重知识产权，不允许非法使用，后果自负，不允许非法使用，后果自负
 * </p>
 *
 * @author hubin
 * @since 1.0
 */
public class FlwProcessDaoImpl implements FlwProcessDao {
    private final FlwProcessMapper processMapper;

    public FlwProcessDaoImpl(FlwProcessMapper processMapper) {
        this.processMapper = processMapper;
    }

    @Override
    public boolean insert(FlwProcess process) {
        return processMapper.insert(process) > 0;
    }

    @Override
    public boolean deleteById(Long id) {
        return processMapper.deleteById(id) > 0;
    }

    @Override
    public boolean updateById(FlwProcess process) {
        return processMapper.updateById(process) > 0;
    }

    @Override
    public boolean updateByProcessKey(FlwProcess process, String tenantId, String processKey) {
        return processMapper.update(process, Wrappers.<FlwProcess>lambdaQuery()
                .eq(null != tenantId, FlwProcess::getTenantId, tenantId)
                .eq(FlwProcess::getProcessKey, processKey)) > 0;
    }

    @Override
    public FlwProcess selectById(Long id) {
        return processMapper.selectById(id);
    }

    @Override
    public List<FlwProcess> selectListByProcessKeyAndVersion(String tenantId, String processKey, Integer version) {
        LambdaQueryWrapper<FlwProcess> lqw = Wrappers.lambdaQuery();
        lqw.eq(null != tenantId, FlwProcess::getTenantId, tenantId);
        lqw.eq(FlwProcess::getProcessKey, processKey);
        if (null != version) {
            lqw.eq(FlwProcess::getProcessVersion, version);
        } else {
            lqw.orderByDesc(FlwProcess::getProcessVersion);
        }
        return processMapper.selectList(lqw);
    }
}
