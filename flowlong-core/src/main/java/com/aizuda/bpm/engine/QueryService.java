/*
 * Copyright 2023-2025 Licensed under the Dual Licensing
 * website: https://aizuda.com
 */
package com.aizuda.bpm.engine;

import com.aizuda.bpm.engine.entity.*;

import java.util.List;
import java.util.Optional;

/**
 * 流程相关的查询服务
 *
 * <p>
 * <a href="https://aizuda.com">官网</a>尊重知识产权，不允许非法使用，后果自负
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
     * 根据流程实例ID获取扩展流程实例对象
     *
     * @param instanceId 扩展流程实例ID
     * @return FlwExtInstance 扩展流程实例对象
     */
    FlwExtInstance getExtInstance(Long instanceId);

    /**
     * 判断流程实例下是否存在活跃子流程实例
     *
     * @param instanceId 流程实例ID
     * @return true 存在 false 不存在
     */
    boolean existActiveSubProcess(Long instanceId);

    /**
     * 判断流程实例下是否存在活跃任务
     *
     * @param instanceId 流程实例ID
     * @return true 存在 false 不存在
     */
    boolean existActiveTask(Long instanceId);

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
     * 通过流程实例ID和任务key获取任务列表
     *
     * @param instanceId 流程实例ID
     * @param taskKey    任务KEY
     * @return 任务对象列表
     */
    List<FlwTask> getTasksByInstanceIdAndTaskKey(Long instanceId, String taskKey);

    default Optional<List<FlwTask>> getActiveTasksByInstanceIdAndTaskName(Long instanceId, String taskName) {
        return Optional.ofNullable(this.getTasksByInstanceIdAndTaskName(instanceId, taskName));
    }

    /**
     * 根据 流程实例ID 获取当前活动任务列表
     *
     * @param instanceId 流程实例ID
     * @return 当前活动任务列表
     */
    default Optional<List<FlwTask>> getActiveTasksByInstanceId(Long instanceId) {
        return Optional.ofNullable(this.getTasksByInstanceId(instanceId));
    }

    /**
     * 根据 流程实例ID 获取当前活动任务列表
     *
     * @param instanceId 流程实例ID
     * @return 当前活动任务列表
     */
    Optional<List<FlwTaskActor>> getActiveTaskActorsByInstanceId(Long instanceId);

    /**
     * 根据流程实例ID获取历史任务参与者数组
     *
     * @param instanceId 历史任务ID
     * @return 当前活动任务参与者列表
     */
    Optional<List<FlwHisTaskActor>> getCcTaskActorsByInstanceId(Long instanceId);

    /**
     * 根据任务ID获取活动任务参与者数组
     *
     * @param taskId 任务ID
     * @return 当前活动任务参与者列表
     */
    List<FlwTaskActor> getTaskActorsByTaskId(Long taskId);

    default Optional<List<FlwTaskActor>> getActiveTaskActorsByTaskId(Long taskId) {
        return Optional.ofNullable(this.getTaskActorsByTaskId(taskId));
    }

    /**
     * 根据任务ID获取活动任务参与者数组
     *
     * @param taskId  任务ID
     * @param actorId 任务参与者ID
     * @return 当前活动任务参与者列表
     */
    List<FlwTaskActor> getTaskActorsByTaskIdAndActorId(Long taskId, String actorId);

    /**
     * 根据任务ID获取历史任务参与者数组
     *
     * @param taskId 历史任务ID
     * @return 当前活动任务参与者列表
     */
    List<FlwHisTaskActor> getHisTaskActorsByTaskId(Long taskId);

    /**
     * 根据任务ID获取历史任务参与者数组
     *
     * @param taskId  历史任务ID
     * @param actorId 任务参与者ID
     * @return 当前活动任务参与者列表
     */
    List<FlwHisTaskActor> getHisTaskActorsByTaskIdAndActorId(Long taskId, String actorId);

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
     * <p>
     * 额外根据唯一的ID进行排序，防止低版本数据库时间重复的情况。（注：ID 是时间增长的，也是有时间顺序的）
     * </p>
     *
     * @param instanceId 实例ID
     * @return 历史任务列表
     */
    Optional<List<FlwHisTask>> getHisTasksByInstanceId(Long instanceId);

    /**
     * 根据实例ID获取所有子流程
     *
     * @param instanceId 实例ID
     * @return 所有子流程
     */
    Optional<List<FlwInstance>> getSubProcessByInstanceId(Long instanceId);

    /**
     * 根据业务主键获取流程实例
     *
     * @param businessKey 业务主键
     * @return 流程实例列表
     */
    Optional<List<FlwInstance>> getInstancesByBusinessKey(String businessKey);

    /**
     * 根据业务主键获取历史流程实例
     *
     * @param businessKey 业务主键
     * @return 历史流程实例列表
     */
    Optional<List<FlwHisInstance>> getHisInstancesByBusinessKey(String businessKey);
}
