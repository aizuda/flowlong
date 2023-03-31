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
package com.flowlong.bpm.engine.core.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.flowlong.bpm.engine.Assignment;
import com.flowlong.bpm.engine.FlowLongEngine;
import com.flowlong.bpm.engine.TaskAccessStrategy;
import com.flowlong.bpm.engine.TaskService;
import com.flowlong.bpm.engine.assist.Assert;
import com.flowlong.bpm.engine.assist.DateUtils;
import com.flowlong.bpm.engine.assist.ObjectUtils;
import com.flowlong.bpm.engine.core.Execution;
import com.flowlong.bpm.engine.core.enums.PerformType;
import com.flowlong.bpm.engine.core.enums.TaskState;
import com.flowlong.bpm.engine.core.enums.TaskType;
import com.flowlong.bpm.engine.core.mapper.*;
import com.flowlong.bpm.engine.entity.Process;
import com.flowlong.bpm.engine.entity.*;
import com.flowlong.bpm.engine.exception.FlowLongException;
import com.flowlong.bpm.engine.listener.TaskListener;
import com.flowlong.bpm.engine.model.CustomModel;
import com.flowlong.bpm.engine.model.NodeModel;
import com.flowlong.bpm.engine.model.ProcessModel;
import com.flowlong.bpm.engine.model.TaskModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 任务执行业务类
 *
 * <p>
 * 尊重知识产权，CV 请保留版权，爱组搭 http://aizuda.com 出品
 * </p>
 *
 * @author hubin
 * @since 1.0
 */
@Service
public class TaskServiceImpl implements TaskService {
    private TaskAccessStrategy taskAccessStrategy;
    private ProcessMapper processMapper;
    private TaskListener taskListener;
    private InstanceMapper instanceMapper;
    private TaskMapper taskMapper;
    private TaskActorMapper taskActorMapper;
    private HisTaskMapper hisTaskMapper;
    private HisTaskActorMapper hisTaskActorMapper;

    public TaskServiceImpl(@Autowired(required = false) TaskAccessStrategy taskAccessStrategy, @Autowired(required = false) TaskListener taskListener,
                           ProcessMapper processMapper, InstanceMapper instanceMapper, TaskMapper taskMapper, TaskActorMapper taskActorMapper,
                           HisTaskMapper hisTaskMapper, HisTaskActorMapper hisTaskActorMapper) {
        this.taskAccessStrategy = taskAccessStrategy;
        this.processMapper = processMapper;
        this.taskListener = taskListener;
        this.instanceMapper = instanceMapper;
        this.taskMapper = taskMapper;
        this.taskActorMapper = taskActorMapper;
        this.hisTaskMapper = hisTaskMapper;
        this.hisTaskActorMapper = hisTaskActorMapper;
    }

    /**
     * 完成指定任务
     * 该方法仅仅结束活动任务，并不能驱动流程继续执行
     */
    @Override
    public Task complete(Long taskId, String userId, Map<String, Object> args) {
        Task task = taskMapper.selectById(taskId);
        Assert.notNull(task, "指定的任务[id=" + taskId + "]不存在");
        task.setVariable(args);
        Assert.isFalse(isAllowed(task, userId), "当前参与者 [" + userId + "]不允许执行任务[taskId=" + taskId + "]");

        // 迁移 task 信息到 flw_his_task
        HisTask hisTask = HisTask.of(task);
        hisTask.setFinishTime(DateUtils.getCurrentDate());
        hisTask.setTaskState(TaskState.finish);
        hisTask.setCreateBy(userId);
        hisTaskMapper.insert(hisTask);

        // 迁移任务参与者
        List<TaskActor> actors = taskActorMapper.selectList(Wrappers.<TaskActor>lambdaQuery().eq(TaskActor::getTaskId, taskId));
        if (ObjectUtils.isNotEmpty(actors)) {
            // 将 task 参与者信息迁移到 flw_his_task_actor
            actors.forEach(t -> hisTaskActorMapper.insert(HisTaskActor.of(t)));
            // 移除 flw_task_actor 中 task 参与者信息
            taskActorMapper.delete(Wrappers.<TaskActor>lambdaQuery().eq(TaskActor::getTaskId, taskId));
        }

        // 删除 flw_task 中指定 task 信息
        taskMapper.deleteById(taskId);

        // 任务监听器通知
        this.taskNotify(TaskListener.EVENT_COMPLETE, task);
        return task;
    }


