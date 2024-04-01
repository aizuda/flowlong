/* 
 * Copyright 2023-2025 Licensed under the AGPL License
 */
package com.aizuda.bpm.mybatisplus.mapper;

import com.aizuda.bpm.engine.entity.FlwTaskActor;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;

import java.util.List;

/**
 * 任务参与者 Mapper
 *
 * <p>
 * 尊重知识产权，不允许非法使用，后果自负
 * </p>
 *
 * @author hubin
 * @since 1.0
 */
public interface FlwTaskActorMapper extends BaseMapper<FlwTaskActor> {

    /**
     * 通过任务ID获取参与者列表
     *
     * @param taskId 任务ID
     */
    default List<FlwTaskActor> selectListByTaskId(Long taskId) {
        return this.selectList(Wrappers.<FlwTaskActor>lambdaQuery().eq(FlwTaskActor::getTaskId, taskId));
    }

    /**
     * 通过任务ID删除参与者
     *
     * @param taskId 任务ID
     */
    default boolean deleteByTaskId(Long taskId) {
        return this.delete(Wrappers.<FlwTaskActor>lambdaQuery().eq(FlwTaskActor::getTaskId, taskId)) > 0;
    }

    /**
     * 通过任务ID删除参与者
     *
     * @param taskIds 任务ID列表
     */
    default boolean deleteByTaskIds(List<Long> taskIds) {
        return this.delete(Wrappers.<FlwTaskActor>lambdaQuery().in(FlwTaskActor::getTaskId, taskIds)) > 0;
    }

}
