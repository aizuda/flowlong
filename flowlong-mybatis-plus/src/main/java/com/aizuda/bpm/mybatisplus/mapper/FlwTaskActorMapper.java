/*
 * Copyright 2023-2025 Licensed under the apache-2.0 License
 * website: https://aizuda.com
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
 * <a href="https://aizuda.com">官网</a>尊重知识产权，不允许非法使用，后果自负
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
     * @return 参与者列表
     */
    default List<FlwTaskActor> selectListByTaskId(Long taskId) {
        return this.selectList(Wrappers.<FlwTaskActor>lambdaQuery().eq(FlwTaskActor::getTaskId, taskId));
    }

    /**
     * 通过任务ID列表获取参与者列表
     *
     * @param taskIds 任务ID列表
     * @return 参与者列表
     */
    default List<FlwTaskActor> selectListByTaskIds(List<Long> taskIds) {
        return this.selectList(Wrappers.<FlwTaskActor>lambdaQuery().in(FlwTaskActor::getTaskId, taskIds));
    }

    /**
     * 通过流程实例ID获取参与者列表
     *
     * @param instanceId 流程实例ID
     * @return 参与者列表
     */
    default List<FlwTaskActor> selectListByInstanceId(Long instanceId) {
        return this.selectList(Wrappers.<FlwTaskActor>lambdaQuery().eq(FlwTaskActor::getInstanceId, instanceId));
    }

    /**
     * 通过任务ID删除参与者
     *
     * @param taskId 任务ID
     * @return true 成功 false 失败
     */
    default boolean deleteByTaskId(Long taskId) {
        return this.delete(Wrappers.<FlwTaskActor>lambdaQuery().eq(FlwTaskActor::getTaskId, taskId)) > 0;
    }

    /**
     * 通过任务ID删除参与者
     *
     * @param taskIds 任务ID列表
     * @return true 成功 false 失败
     */
    default boolean deleteByTaskIds(List<Long> taskIds) {
        return this.delete(Wrappers.<FlwTaskActor>lambdaQuery().in(FlwTaskActor::getTaskId, taskIds)) > 0;
    }

}
