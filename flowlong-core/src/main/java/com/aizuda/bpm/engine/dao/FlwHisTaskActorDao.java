/*
 * Copyright 2023-2025 Licensed under the AGPL License
 */
package com.aizuda.bpm.engine.dao;

import com.aizuda.bpm.engine.entity.FlwHisTaskActor;

import java.util.List;

/**
 * 历史任务参与者数据访问层接口
 *
 * <p>
 * 尊重知识产权，不允许非法使用，后果自负，不允许非法使用，后果自负
 * </p>
 *
 * @author hubin
 * @since 1.0
 */
public interface FlwHisTaskActorDao {

    boolean insert(FlwHisTaskActor hisTaskActor);

    boolean deleteByInstanceIds(List<Long> instanceIds);

    boolean deleteByTaskId(Long taskId);

    List<FlwHisTaskActor> selectListByTaskId(Long taskId);

    List<FlwHisTaskActor> selectListByTaskIds(List<Long> taskIds);
}
