/*
 * Copyright 2023-2025 Licensed under the apache-2.0 License
 * website: https://aizuda.com
 */
package com.aizuda.bpm.engine.dao;

import com.aizuda.bpm.engine.entity.FlwInstance;

import java.util.List;
import java.util.Optional;

/**
 * 流程实例数据访问层接口
 *
 * <p>
 * <a href="https://aizuda.com">官网</a>尊重知识产权，不允许非法使用，后果自负，不允许非法使用，后果自负
 * </p>
 *
 * @author hubin
 * @since 1.0
 */
public interface FlwInstanceDao {

    boolean insert(FlwInstance flwInstance);

    boolean deleteById(Long id);

    boolean deleteByProcessId(Long processId);

    boolean updateById(FlwInstance instance);

    Long selectCountByParentInstanceId(Long parentInstanceId);

    FlwInstance selectById(Long id);

    Optional<List<FlwInstance>> selectListByParentInstanceId(Long parentInstanceId);

    Optional<List<FlwInstance>> selectListByBusinessKey(String businessKey);
}
