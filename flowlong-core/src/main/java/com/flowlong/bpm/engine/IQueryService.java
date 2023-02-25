/* Copyright 2023-2025 www.flowlong.com
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

import com.flowlong.bpm.engine.access.Page;
import com.flowlong.bpm.engine.access.QueryFilter;
import com.flowlong.bpm.engine.entity.*;

import java.util.List;

/**
 * 流程相关的查询服务
 *
 * @author hubin
 * @since 1.0
 */
public interface IQueryService {

    /**
     * 根据流程实例ID获取流程实例对象
     *
     * @param instanceId 流程实例id
     * @return Instance 流程实例对象
     */
    Instance getInstance(String instanceId);

    /**
     * 根据流程实例ID获取历史流程实例对象
     *
     * @param instanceId 历史流程实例id
     * @return HistoryInstance 历史流程实例对象
     */
    HisInstance getHistInstance(String instanceId);

    /**
     * 根据任务ID获取任务对象
     *
     * @param taskId 任务id
     * @return Task 任务对象
     */
    Task getTask(String taskId);

    /**
     * 根据任务ID获取历史任务对象
     *
     * @param taskId 历史任务id
     * @return HistoryTask 历史任务对象
     */
    HisTask getHistTask(String taskId);

    /**
     * 根据任务ID获取活动任务参与者数组
     *
     * @param taskId 任务id
     * @return String[] 参与者id数组
     */
    String[] getTaskActorsByTaskId(String taskId);

    /**
     * 根据任务ID获取历史任务参与者数组
     *
     * @param taskId 历史任务id
     * @return String[] 历史参与者id数组
     */
    String[] getHistoryTaskActorsByTaskId(String taskId);

    /**
     * 根据filter查询活动任务
     *
     * @param filter 查询过滤器
     * @return List<Task> 活动任务集合
     */
    List<Task> getActiveTasks(QueryFilter filter);

    /**
     * 根据filter分页查询活动任务
     *
     * @param page   分页对象
     * @param filter 查询过滤器
     * @return List<Task> 活动任务集合
     */
    List<Task> getActiveTasks(Page<Task> page, QueryFilter filter);

    /**
     * 根据filter查询流程实例列表
     *
     * @param filter 查询过滤器
     * @return List<Instance> 活动实例集合
     */
    List<Instance> getActiveInstances(QueryFilter filter);

    /**
     * 根据filter分页查询流程实例列表
     *
     * @param page   分页对象
     * @param filter 查询过滤器
     * @return List<Instance> 活动实例集合
     */
    List<Instance> getActiveInstances(Page<Instance> page, QueryFilter filter);

    /**
     * 根据filter查询历史流程实例
     *
     * @param filter 查询过滤器
     * @return List<HistoryInstance> 历史实例集合
     */
    List<HisInstance> getHistoryInstances(QueryFilter filter);

    /**
     * 根据filter分页查询历史流程实例
     *
     * @param page   分页对象
     * @param filter 查询过滤器
     * @return List<HistoryInstance> 历史实例集合
     */
    List<HisInstance> getHistoryInstances(Page<HisInstance> page, QueryFilter filter);

    /**
     * 根据filter查询所有已完成的任务
     *
     * @param filter 查询过滤器
     * @return List<HistoryTask> 历史任务集合
     */
    List<HisTask> getHistoryTasks(QueryFilter filter);

    /**
     * 根据filter分页查询已完成的历史任务
     *
     * @param page   分页对象
     * @param filter 查询过滤器
     * @return List<HistoryTask> 历史任务集合
     */
    List<HisTask> getHistoryTasks(Page<HisTask> page, QueryFilter filter);

    /**
     * 根据filter分页查询工作项（包含process、instance、task三个实体的字段集合）
     *
     * @param page   分页对象
     * @param filter 查询过滤器
     * @return List<WorkItem> 活动工作项集合
     */
    List<WorkItem> getWorkItems(Page<WorkItem> page, QueryFilter filter);

    /**
     * 根据filter分页查询抄送工作项（包含process、instance）
     *
     * @param page   分页对象
     * @param filter 查询过滤器
     * @return List<WorkItem> 抄送工作项集合
     */
    List<HisInstance> getCCWorks(Page<HisInstance> page, QueryFilter filter);

    /**
     * 根据filter分页查询已完成的历史任务项
     *
     * @param page   分页对象
     * @param filter 查询过滤器
     * @return List<WorkItem> 历史工作项集合
     */
    List<WorkItem> getHistoryWorkItems(Page<WorkItem> page, QueryFilter filter);

    /**
     * 根据类型T、Sql语句、参数查询单个对象
     *
     * @param T    类型
     * @param sql  sql语句
     * @param args 参数列表
     * @return
     */
    public <T> T nativeQueryObject(Class<T> T, String sql, Object... args);

    /**
     * 根据类型T、Sql语句、参数查询列表对象
     *
     * @param T    类型
     * @param sql  sql语句
     * @param args 参数列表
     * @return
     */
    public <T> List<T> nativeQueryList(Class<T> T, String sql, Object... args);

    /**
     * 根据类型T、Sql语句、参数分页查询列表对象
     *
     * @param page 分页对象
     * @param T    类型
     * @param sql  sql语句
     * @param args 参数列表
     * @return
     */
    public <T> List<T> nativeQueryList(Page<T> page, Class<T> T, String sql, Object... args);
}