    protected void taskNotify(String event, Task task) {
        if (null != taskListener) {
            taskListener.notify(event, task);
        }
    }

    /**
     * 更新任务对象的finish_Time、createBy、expire_Time、version、variable
     *
     * @param task 任务对象
     */
    @Override
    public void updateTaskById(Task task) {
        taskMapper.updateById(task);
        // 任务监听器通知
        this.taskNotify(TaskListener.EVENT_UPDATE, task);
    }

    /**
     * 任务设置超时
     *
     * @param taskId 任务ID
     */
    @Override
    public boolean taskTimeout(Long taskId) {
        Task task = taskMapper.selectById(taskId);
        if (null != task) {
            // 1，更新历史任务状态为超时，设置完成时间
            HisTask hisTask = new HisTask();
            hisTask.setId(taskId);
            hisTask.setTaskState(TaskState.timeout);
            hisTask.setFinishTime(DateUtils.getCurrentDate());
            hisTaskMapper.updateById(hisTask);

            // 2，删除任务
            taskMapper.deleteById(taskId);

            // 3，任务监听器通知
            this.taskNotify(TaskListener.EVENT_UPDATE, task);
        }
        return true;
    }

    /**
     * 任务历史记录方法
     *
     * @param execution 执行对象
     * @param model     自定义节点模型
     * @return 历史任务对象
     */
    @Override
    public HisTask history(Execution execution, CustomModel model) {
        HisTask hisTask = new HisTask();
        hisTask.setInstanceId(execution.getInstance().getId());
        hisTask.setCreateTime(DateUtils.getCurrentDate());
        hisTask.setFinishTime(hisTask.getCreateTime());
        hisTask.setDisplayName(model.getDisplayName());
        hisTask.setTaskName(model.getName());
        hisTask.setTaskState(TaskState.finish);
        hisTask.setTaskType(TaskType.record);
        hisTask.setParentTaskId(execution.getTask() == null ? 0L : execution.getTask().getId());
        hisTask.setVariable(execution.getArgs());
        hisTaskMapper.insert(hisTask);
        return hisTask;
    }

    /**
     * 根据 任务ID 认领任务，删除其它任务参与者
     *
     * @param taskId 任务ID
     * @param userId 用户ID
     * @return
     */
    @Override
    public Task claim(Long taskId, String userId) {
        Task task = taskMapper.selectById(taskId);
        Assert.notNull(task, "指定的任务[id=" + taskId + "]不存在");
        if (!isAllowed(task, userId)) {
            throw new FlowLongException("当前执行用户ID [" + userId + "] 不允许提取任务 [taskId=" + taskId + "]");
        }
        // 删除任务参与者
        taskActorMapper.delete(Wrappers.<TaskActor>lambdaQuery().eq(TaskActor::getTaskId, taskId));
        // 插入当前用户ID作为唯一参与者
        taskActorMapper.insert(TaskActor.of(taskId, userId));
        return task;
    }

    /**
     * 唤醒指定的历史任务
     */
    @Override
    public Task resume(Long taskId, String userId) {
        HisTask histTask = hisTaskMapper.selectById(taskId);
        Assert.notNull(histTask, "指定的历史任务[id=" + taskId + "]不存在");
        Assert.isTrue(ObjectUtils.isEmpty(histTask.getCreateBy()) || !Objects.equals(histTask.getCreateBy(), userId), "当前参与者[" + userId + "]不允许唤醒历史任务[taskId=" + taskId + "]");

        // 流程实例结束情况恢复流程实例
        Instance instance = instanceMapper.selectById(histTask.getInstanceId());
        Assert.isNull(instance, "已结束流程任务不支持唤醒");

        // 历史任务恢复
        Task task = histTask.undoTask();
        task.setCreateTime(DateUtils.getCurrentDate());
        taskMapper.insert(task);

        // 分配任务
        assignTask(task.getId(), userId);
        return task;
    }

