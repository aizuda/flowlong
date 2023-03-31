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
import com.flowlong.bpm.engine.core.enums.TaskType;
import com.flowlong.bpm.engine.entity.HisTask;
import com.flowlong.bpm.engine.entity.Task;
import com.flowlong.bpm.engine.model.CustomModel;
import com.flowlong.bpm.engine.model.ProcessModel;
import com.flowlong.bpm.engine.model.TaskModel;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * 任务业务类，包括以下服务：
 * 1、创建任务
 * 2、添加、删除参与者
 * 3、完成任务
 * 4、撤回任务
 * 5、回退任务
 * 6、提取任务
 *
 * <p>
 * 尊重知识产权，CV 请保留版权，爱组搭 http://aizuda.com 出品
 * </p>
 *
 * @author hubin
 * @since 1.0
 */
public interface TaskService {

    /**
     * 完成指定的任务，删除活动任务记录，创建历史任务
     *
     * @param taskId 任务ID
     * @return Task 任务对象
     */
    default Task complete(Long taskId) {
        return this.complete(taskId, null);
    }

    /**
     * 完成指定的任务，删除活动任务记录，创建历史任务
     *
     * @param taskId 任务ID
     * @param userId 用户ID
     * @return Task 任务对象
     */
    default Task complete(Long taskId, String userId) {
        return this.complete(taskId, userId, null);
    }

    /**
     * 根据任务ID，创建人ID完成任务
     *
     * @param taskId 任务ID
     * @param userId 用户ID
     * @param args   参数集合
     * @return Task 任务对象
     */
    Task complete(Long taskId, String userId, Map<String, Object> args);

    /**
     * 更新任务对象
     *
     * @param task 任务对象
     */
    void updateTaskById(Task task);

    /**
     * 任务设置超时
     *
     * @param taskId 任务ID
     */
    boolean taskTimeout(Long taskId);

    /**
     * 根据执行对象、自定义节点模型创建历史任务记录
     *
     * @param execution 执行对象
     * @param model     自定义节点模型
     * @return 历史任务
     */
    HisTask history(Execution execution, CustomModel model);

    /**
     * 根据 任务ID 认领任务，删除其它任务参与者
     *
     * @param taskId 任务ID
     * @param userId 用户ID
     * @return Task 任务对象
     */
    Task claim(Long taskId, String userId);

    /**
     * 唤醒历史任务
     * <p>
     * 该方法会导致流程状态不可控，请慎用
     * </p>
     *
     * @param taskId 历史任务ID
     * @param userId 用户ID
     * @return {@link Task} 唤醒后的任务对象
     */
    Task resume(Long taskId, String userId);

    /**
     * 向指定的任务ID添加参与者
     *
     * @param taskId 任务ID
     * @param actors 参与者
     */
    default void addTaskActor(Long taskId, List<String> actors) {
        this.addTaskActor(taskId, null, actors);
    }

    default void addTaskActor(Long taskId, String actor) {
        this.addTaskActor(taskId, Arrays.asList(actor));
    }

    /**
     * 向指定的任务ID添加参与者
     *
     * @param taskId   任务ID
     * @param taskType 参与类型 {@link TaskType}
     * @param actors   参与者
     */
    void addTaskActor(Long taskId, TaskType taskType, List<String> actors);

    default void addTaskActor(Long taskId, TaskType taskType, String actor) {
        this.addTaskActor(taskId, taskType, Arrays.asList(actor));
    }

    /**
     * 根据任务ID、创建人撤回任务
     *
     * @param taskId   任务ID
     * @param createBy 创建人
     * @return Task 任务对象
     */
    Task withdrawTask(Long taskId, String createBy);

    /**
     * 根据当前任务对象驳回至上一步处理
     *
     * @param model       流程定义模型，用以获取上一步模型对象
     * @param currentTask 当前任务对象
     * @return Task 任务对象
     */
    Task rejectTask(ProcessModel model, Task currentTask);

    /**
     * 根据 taskId、createBy 判断创建人createBy是否允许执行任务
     *
     * @param task   任务对象
     * @param userId 用户ID
     * @return boolean 是否允许操作
     */
    boolean isAllowed(Task task, String userId);

    /**
     * 根据任务模型、执行对象创建新的任务
     *
     * @param taskModel 任务模型
     * @param execution 执行对象
     * @return List<Task> 创建任务集合
     */
    List<Task> createTask(TaskModel taskModel, Execution execution);

    /**
     * 根据已有任务ID、任务类型、参与者创建新的任务
     *
     * @param taskId   主办任务ID
     * @param taskType 任务类型 {@link TaskType}
     * @param actors   参与者集合
     * @return List<Task> 创建任务集合
     */
    List<Task> createNewTask(Long taskId, TaskType taskType, List<String> actors);

    default List<Task> createNewTask(Long taskId, TaskType taskType, String actor) {
        return this.createNewTask(taskId, taskType, Arrays.asList(actor));
    }

    /**
     * 获取超时或者提醒的任务
     *
     * @return List<Task> 任务列表
     */
    List<Task> getTimeoutOrRemindTasks();

    /**
     * 根据任务ID获取任务模型
     *
     * @param taskId 任务ID
     * @return
     */
    TaskModel getTaskModel(String taskId);

    /**
     * 对指定的任务ID删除参与者
     *
     * @param taskId 任务ID
     * @param actors 参与者
     */
    boolean removeTaskActor(Long taskId, List<String> actors);

    default boolean removeTaskActor(Long taskId, String actor) {
        return removeTaskActor(taskId, Arrays.asList(actor));
    }

    /**
     * 级联删除 flw_his_task, flw_his_task_actor, flw_task, flw_task_actor
     *
     * @param instanceId 流程实例ID
     */
    void cascadeRemoveByInstanceId(Long instanceId);
}
