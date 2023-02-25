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

import com.flowlong.bpm.engine.core.FlowLongContext;
import com.flowlong.bpm.engine.core.Execution;
import com.flowlong.bpm.engine.entity.Instance;
import com.flowlong.bpm.engine.entity.Task;
import com.flowlong.bpm.engine.model.TaskModel;

import java.util.List;
import java.util.Map;

/**
 * FlowLong流程引擎接口
 *
 * @author hubin
 * @since 1.0
 */
public interface FlowLongEngine {
    String ADMIN = "long.admin";
    String AUTO = "long.auto";
    String ID = "long.instanceNo";

    /**
     * 根据Configuration对象配置实现类
     *
     * @param config 全局配置对象
     * @return FlowLongEngine 流程引擎
     */
    FlowLongEngine configure(FlowLongContext config);

    /**
     * 获取process服务
     *
     * @return IProcessService 流程定义服务
     */
    IProcessService processService();

    /**
     * 获取查询服务
     *
     * @return IQueryService 常用查询服务
     */
    IQueryService queryService();

    /**
     * 获取实例服务
     *
     * @return IQueryService 流程实例服务
     */
    IRuntimeService runtimeService();

    /**
     * 获取任务服务
     *
     * @return ITaskService 任务服务
     */
    ITaskService taskService();

    /**
     * 获取管理服务
     *
     * @return IManagerService 管理服务
     */
    IManagerService managerService();

    /**
     * 根据流程定义ID启动流程实例
     *
     * @param id 流程定义ID
     * @return Instance 流程实例
     * @see #startInstanceById(String, String, Map)
     */
    Instance startInstanceById(String id);

    /**
     * 根据流程定义ID，操作人ID启动流程实例
     *
     * @param id       流程定义ID
     * @param operator 操作人ID
     * @return Instance 流程实例
     * @see #startInstanceById(String, String, Map)
     */
    Instance startInstanceById(String id, String operator);

    /**
     * 根据流程定义ID，操作人ID，参数列表启动流程实例
     *
     * @param id       流程定义ID
     * @param operator 操作人ID
     * @param args     参数列表
     * @return Instance 流程实例
     */
    Instance startInstanceById(String id, String operator, Map<String, Object> args);

    /**
     * 根据流程名称启动流程实例
     *
     * @param name 流程定义名称
     * @return Instance 流程实例
     */
    Instance startInstanceByName(String name);

    /**
     * 根据流程名称、版本号启动流程实例
     *
     * @param name    流程定义名称
     * @param version 版本号
     * @return Instance 流程实例
     */
    Instance startInstanceByName(String name, Integer version);

    /**
     * 根据流程名称、版本号、操作人启动流程实例
     *
     * @param name     流程定义名称
     * @param version  版本号
     * @param operator 操作人
     * @return Instance 流程实例
     */
    Instance startInstanceByName(String name, Integer version, String operator);

    /**
     * 根据流程名称、版本号、操作人、参数列表启动流程实例
     *
     * @param name     流程定义名称
     * @param version  版本号
     * @param operator 操作人
     * @param args     参数列表
     * @return Instance 流程实例
     */
    Instance startInstanceByName(String name, Integer version, String operator, Map<String, Object> args);

    /**
     * 根据父执行对象启动子流程实例
     *
     * @param execution 执行对象
     * @return Instance 流程实例
     */
    Instance startInstanceByExecution(Execution execution);

    /**
     * 根据任务主键ID执行任务
     *
     * @param taskId 任务主键ID
     * @return List<Task> 任务集合
     * @see #executeTask(String, String, Map)
     */
    List<Task> executeTask(String taskId);

    /**
     * 根据任务主键ID，操作人ID执行任务
     *
     * @param taskId   任务主键ID
     * @param operator 操作人主键ID
     * @return List<Task> 任务集合
     * @see #executeTask(String, String, Map)
     */
    List<Task> executeTask(String taskId, String operator);

    /**
     * 根据任务主键ID，操作人ID，参数列表执行任务
     *
     * @param taskId   任务主键ID
     * @param operator 操作人主键ID
     * @param args     参数列表
     * @return List<Task> 任务集合
     */
    List<Task> executeTask(String taskId, String operator, Map<String, Object> args);

    /**
     * 根据任务主键ID，操作人ID，参数列表执行任务，并且根据nodeName跳转到任意节点
     * 1、nodeName为null时，则跳转至上一步处理
     * 2、nodeName不为null时，则任意跳转，即动态创建转移
     *
     * @param taskId   任务主键ID
     * @param operator 操作人主键ID
     * @param args     参数列表
     * @param nodeName 跳转的节点名称
     * @return List<Task> 任务集合
     */
    List<Task> executeAndJumpTask(String taskId, String operator, Map<String, Object> args, String nodeName);

    /**
     * 根据流程实例ID，操作人ID，参数列表按照节点模型model创建新的自由任务
     *
     * @param instanceId  流程实例id
     * @param operator 操作人id
     * @param args     参数列表
     * @param model    节点模型
     * @return List<Task> 任务集合
     */
    List<Task> createFreeTask(String instanceId, String operator, Map<String, Object> args, TaskModel model);
}
