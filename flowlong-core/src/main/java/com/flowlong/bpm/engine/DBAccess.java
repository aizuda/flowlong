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
import com.flowlong.bpm.engine.entity.Process;
import com.flowlong.bpm.engine.entity.*;

import java.util.List;

/**
 * 数据库访问接口
 * 主要提供保存、更新、查询流程的相关table
 *
 * @author hubin
 * @since 1.0
 */
public interface DBAccess {
    /**
     * 根据访问对象，设置具体的实现类
     *
     * @param accessObject 数据库访问对象(Connection等)
     */
    void initialize(Object accessObject);

    /**
     * 保存任务对象
     *
     * @param task 任务对象
     */
    void saveTask(Task task);

    /**
     * 保存流程实例对象
     *
     * @param instance 流程实例对象
     */
    void saveInstance(Instance instance);

    /**
     * 保存抄送实例
     *
     * @param ccinstance 抄送实体
     */
    void saveCCInstance(CCInstance ccinstance);

    /**
     * 保存流程定义对象
     *
     * @param process 流程定义对象
     */
    void saveProcess(Process process);

    /**
     * 保存任务参与者对象
     *
     * @param taskActor 任务参与者对象
     */
    void saveTaskActor(TaskActor taskActor);

    /**
     * 更新任务对象
     *
     * @param task 任务对象
     */
    void updateTask(Task task);

    /**
     * 更新流程实例对象
     *
     * @param instance 流程实例对象
     */
    void updateInstance(Instance instance);

    /**
     * 更新抄送状态
     *
     * @param ccinstance 抄送实体对象
     */
    void updateCCInstance(CCInstance ccinstance);

    /**
     * 更新流程定义对象
     *
     * @param process 流程定义对象
     */
    void updateProcess(Process process);

    /**
     * 删除流程定义对象
     *
     * @param process 流程定义对象
     */
    void deleteProcess(Process process);

    /**
     * 更新流程定义类别
     *
     * @param type 类别
     */
    void updateProcessType(String id, String type);

    /**
     * 删除任务、任务参与者对象
     *
     * @param task 任务对象
     */
    void deleteTask(Task task);

    /**
     * 删除流程实例对象
     *
     * @param instance 流程实例对象
     */
    void deleteInstance(Instance instance);

    /**
     * 删除抄送记录
     *
     * @param ccinstance 抄送实体对象
     */
    void deleteCCInstance(CCInstance ccinstance);

    /**
     * 删除参与者
     *
     * @param taskId 任务id
     * @param actors 参与者集合
     */
    void removeTaskActor(String taskId, String... actors);

    /**
     * 迁移活动实例
     *
     * @param instance 历史流程实例对象
     */
    void saveHistory(HisInstance instance);

    /**
     * 更新历史流程实例状态
     *
     * @param instance 历史流程实例对象
     */
    void updateHistory(HisInstance instance);

    /**
     * 迁移活动任务
     *
     * @param task 历史任务对象
     */
    void saveHistory(HisTask task);

    /**
     * 删除历史实例记录
     *
     * @param hisInstance 历史实例
     */
    void deleteHistoryInstance(HisInstance hisInstance);

    /**
     * 删除历史任务记录
     *
     * @param hisTask 历史任务
     */
    void deleteHistoryTask(HisTask hisTask);

    /**
     * 更新实例变量（包括历史实例表）
     *
     * @param instance 实例对象
     */
    void updateInstanceVariable(Instance instance);

    /**
     * 保存委托代理对象
     *
     * @param surrogate 委托代理对象
     */
    void saveSurrogate(Surrogate surrogate);

    /**
     * 更新委托代理对象
     *
     * @param surrogate 委托代理对象
     */
    void updateSurrogate(Surrogate surrogate);

    /**
     * 删除委托代理对象
     *
     * @param surrogate 委托代理对象
     */
    void deleteSurrogate(Surrogate surrogate);

    /**
     * 根据主键id查询委托代理对象
     *
     * @param id 主键id
     * @return surrogate 委托代理对象
     */
    Surrogate getSurrogate(String id);

    /**
     * 根据授权人、流程名称查询委托代理对象
     *
     * @param page   分页对象
     * @param filter 查询过滤器
     * @return List<Surrogate> 委托代理对象集合
     */
    List<Surrogate> getSurrogate(Page<Surrogate> page, QueryFilter filter);

    /**
     * 根据任务id查询任务对象
     *
     * @param taskId 任务id
     * @return Task 任务对象
     */
    Task getTask(String taskId);

    /**
     * 根据任务ID获取历史任务对象
     *
     * @param taskId 历史任务id
     * @return 历史任务对象
     */
    HisTask getHistTask(String taskId);

    /**
     * 根据父任务id查询所有子任务
     *
     * @param parentTaskId 父任务id
     * @return List<Task> 活动任务集合
     */
    List<Task> getNextActiveTasks(String parentTaskId);