    /**
     * 向指定的任务ID添加参与者
     *
     * @param taskId   任务ID
     * @param taskType 参与类型 {@link TaskType}
     * @param actors   参与者
     */
    @Override
    public void addTaskActor(Long taskId, TaskType taskType, List<String> actors) {
        Task task = taskMapper.selectById(taskId);
        Assert.notNull(task, "指定的任务[id=" + taskId + "]不存在");
        if (!task.major() || ObjectUtils.isEmpty(actors)) {
            return;
        }
        if (taskType == null) {
            taskType = TaskType.get(task.getPerformType());
        }
        if (taskType == TaskType.major) {
            /**
             * 普通任务
             */
            actors.forEach(t -> this.assignTask(task.getId(), t));
            Map<String, Object> data = task.variableMap();
            String oldActor = (String) data.get(Task.KEY_ACTOR);
            data.put(Task.KEY_ACTOR, oldActor + "," + actors.stream().collect(Collectors.joining(",")));
            task.setVariable(data);
            taskMapper.updateById(task);
        } else if (taskType == TaskType.countersign) {
            /**
             * 会签任务
             */
            try {
                for (String actor : actors) {
                    Task newTask = task.cloneTask(actor);
                    taskMapper.insert(newTask);
                    this.assignTask(newTask.getId(), actor);
                }
            } catch (CloneNotSupportedException ex) {
                throw new FlowLongException("任务对象不支持复制", ex.getCause());
            }
        }
    }

    /**
     * 撤回指定的任务
     */
    @Override
    public Task withdrawTask(Long taskId, String createBy) {
        HisTask hist = hisTaskMapper.selectById(taskId);
        Assert.notNull(hist, "指定的历史任务[id=" + taskId + "]不存在");
        List<Task> tasks = new ArrayList<>();
        PerformType performType = PerformType.get(hist.getPerformType());
        if (performType == PerformType.any) {
            // 根据父任务ID查询所有子任务
            tasks = taskMapper.selectList(Wrappers.<Task>lambdaQuery().eq(Task::getParentTaskId, hist.getId()));
        } else {
            List<Long> hisTaskIds = hisTaskMapper.selectList(Wrappers.<HisTask>lambdaQuery().eq(HisTask::getInstanceId, hist.getInstanceId()).eq(HisTask::getTaskName, hist.getTaskName()).eq(HisTask::getParentTaskId, hist.getParentTaskId())).stream().map(HisTask::getId).collect(Collectors.toList());
            if (!hisTaskIds.isEmpty()) {
                tasks = taskMapper.selectList(Wrappers.<Task>lambdaQuery().in(Task::getParentTaskId, hisTaskIds));
            }
        }
        if (tasks == null || tasks.isEmpty()) {
            throw new FlowLongException("后续活动任务已完成或不存在，无法撤回.");
        }
        List<Long> taskIds = tasks.stream().map(BaseEntity::getId).collect(Collectors.toList());
        // 查询任务参与者
        List<Long> taskActorIds = taskActorMapper.selectList(Wrappers.<TaskActor>lambdaQuery().in(TaskActor::getTaskId, taskIds)).stream().map(TaskActor::getId).collect(Collectors.toList());
        if (!taskActorIds.isEmpty()) {
            taskActorMapper.deleteBatchIds(taskActorIds);
        }
        taskMapper.deleteBatchIds(tasks.stream().map(BaseEntity::getId).collect(Collectors.toList()));

        Task task = hist.undoTask();
        task.setCreateTime(DateUtils.getCurrentDate());
        taskMapper.insert(task);
        assignTask(task.getId(), task.getCreateBy());
        return task;
    }

