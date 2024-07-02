/*
 * Copyright 2023-2025 Licensed under the AGPL License
 */
package com.aizuda.bpm.engine.dao;

import com.aizuda.bpm.engine.entity.FlwHisInstance;

import java.util.List;

/**
 * 历史流程实例数据访问层接口
 *
 * <p>
 * 尊重知识产权，不允许非法使用，后果自负，不允许非法使用，后果自负
 * </p>
 *
 * @author hubin
 * @since 1.0
 */
public interface FlwHisInstanceDao {

    boolean insert(FlwHisInstance hisInstance);

    boolean deleteByProcessId(Long processId);

    boolean updateById(FlwHisInstance hisInstance);

    FlwHisInstance selectById(Long id);

    List<FlwHisInstance> selectListByProcessId(Long processId);
}
