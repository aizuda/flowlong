/*
 * Copyright 2023-2025 Licensed under the Dual Licensing
 * website: https://aizuda.com
 */
package com.aizuda.bpm.mybatisplus.mapper;

import com.aizuda.bpm.engine.entity.FlwTask;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;

import java.util.List;

/**
 * 任务 Mapper
 *
 * <p>
 * <a href="https://aizuda.com">官网</a>尊重知识产权，不允许非法使用，后果自负
 * </p>
 *
 * @author hubin
 * @since 1.0
 */
public interface FlwTaskMapper extends BaseMapper<FlwTask> {

    /**
     * 根据流程实例ID获取任务列表
     *
     * @param instanceId 流程实例ID
     * @return 任务列表
     */
    default List<FlwTask> selectListByInstanceId(Long instanceId) {
        return this.selectList(Wrappers.<FlwTask>lambdaQuery().eq(FlwTask::getInstanceId, instanceId));
    }

    /**
     * 根据父任务ID获取任务列表
     *
     * @param parentTaskId 父任务ID
     * @return 任务列表
     */
    default List<FlwTask> selectListByParentTaskId(Long parentTaskId) {
        return this.selectList(Wrappers.<FlwTask>lambdaQuery().eq(FlwTask::getParentTaskId, parentTaskId));
    }

    /**
     * 根据父任务ID获取任务数量
     *
     * @param parentTaskId 父任务ID
     * @return 任务数量
     */
    default Long selectCountByParentTaskId(Long parentTaskId) {
        return this.selectCount(Wrappers.<FlwTask>lambdaQuery().eq(FlwTask::getParentTaskId, parentTaskId));
    }

}