    /**
     * 驳回任务
     */
    @Override
    public Task rejectTask(ProcessModel model, Task currentTask) {
        Long parentTaskId = currentTask.getParentTaskId();
        if (Objects.equals(parentTaskId, 0L)) {
            throw new FlowLongException("上一步任务ID为空，无法驳回至上一步处理");
        }
        NodeModel current = model.getNode(currentTask.getTaskName());
        HisTask history = hisTaskMapper.selectById(parentTaskId);
        NodeModel parent = model.getNode(history.getTaskName());
        if (!NodeModel.canRejected(current, parent)) {
            throw new FlowLongException("无法驳回至上一步处理，请确认上一步骤并非fork、join、suprocess以及会签任务");
        }

        Task task = history.undoTask();
        task.setCreateTime(DateUtils.getCurrentDate());
        task.setCreateBy(history.getCreateBy());
        taskMapper.insert(task);
        assignTask(task.getId(), task.getCreateBy());
        return task;
    }

    /**
     * 对指定的任务分配参与者。参与者可以为用户、部门、角色
     *
     * @param taskId  任务ID
     * @param actorId 参与者ID
     */
    private void assignTask(Long taskId, String actorId) {
        taskActorMapper.insert(TaskActor.of(taskId, actorId));
    }

    /**
     * 根据已有任务、任务类型、参与者创建新的任务
     * 适用于转派，动态协办处理
     */
    @Override
    public List<Task> createNewTask(Long taskId, TaskType taskType, List<String> actors) {
        Assert.isTrue(ObjectUtils.isEmpty(actors), "参与者不能为空");
        Task task = taskMapper.selectById(taskId);
        Assert.notNull(task, "指定的任务[id=" + taskId + "]不存在");
        List<Task> tasks = new ArrayList<>();
        try {
            Task newTask = task.cloneTask(null);
            newTask.setTaskType(taskType);
            newTask.setParentTaskId(taskId);
            tasks.add(this.saveTask(newTask, PerformType.any, actors));
        } catch (CloneNotSupportedException e) {
            throw new FlowLongException("任务对象不支持复制", e.getCause());
        }
        return tasks;
    }

    /**
     * 获取超时或者提醒的任务
     *
     * @return List<Task> 任务列表
     */
    @Override
    public List<Task> getTimeoutOrRemindTasks() {
        Date currentDate = DateUtils.getCurrentDate();
        return taskMapper.selectList(Wrappers.<Task>lambdaQuery().le(Task::getExpireTime, currentDate).or().le(Task::getRemindTime, currentDate));
    }

    /**
     * 获取任务模型
     *
     * @param taskId 任务ID
     * @return TaskModel
     */
    @Override
    public TaskModel getTaskModel(String taskId) {
        Task task = taskMapper.selectById(taskId);
        Assert.notNull(task);
        Instance instance = instanceMapper.selectById(task.getInstanceId());
        Assert.notNull(instance);
        Process process = processMapper.selectById(instance.getProcessId());
        ProcessModel model = process.getProcessModel();
        NodeModel nodeModel = model.getNode(task.getTaskName());
        Assert.notNull(nodeModel, "任务ID无法找到节点模型.");
        if (nodeModel instanceof TaskModel) {
            return (TaskModel) nodeModel;
        } else {
            throw new IllegalArgumentException("任务ID找到的节点模型不匹配");
        }
    }

