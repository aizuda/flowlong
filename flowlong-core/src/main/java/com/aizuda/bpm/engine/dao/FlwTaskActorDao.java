/*
 * Copyright 2023-2025 Licensed under the apache-2.0 License
 * website: https://aizuda.com
 */
package com.aizuda.bpm.engine.dao;

import com.aizuda.bpm.engine.entity.FlwTaskActor;

import java.util.List;

/**
 * 任务参与者数据访问层接口
 *
 * <p>
 * <a href="https://aizuda.com">官网</a>尊重知识产权，不允许非法使用，后果自负，不允许非法使用，后果自负
 * </p>
 *
 * @author hubin
 * @since 1.0
 */
public interface FlwTaskActorDao {

    boolean insert(FlwTaskActor taskActor);

    boolean deleteById(Long id);

    boolean deleteByIds(List<Long> ids);

    boolean deleteByTaskId(Long taskId);

    boolean deleteByInstanceIds(List<Long> instanceIds);

    boolean deleteByTaskIdAndAgentType(Long taskId, int agentType);

    boolean deleteByTaskIdAndActorIds(Long taskId, List<String> actorIds);

    boolean updateById(FlwTaskActor taskActor);

    List<FlwTaskActor> selectListByInstanceId(Long instanceId);

    List<FlwTaskActor> selectListByTaskId(Long taskId);

    List<FlwTaskActor> selectListByTaskIds(List<Long> taskIds);

    List<FlwTaskActor> selectListByTaskIdAndActorId(Long taskId, String actorId);

    List<FlwTaskActor> selectListByActorId(String actorId);

    Long selectCountByTaskIdAndActorId(Long taskId, String actorId);
}