    /**
     * 根据流程实例id、任务名称获取
     *
     * @param instanceId      流程实例id
     * @param taskName     任务名称
     * @param parentTaskId 父任务id
     * @return List<Task> 活动任务集合
     */
    List<Task> getNextActiveTasks(String instanceId, String taskName, String parentTaskId);

    /**
     * 根据任务id查询所有活动任务参与者集合
     *
     * @param taskId 活动任务id
     * @return List<TaskActor> 活动任务参与者集合
     */
    List<TaskActor> getTaskActorsByTaskId(String taskId);

    /**
     * 根据任务id查询所有历史任务参与者集合
     *
     * @param taskId 历史任务id
     * @return List<HistoryTaskActor> 历史任务参与者集合
     */
    List<HisTaskActor> getHistTaskActorsByTaskId(String taskId);

    /**
     * 根据流程实例id查询实例对象
     *
     * @param instanceId 活动流程实例id
     * @return Instance 活动流程实例对象
     */
    Instance getInstance(String instanceId);

    /**
     * 根据流程实例id、参与者id获取抄送记录
     *
     * @param instanceId  活动流程实例id
     * @param actorIds 参与者id
     * @return 传送记录列表
     */
    List<CCInstance> getCCInstance(String instanceId, String... actorIds);

    /**
     * 根据流程实例ID获取历史流程实例对象
     *
     * @param instanceId 历史流程实例id
     * @return HistoryInstance 历史流程实例对象
     */
    HisInstance getHistInstance(String instanceId);

    /**
     * 根据流程定义id查询流程定义对象
     *
     * @param id 流程定义id
     * @return Process 流程定义对象
     */
    Process getProcess(String id);

    /**
     * 根据流程名称查询最近的版本号
     *
     * @param name 流程名称
     * @return Integer 流程定义版本号
     */
    Integer getLatestProcessVersion(String name);

    /**
     * 根据查询的参数，分页对象，返回分页后的查询结果
     *
     * @param page   分页对象
     * @param filter 查询过滤器
     * @return List<Process> 流程定义集合
     */
    List<Process> getProcess(Page<Process> page, QueryFilter filter);

    /**
     * 分页查询流程实例
     *
     * @param page   分页对象
     * @param filter 查询过滤器
     * @return List<Instance> 活动流程实例集合
     */
    List<Instance> getActiveInstances(Page<Instance> page, QueryFilter filter);

    /**
     * 分页查询活动任务列表
     *
     * @param page   分页对象
     * @param filter 查询过滤器
     * @return List<Task> 活动任务集合
     */
    List<Task> getActiveTasks(Page<Task> page, QueryFilter filter);

    /**
     * 分页查询历史流程实例
     *
     * @param page   分页对象
     * @param filter 查询过滤器
     * @return List<HistoryInstance> 历史流程实例集合
     */
    List<HisInstance> getHistoryInstances(Page<HisInstance> page, QueryFilter filter);

    /**
     * 根据参与者分页查询已完成的历史任务
     *
     * @param page   分页对象
     * @param filter 查询过滤器
     * @return List<HistoryTask> 历史任务集合
     */
    List<HisTask> getHistoryTasks(Page<HisTask> page, QueryFilter filter);

    /**
     * 根据查询的参数，分页对象，返回分页后的活动工作项
     *
     * @param page   分页对象
     * @param filter 查询过滤器
     * @return List<WorkItem> 活动工作项
     */
    List<WorkItem> getWorkItems(Page<WorkItem> page, QueryFilter filter);

    /**
     * 根据查询的参数，分页对象，返回分页后的抄送任务项
     *
     * @param page   分页对象
     * @param filter 查询过滤器
     * @return List<WorkItem> 活动工作项
     */
    List<HisInstance> getCCWorks(Page<HisInstance> page, QueryFilter filter);

    /**
     * 根据流程定义ID、参与者分页查询已完成的历史任务项
     *
     * @param page   分页对象
     * @param filter 查询过滤器
     * @return List<WorkItem> 历史工作项
     */
    List<WorkItem> getHistoryWorkItems(Page<WorkItem> page, QueryFilter filter);

    /**
     * 根据类型clazz、Sql语句、参数查询单个对象
     *
     * @param clazz 类型
     * @param sql   sql语句
     * @param args  参数列表
     * @return 结果对象
     */
    <T> T queryObject(Class<T> clazz, String sql, Object... args);

    /**
     * 根据类型clazz、Sql语句、参数查询列表对象
     *
     * @param clazz 类型
     * @param sql   sql语句
     * @param args  参数列表
     * @return 结果对象列表
     */
    <T> List<T> queryList(Class<T> clazz, String sql, Object... args);

    /**
     * 根据类型clazz、Sql语句、参数分页查询列表对象
     *
     * @param page   分页对象
     * @param filter 查询过滤器
     * @param clazz  类型
     * @param sql    sql语句
     * @param args   参数列表
     * @return 结果对象列表
     */
    <T> List<T> queryList(Page<T> page, QueryFilter filter, Class<T> clazz, String sql, Object... args);

    /**
     * 运行脚本文件
     */
    void runScript();
}