    /**
     * 创建 task 根据 model 决定是否分配参与者
     *
     * @param taskModel 模型
     * @param execution 执行对象
     * @return 任务列表
     */
    @Override
    public List<Task> createTask(TaskModel taskModel, Execution execution) {
        Map<String, Object> args = execution.getArgs();
        // 执行参数中获取失效时间，提醒时间
        Date expireTime = DateUtils.processTime(args, taskModel.getExpireTime());
        Date remindTime = DateUtils.processTime(args, taskModel.getReminderTime());
        String form = (String) args.get(taskModel.getForm());
        String actionUrl = ObjectUtils.isEmpty(form) ? taskModel.getForm() : form;

        // 执行任务
        Task task = this.createTaskBase(taskModel, execution);
        task.setActionUrl(actionUrl);
        task.setExpireTime(expireTime);

        // 模型中获取参与者信息
        List<String> actors = this.getTaskActors(taskModel, execution);
        if (ObjectUtils.isNotEmpty(actors)) {
            args.put(Task.KEY_ACTOR, actors.stream().collect(Collectors.joining(",")));
        }
        task.setVariable(args);

        List<Task> tasks = new LinkedList<>();
        if (taskModel.isPerformAny()) {
            // 任务执行方式为参与者中任何一个执行即可驱动流程继续流转，该方法只产生一个task
            task = this.saveTask(task, PerformType.any, actors);
            task.setRemindTime(remindTime);
            tasks.add(task);
        } else if (taskModel.isPerformAll()) {
            // 任务执行方式为参与者中每个都要执行完才可驱动流程继续流转，该方法根据参与者个数产生对应的task数量
            for (String actor : actors) {
                Task singleTask;
                try {
                    singleTask = task.cloneTask(actor);
                } catch (CloneNotSupportedException e) {
                    singleTask = task;
                }
                singleTask = this.saveTask(singleTask, PerformType.all, Collections.singletonList(actor));
                singleTask.setRemindTime(remindTime);
                tasks.add(singleTask);
            }
        } else if (taskModel.isPerformPercentage()) {
            // 任务执行方式为参与者中执行完数/总参与者数 >= 通过百分比才可驱动流程继续流转，该方法根据参与者个数产生对应的task数量
            for (String actor : actors) {
                Task singleTask;
                try {
                    singleTask = task.cloneTask(actor);
                } catch (CloneNotSupportedException e) {
                    singleTask = task;
                }
                singleTask.setId(null);
                singleTask = this.saveTask(singleTask, PerformType.percentage, Collections.singletonList(actor));
                singleTask.setRemindTime(remindTime);
                tasks.add(singleTask);
            }
        }
        return tasks;
    }

    /**
     * 根据模型、执行对象、任务类型构建基本的task对象
     *
     * @param model     模型
     * @param execution 执行对象
     * @return Task任务对象
     */
    private Task createTaskBase(TaskModel model, Execution execution) {
        Task task = new Task();
        task.setCreateBy(execution.getCreateBy());
        task.setInstanceId(execution.getInstance().getId());
        task.setTaskName(model.getName());
        task.setDisplayName(model.getDisplayName());
        task.setCreateTime(DateUtils.getCurrentDate());
        if (model.isMajor()) {
            task.setTaskType(TaskType.major);
        } else {
            task.setTaskType(TaskType.assist);
        }
        task.setParentTaskId(execution.getTask() == null ? 0L : execution.getTask().getId());
        return task;
    }

    /**
     * 保存任务及参与者信息
     *
     * @param task   任务对象
     * @param actors 参与者ID集合
     * @return
     */
    private Task saveTask(Task task, PerformType performType, List<String> actors) {
        task.setPerformType(performType.ordinal());
        taskMapper.insert(task);
        if (ObjectUtils.isNotEmpty(actors)) {
            actors.forEach(t -> this.assignTask(task.getId(), t));
        }
        return task;
    }

    /**
     * 根据Task模型的assignee、assignmentHandler属性以及运行时数据，确定参与者
     *
     * @param model     模型
     * @param execution 执行对象
     * @return 参与者数组
     */
    private List<String> getTaskActors(TaskModel model, Execution execution) {
        Object assigneeObject = null;
        Assignment handler = model.getAssignmentHandlerObject();
        if (ObjectUtils.isNotEmpty(model.getAssignee())) {
            assigneeObject = execution.getArgs().get(model.getAssignee());
        } else if (null != handler) {
            if (handler instanceof Assignment) {
                assigneeObject = handler.assign(model, execution);
            } else {
                assigneeObject = handler.assign(execution);
            }
        }
        return this.getTaskActors(null == assigneeObject ? model.getAssignee() : assigneeObject);
    }

    /**
     * 根据 task model 指定的 assignee 属性，从args中取值
     * 将取到的值处理为String[]类型。
     *
     * @param actors 参与者对象
     * @return 参与者数组
     */
    private List<String> getTaskActors(Object actors) {
        if (null == actors) {
            return null;
        }
        if (actors instanceof String) {
            return Arrays.asList(((String) actors).split(","));
        } else if (actors instanceof List) {
            return (List<String>) actors;
        } else if (actors instanceof Long) {
            return Arrays.asList(String.valueOf(actors));
        } else if (actors instanceof Integer) {
            return Arrays.asList(String.valueOf(actors));
        } else if (actors instanceof String[]) {
            return Arrays.asList((String[]) actors);
        } else {
            throw new FlowLongException("任务参与者对象[" + actors + "]类型不支持");
        }
    }

