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

/**
 * 流程相关的查询服务
 *
 * <p>
 * 尊重知识产权，CV 请保留版权，爱组搭 http://aizuda.com 出品
 * </p>
 *
 * @author hubin
 * @since 1.0
 */
public interface QueryService {

    /**
     * 根据流程实例ID获取流程实例对象
     *
     * @param instanceId 流程实例id
     * @return Instance 流程实例对象
     */
    Instance getInstance(Long instanceId);

    /**
     * 根据流程实例ID获取历史流程实例对象
     *
     * @param instanceId 历史流程实例id
     * @return HistoryInstance 历史流程实例对象
     */
    HisInstance getHistInstance(Long instanceId);

    /**
     * 根据任务ID获取任务对象
     *
     * @param taskId 任务id
     * @return Task 任务对象
     */
    Task getTask(Long taskId);

    /**
     * 根据任务ID获取历史任务对象
     *
     * @param taskId 历史任务id
     * @return HistoryTask 历史任务对象
     */
    HisTask getHistTask(Long taskId);


    /**
     * 通过流程实例ID获取任务列表
     *
     * @param instanceId 流程实例ID
     * @return 任务对象列表
     */
    List<Task> getTasksByInstanceId(Long instanceId);

    List<Task> getActiveTasksByInstanceId(Long instanceId);

    /**
     * 根据任务ID获取活动任务参与者数组
     *
     * @param taskId 任务id
     * @return String[] 参与者id数组
     */
    List<TaskActor> getTaskActorsByTaskId(Long taskId);

    /**
     * 根据任务ID获取历史任务参与者数组
     *
     * @param taskId 历史任务id
     * @return String[] 历史参与者id数组
     */
    List<HisTaskActor> getHistoryTaskActorsByTaskId(Long taskId);

}
