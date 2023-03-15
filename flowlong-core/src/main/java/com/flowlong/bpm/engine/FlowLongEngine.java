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

import com.flowlong.bpm.engine.core.Execution;
import com.flowlong.bpm.engine.core.FlowLongContext;
import com.flowlong.bpm.engine.entity.Instance;
import com.flowlong.bpm.engine.entity.Task;
import com.flowlong.bpm.engine.model.TaskModel;

import java.util.List;
import java.util.Map;

/**
 * FlowLong流程引擎接口
 *
 * <p>
 * 尊重知识产权，CV 请保留版权，爱组搭 http://aizuda.com 出品
 * </p>
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

    FlowLongContext getContext();

    /**
     * 获取process服务
     *
     * @return ProcessService 流程定义服务
     */
    default ProcessService processService() {
        return getContext().getProcessService();
    }

    /**
     * 获取查询服务
     *
     * @return QueryService 常用查询服务
     */
    default QueryService queryService() {
        return getContext().getQueryService();
    }

    /**
     * 获取实例服务
     *
     * @return QueryService 流程实例服务
     */
    default RuntimeService runtimeService() {
        return getContext().getRuntimeService();
    }

    /**
     * 获取任务服务
     *
     * @return TaskService 任务服务
     */
    default TaskService taskService() {
        return getContext().getTaskService();
    }

    /**
     * 获取管理服务
     *
     * @return ManagerService 管理服务
     */
    default ManagerService managerService() {
        return getContext().getManagerService();
    }

    /**
     * 根据流程定义ID启动流程实例
     *
     * @param id 流程定义ID
     * @return Instance 流程实例
     */
    Instance startInstanceById(Long id);

    /**
     * 根据流程定义ID，创建人ID启动流程实例
     *
     * @param id       流程定义ID
     * @param createBy 创建人ID
     * @return Instance 流程实例
     */
    Instance startInstanceById(Long id, String createBy);

    /**
     * 根据流程定义ID，创建人ID，参数列表启动流程实例
     *
     * @param id       流程定义ID
     * @param createBy 创建人ID
     * @param args     参数列表
     * @return Instance 流程实例
     */
    Instance startInstanceById(Long id, String createBy, Map<String, Object> args);

    /**
     * 根据流程定义ID，父流程实例ID，创建人ID，参数列表启动流程实例
     *
     * @param id       流程定义ID
     * @param createBy 创建人ID
     * @param args     参数列表
     * @param parentId 父流程实例ID
     * @param parentName 父流程实例名称
     * @return Instance 流程实例
     */
    Instance startInstanceByIdAndParentId(Long id, String createBy, Map<String, Object> args, Long parentId, String parentName);

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
     * 根据流程名称、版本号、创建人启动流程实例
     *
     * @param name     流程定义名称
     * @param version  版本号
     * @param createBy 创建人
     * @return Instance 流程实例
     */
    Instance startInstanceByName(String name, Integer version, String createBy);

    /**
     * 根据流程名称、版本号、创建人、参数列表启动流程实例
     *
     * @param name     流程定义名称
     * @param version  版本号
     * @param createBy 创建人
     * @param args     参数列表
     * @return Instance 流程实例
     */
    Instance startInstanceByName(String name, Integer version, String createBy, Map<String, Object> args);

    /**
     * 根据父执行对象启动子流程实例
     *
     * @param execution 执行对象
     * @return Instance 流程实例
     */
    Instance startInstanceByExecution(Execution execution);

    /**
     * 根据任务ID执行任务
     *
     * @param taskId 任务ID
     * @return List<Task> 任务集合
     */
    List<Task> executeTask(Long taskId);

    /**
     * 根据任务ID，创建人ID执行任务
     *
     * @param taskId   任务ID
     * @param createBy 创建人ID
     * @return List<Task> 任务集合
     */
    List<Task> executeTask(Long taskId, String createBy);

    /**
     * 根据任务ID，创建人ID，参数列表执行任务
     *
     * @param taskId   任务ID
     * @param createBy 创建人ID
     * @param args     参数列表
     * @return List<Task> 任务集合
     */
    List<Task> executeTask(Long taskId, String createBy, Map<String, Object> args);

    /**
     * 根据任务ID，创建人ID，参数列表执行任务，并且根据nodeName跳转到任意节点
     * 1、nodeName为null时，则跳转至上一步处理
     * 2、nodeName不为null时，则任意跳转，即动态创建转移
     *
     * @param taskId   任务ID
     * @param createBy 创建人ID
     * @param args     参数列表
     * @param nodeName 跳转的节点名称
     * @return List<Task> 任务集合
     */
    List<Task> executeAndJumpTask(Long taskId, String createBy, Map<String, Object> args, String nodeName);


    /**
     * 根据流程ID，创建人ID，参数列表结束当前任务并根据nodeName回退到历史节点
     * @param taskId 任务ID
     * @param createBy 创建人ID
     * @param args 参数列表
     * @param nodeName 节点名称
     * @return java.util.List<com.flowlong.bpm.engine.entity.Task>
     */
    List<Task> retreatTask(Long taskId, String createBy, Map<String, Object> args, String nodeName);

    /**
     * 根据流程实例ID，创建人ID，参数列表按照节点模型model创建新的自由任务
     *
     * @param instanceId 流程实例ID
     * @param createBy   创建人ID
     * @param args       参数列表
     * @param model      节点模型
     * @return List<Task> 任务集合
     */
    List<Task> createFreeTask(Long instanceId, String createBy, Map<String, Object> args, TaskModel model);
}
