/* Copyright 2023-2025 jobob@qq.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.flowlong.bpm.engine;

import com.flowlong.bpm.engine.entity.*;

import java.util.List;
import java.util.Optional;

/**
 * 流程相关的查询服务
 *
 * <p>
 * 尊重知识产权，CV 请保留版权，爱组搭 http://aizuda.com 出品，不允许非法使用，后果自负
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
    Instance getInstance(Long instanceId);

    /**
     * 根据流程实例ID获取历史流程实例对象
     *
     * @param instanceId 历史流程实例ID
     * @return HistoryInstance 历史流程实例对象
     */
    HisInstance getHistInstance(Long instanceId);

    /**
     * 根据任务ID获取任务对象
     *
     * @param taskId 任务ID
     * @return Task 任务对象
     */
    Task getTask(Long taskId);

    /**
     * 根据任务ID获取历史任务对象
     *
     * @param taskId 历史任务ID
     * @return HistoryTask 历史任务对象
     */
    HisTask getHistTask(Long taskId);

    /**
     * 根据任务名称查询历史任务对象列表
     *
     * @param instanceId
     * @param taskName   任务名称(亦是节点名称)
     * @return 历史任务节点列表
     */
    Optional<List<HisTask>> getHisTasksByName(Long instanceId, String taskName);

    /**
     * 通过流程实例ID获取任务列表
     *
     * @param instanceId 流程实例ID
     * @return 任务对象列表
     */
    List<Task> getTasksByInstanceId(Long instanceId);

    List<Task> getTasksByInstanceIdAndTaskName(Long instanceId, String taskName);

    /**
     * 根据 流程实例ID 获取当前活动任务列表
     *
     * @param instanceId 流程实例ID
     * @return 当前活动任务列表
     */
    Optional<List<Task>> getActiveTasksByInstanceId(Long instanceId);

    /**
     * 根据 流程实例ID 获取当前活动任务列表
     *
     * @param instanceId 流程实例ID
     * @return 当前活动任务列表
     */
    Optional<List<TaskActor>> getActiveTaskActorsByInstanceId(Long instanceId);

    /**
     * 根据任务ID获取活动任务参与者数组
     *
     * @param taskId 任务ID
     * @return String[] 参与者ID数组
     */
    List<TaskActor> getTaskActorsByTaskId(Long taskId);

    /**
     * 根据任务ID获取历史任务参与者数组
     *
     * @param taskId 历史任务ID
     * @return String[] 历史参与者ID数组
     */
    List<HisTaskActor> getHistoryTaskActorsByTaskId(Long taskId);

    /**
     * 根据实例ID和任务节点名称获取当前节点激活的任务
     *
     * @param instanceId 实例ID
     * @param taskNames  任务节点名称
     * @return 子任务列表
     */
    List<Task> getActiveTasks(Long instanceId, List<String> taskNames);

    /**
     * 根据实例ID获取实例所有历史任务，时间倒序
     *
     * @param instanceId 实例ID
     * @return 历史任务列表
     */
    Optional<List<HisTask>> getHisTasksByInstanceId(Long instanceId);
}
