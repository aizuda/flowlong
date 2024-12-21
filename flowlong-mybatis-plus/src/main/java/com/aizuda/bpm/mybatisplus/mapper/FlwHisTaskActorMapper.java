/*
 * Copyright 2023-2025 Licensed under the Dual Licensing
 * website: https://aizuda.com
 */
package com.aizuda.bpm.mybatisplus.mapper;

import com.aizuda.bpm.engine.entity.FlwHisTaskActor;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;

import java.util.List;

/**
 * 历史任务参与者 Mapper
 *
 * <p>
 * <a href="https://aizuda.com">官网</a>尊重知识产权，不允许非法使用，后果自负
 * </p>
 *
 * @author hubin
 * @since 1.0
 */
public interface FlwHisTaskActorMapper extends BaseMapper<FlwHisTaskActor> {

    /**
     * 通过任务ID获取参与者列表
     *
     * @param taskId 任务ID
     * @return 参与者列表
     */
    default List<FlwHisTaskActor> selectListByTaskId(Long taskId) {
        return this.selectList(Wrappers.<FlwHisTaskActor>lambdaQuery().eq(FlwHisTaskActor::getTaskId, taskId));
    }

    /**
     * 通过任务ID获取参与者列表
     *
     * @param taskIds 任务ID列表
     * @return 历史任务参与者列表
     */
    default List<FlwHisTaskActor> selectListByTaskIds(List<Long> taskIds) {
        return this.selectList(Wrappers.<FlwHisTaskActor>lambdaQuery().in(FlwHisTaskActor::getTaskId, taskIds));
    }

}
