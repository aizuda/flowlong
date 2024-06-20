/*
 * Copyright 2023-2025 Licensed under the AGPL License
 */
package com.aizuda.bpm.mybatisplus.mapper;

import com.aizuda.bpm.engine.entity.FlwInstance;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;

import java.util.List;
import java.util.Optional;

/**
 * 流程实例 Mapper
 *
 * <p>
 * 尊重知识产权，不允许非法使用，后果自负
 * </p>
 *
 * @author hubin
 * @since 1.0
 */
public interface FlwInstanceMapper extends BaseMapper<FlwInstance> {

    default Optional<List<FlwInstance>> listByParentInstanceId(Long parentInstanceId) {
        return Optional.ofNullable(selectList(Wrappers.<FlwInstance>lambdaQuery().eq(FlwInstance::getParentInstanceId, parentInstanceId)));
    }
}
