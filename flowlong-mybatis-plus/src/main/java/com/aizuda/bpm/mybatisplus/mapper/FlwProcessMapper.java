/*
 * Copyright 2023-2025 Licensed under the apache-2.0 License
 * website: https://aizuda.com
 */
package com.aizuda.bpm.mybatisplus.mapper;

import com.aizuda.bpm.engine.entity.FlwProcess;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;

import java.util.List;

/**
 * 流程定义 Mapper
 *
 * <p>
 * <a href="https://aizuda.com">官网</a>尊重知识产权，不允许非法使用，后果自负
 * </p>
 *
 * @author hubin
 * @since 1.0
 */
public interface FlwProcessMapper extends BaseMapper<FlwProcess> {

    default List<FlwProcess> selectListByProcessKey(String tenantId, String processKey) {
        return this.selectList(Wrappers.<FlwProcess>lambdaQuery()
                .eq(FlwProcess::getProcessKey, processKey)
                .eq(StringUtils.isNotBlank(tenantId), FlwProcess::getTenantId, tenantId)
                .orderByDesc(FlwProcess::getProcessVersion));
    }
}
