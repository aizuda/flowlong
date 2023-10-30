/* 
 * Copyright 2023-2025 Licensed under the AGPL License
 */
package com.flowlong.bpm.mybatisplus.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.flowlong.bpm.engine.entity.FlwHisTaskActor;

import java.util.List;

/**
 * 历史任务参与者 Mapper
 *
 * <p>
 * 尊重知识产权，不允许非法使用，后果自负
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
     */
    default List<FlwHisTaskActor> selectListByTaskId(Long taskId) {
        return this.selectList(Wrappers.<FlwHisTaskActor>lambdaQuery().eq(FlwHisTaskActor::getTaskId, taskId));
    }

    /**
     * 通过任务ID删除参与者
     *
     * @param taskIds 任务ID列表
     */
    default boolean deleteByTaskIds(List<Long> taskIds) {
        return this.delete(Wrappers.<FlwHisTaskActor>lambdaQuery().in(FlwHisTaskActor::getTaskId, taskIds)) > 0;
    }

}
