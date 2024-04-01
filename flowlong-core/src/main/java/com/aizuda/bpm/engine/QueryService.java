/*
 * Copyright 2023-2025 Licensed under the AGPL License
 */
package com.aizuda.bpm.engine;

import com.aizuda.bpm.engine.entity.*;

import java.util.List;
import java.util.Optional;

/**
 * 流程相关的查询服务
 *
 * <p>
 * 尊重知识产权，不允许非法使用，后果自负
 * </p>
 *
 * @author hubin
 * @since 1.0
 */
public interface QueryService {

    /**
     * 根据流程实例ID获取流程实例对象
     *
     * @param instanceId 流程实例ID
     * @return Instance 流程实例对象
     */
    FlwInstance getInstance(Long instanceId);

    /**
     * 根据流程实例ID获取历史流程实例对象
     *
     * @param instanceId 历史流程实例ID
     * @return HistoryInstance 历史流程实例对象
     */
    FlwHisInstance getHistInstance(Long instanceId);

    /**
     * 根据任务ID获取任务对象
     *
     * @param taskId 任务ID
     * @return Task 任务对象
     */
    FlwTask getTask(Long taskId);

    /**
     * 根据任务ID获取历史任务对象
     *
     * @param taskId 历史任务ID
     * @return HistoryTask 历史任务对象
     */
    FlwHisTask getHistTask(Long taskId);

    /**
     * 根据任务名称查询历史任务对象列表
     *
     * @param instanceId 流程实例ID
     * @param taskName   任务名称(亦是节点名称)
     * @return 历史任务节点列表
     */
    Optional<List<FlwHisTask>> getHisTasksByName(Long instanceId, String taskName);

    /**
     * 通过流程实例ID获取任务列表
     *
     * @param instanceId 流程实例ID
     * @return 任务对象列表
     */
    List<FlwTask> getTasksByInstanceId(Long instanceId);

    List<FlwTask> getTasksByInstanceIdAndTaskName(Long instanceId, String taskName);

    /**
     * 根据 流程实例ID 获取当前活动任务列表
     *
     * @param instanceId 流程实例ID
     * @return 当前活动任务列表
     */
    Optional<List<FlwTask>> getActiveTasksByInstanceId(Long instanceId);

    /**
     * 根据 流程实例ID 获取当前活动任务列表
     *
     * @param instanceId 流程实例ID
     * @return 当前活动任务列表
     */
    Optional<List<FlwTaskActor>> getActiveTaskActorsByInstanceId(Long instanceId);

    /**
     * 根据任务ID获取活动任务参与者数组
     *
     * @param taskId 任务ID
     * @return String[] 参与者ID数组
     */
    List<FlwTaskActor> getTaskActorsByTaskId(Long taskId);

    /**
     * 根据任务ID获取历史任务参与者数组
     *
     * @param taskId 历史任务ID
     * @return String[] 历史参与者ID数组
     */
    List<FlwHisTaskActor> getHistoryTaskActorsByTaskId(Long taskId);

    /**
     * 根据实例ID和任务节点名称获取当前节点激活的任务
     *
     * @param instanceId 实例ID
     * @param taskNames  任务节点名称
     * @return 子任务列表
     */
    List<FlwTask> getActiveTasks(Long instanceId, List<String> taskNames);

    /**
     * 根据实例ID获取实例所有历史任务，时间倒序
     *
     * @param instanceId 实例ID
     * @return 历史任务列表
     */
    Optional<List<FlwHisTask>> getHisTasksByInstanceId(Long instanceId);
}