    /**
     * 根据 taskId、createBy 判断创建人createBy是否允许执行任务
     *
     * @param task   任务对象
     * @param userId 用户ID
     * @return
     */
    @Override
    public boolean isAllowed(Task task, String userId) {
        if (ObjectUtils.isEmpty(userId)) {
            // 任务执行创建人不存在
            return false;
        }
        // 如果是admin或者auto，直接返回true
        if (FlowLongEngine.ADMIN.equalsIgnoreCase(userId) || FlowLongEngine.AUTO.equalsIgnoreCase(userId)) {
            return true;
        }
        // 任务参与者列表
        List<TaskActor> actors = taskActorMapper.selectList(Wrappers.<TaskActor>lambdaQuery().eq(TaskActor::getTaskId, task.getId()));
        if (actors == null || actors.isEmpty()) {
            // 未设置参与者，默认返回 true
            return true;
        }
        return taskAccessStrategy.isAllowed(userId, actors);
    }


    @Override
    public void removeTaskActor(Long taskId, String... actors) {
        Task task = taskMapper.selectById(taskId);
        Assert.notNull(task, "指定的任务[id=" + taskId + "]不存在");
        if (actors == null || actors.length == 0) {
            return;
        }
        if (task.major()) {
            Map<String, Object> taskData = task.variableMap();
            String actorStr = (String) taskData.get(Task.KEY_ACTOR);
            if (ObjectUtils.isNotEmpty(actorStr)) {
                String[] actorArray = actorStr.split(",");
                StringBuilder newActor = new StringBuilder(actorStr.length());
                boolean isMatch;
                for (String actor : actorArray) {
                    isMatch = false;
                    if (ObjectUtils.isEmpty(actor)) {
                        continue;
                    }
                    for (String removeActor : actors) {
                        if (actor.equals(removeActor)) {
                            isMatch = true;
                            break;
                        }
                    }
                    if (isMatch) {
                        continue;
                    }
                    newActor.append(actor).append(",");
                }
                if (newActor.length() > 0) {
                    newActor.deleteCharAt(newActor.length() - 1);
                }
                taskData.put(Task.KEY_ACTOR, newActor.toString());
                task.setVariable(taskData);
                taskMapper.updateById(task);
            }
        }
    }

    /**
     * 级联删除 flw_his_task, flw_his_task_actor, flw_task, flw_task_actor
     *
     * @param instanceId 流程实例ID
     */
    @Override
    public void cascadeRemoveByInstanceId(Long instanceId) {
        // 删除历史任务及参与者
        List<HisTask> hisTaskList = hisTaskMapper.selectList(Wrappers.<HisTask>lambdaQuery().select(HisTask::getId).eq(HisTask::getInstanceId, instanceId));
        if (ObjectUtils.isNotEmpty(hisTaskList)) {
            List<Long> hisTaskIds = hisTaskList.stream().map(t -> t.getId()).collect(Collectors.toList());
            hisTaskActorMapper.delete(Wrappers.<HisTaskActor>lambdaQuery().in(HisTaskActor::getTaskId, hisTaskIds));
            hisTaskMapper.delete(Wrappers.<HisTask>lambdaQuery().eq(HisTask::getInstanceId, instanceId));
        }

        // 删除任务及参与者
        List<Task> taskList = taskMapper.selectList(Wrappers.<Task>lambdaQuery().select(Task::getId).eq(Task::getInstanceId, instanceId));
        if (ObjectUtils.isNotEmpty(taskList)) {
            List<Long> taskIds = taskList.stream().map(t -> t.getId()).collect(Collectors.toList());
            taskActorMapper.delete(Wrappers.<TaskActor>lambdaQuery().in(TaskActor::getTaskId, taskIds));
            taskMapper.delete(Wrappers.<Task>lambdaQuery().eq(Task::getInstanceId, instanceId));
        }
    }
}
