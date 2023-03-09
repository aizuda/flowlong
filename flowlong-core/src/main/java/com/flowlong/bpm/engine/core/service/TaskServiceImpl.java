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
import com.flowlong.bpm.engine.assist.StringUtils;
import com.flowlong.bpm.engine.core.Execution;
import com.flowlong.bpm.engine.core.FlowLongContext;
import com.flowlong.bpm.engine.core.enums.InstanceState;
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

    public TaskServiceImpl(@Autowired(required = false) TaskAccessStrategy taskAccessStrategy,
                           @Autowired(required = false) TaskListener taskListener, ProcessMapper processMapper, InstanceMapper instanceMapper,
                           TaskMapper taskMapper, TaskActorMapper taskActorMapper, HisTaskMapper hisTaskMapper, HisTaskActorMapper hisTaskActorMapper) {
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
     */
    @Override
    public Task complete(Long taskId) {
        return complete(taskId, null, null);
    }

    /**
     * 完成指定任务
     */
    @Override
    public Task complete(Long taskId, String createBy) {
        return complete(taskId, createBy, null);
    }

    /**
     * 完成指定任务
     * 该方法仅仅结束活动任务，并不能驱动流程继续执行
     */
    @Override
    public Task complete(Long taskId, String createBy, Map<String, Object> args) {
        Task task = taskMapper.selectById(taskId);
        Assert.notNull(task, "指定的任务[id=" + taskId + "]不存在");
        task.setVariable(FlowLongContext.JSON_HANDLER.toJson(args));
        if (!isAllowed(task, createBy)) {
            throw new FlowLongException("当前参与者[" + createBy + "]不允许执行任务[taskId=" + taskId + "]");
        }
        HisTask history = new HisTask(task);
        history.setFinishTime(new Date());
        history.setTaskState(InstanceState.finish);
        history.setCreateBy(createBy);

        List<HisTaskActor> hisTaskActors = new ArrayList<>();
        if (history.actorIds() == null) {
            List<TaskActor> actors = taskActorMapper.selectList(Wrappers.<TaskActor>lambdaQuery().eq(TaskActor::getTaskId, taskId));
            for (TaskActor actor : actors) {
                hisTaskActors.add(new HisTaskActor(actor));
            }
        }
        // 迁移 task 信息到 flw_his_task
        hisTaskMapper.insert(history);
        if (hisTaskActors.size() > 0) {
            // 将 task 参与者信息迁移到 flw_his_task_actor
            hisTaskActorMapper.insertBatchSomeColumn(hisTaskActors);
            // 移除 flw_task_actor 中 task 参与者信息
            taskActorMapper.delete(Wrappers.<TaskActor>lambdaQuery().eq(TaskActor::getTaskId, taskId));
        }
        // 删除 flw_task 中指定 task 信息
        taskMapper.deleteById(task);
        if (null != taskListener) {
            taskListener.notify(TaskListener.EVENT_COMPLETE, history);
        }
        return task;
    }

    /**
     * 更新任务对象的finish_Time、createBy、expire_Time、version、variable
     *
     * @param task 任务对象
     */
    @Override
    public void updateTask(Task task) {
        taskMapper.updateById(task);
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
        hisTask.setCreateTime(new Date());
        hisTask.setFinishTime(hisTask.getCreateTime());
        hisTask.setDisplayName(model.getDisplayName());
        hisTask.setTaskName(model.getName());
        hisTask.setTaskState(InstanceState.finish);
        hisTask.setTaskType(TaskModel.TaskType.Record.ordinal());
        hisTask.setParentTaskId(execution.getTask() == null ? 0L : execution.getTask().getId());
        hisTask.setVariable(FlowLongContext.JSON_HANDLER.toJson(execution.getArgs()));
        hisTaskMapper.insert(hisTask);
        return hisTask;
    }

    /**
     * 提取指定任务，设置完成时间及创建人，状态不改变
     */
    @Override
    public Task take(Long taskId, String createBy) {
        Task task = taskMapper.selectById(taskId);
        Assert.notNull(task, "指定的任务[id=" + taskId + "]不存在");
        if (!isAllowed(task, createBy)) {
            throw new FlowLongException("当前参与者[" + createBy + "]不允许提取任务[taskId=" + taskId + "]");
        }
        Task newTask = new Task();
        newTask.setId(taskId);
        newTask.setCreateBy(createBy);
        newTask.setFinishTime(new Date());
        taskMapper.updateById(newTask);
        return task;
    }

    /**
     * 唤醒指定的历史任务
     */
    @Override
    public Task resume(Long taskId, String createBy) {
        HisTask histTask = hisTaskMapper.selectById(taskId);
        Assert.notNull(histTask, "指定的历史任务[id=" + taskId + "]不存在");
        boolean isAllowed = true;
        if (StringUtils.isNotEmpty(histTask.getCreateBy())) {
            isAllowed = histTask.getCreateBy().equals(createBy);
        }
        if (isAllowed) {
            Task task = histTask.undoTask();
            task.setCreateTime(new Date());
            taskMapper.insert(task);
            assignTask(task.getId(), task.getCreateBy());
            return task;
        } else {
            throw new FlowLongException("当前参与者[" + createBy + "]不允许唤醒历史任务[taskId=" + taskId + "]");
        }
    }

    /**
     * 向指定任务添加参与者
     */
    @Override
    public void addTaskActor(Long taskId, String... actors) {
        addTaskActor(taskId, null, actors);
    }

    /**
     * 向指定任务添加参与者
     * 该方法根据performType类型判断是否需要创建新的活动任务
     */
    @Override
    public void addTaskActor(Long taskId, Integer performType, String... actors) {
        Task task = taskMapper.selectById(taskId);
        Assert.notNull(task, "指定的任务[id=" + taskId + "]不存在");
        if (!task.major()) {
            return;
        }
        if (performType == null) {
            performType = task.getPerformType();
        }
        if (performType == null) {
            performType = 0;
        }
        switch (performType) {
            case 0:
                assignTask(task.getId(), actors);
                Map<String, Object> data = task.variableMap();
                String oldActor = (String) data.get(Task.KEY_ACTOR);
                data.put(Task.KEY_ACTOR, oldActor + "," + StringUtils.getStringByArray(actors));
                task.setVariable(FlowLongContext.JSON_HANDLER.toJson(data));
                taskMapper.updateById(task);
                break;
            case 1:
                try {
                    for (String actor : actors) {
                        Task newTask = (Task) task.clone();
                        newTask.setCreateTime(new Date());
                        newTask.setCreateBy(actor);
                        Map<String, Object> taskData = task.variableMap();
                        taskData.put(Task.KEY_ACTOR, actor);
                        task.setVariable(FlowLongContext.JSON_HANDLER.toJson(taskData));
                        taskMapper.insert(newTask);
                        assignTask(newTask.getId(), actor);
                    }
                } catch (CloneNotSupportedException ex) {
                    throw new FlowLongException("任务对象不支持复制", ex.getCause());
                }
                break;
            default:
                break;
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
        if (hist.isPerformAny()) {
            // 根据父任务id查询所有子任务
            tasks = taskMapper.selectList(Wrappers.<Task>lambdaQuery().eq(Task::getParentTaskId, hist.getId()));
        } else {
            List<Long> hisTaskIds = hisTaskMapper.selectList(Wrappers.<HisTask>lambdaQuery()
                    .eq(HisTask::getInstanceId, hist.getInstanceId())
                    .eq(HisTask::getTaskName, hist.getTaskName())
                    .eq(HisTask::getParentTaskId, hist.getParentTaskId()))
                    .stream().map(HisTask::getId).collect(Collectors.toList());
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
        task.setCreateTime(new Date());
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
        task.setCreateTime(new Date());
        task.setCreateBy(history.getCreateBy());
        taskMapper.insert(task);
        assignTask(task.getId(), task.getCreateBy());
        return task;
    }

    /**
     * 对指定的任务分配参与者。参与者可以为用户、部门、角色
     *
     * @param taskId   任务id
     * @param actorIds 参与者id集合
     */
    private void assignTask(Long taskId, String... actorIds) {
        if (actorIds == null || actorIds.length == 0) {
            return;
        }
        for (String actorId : actorIds) {
            //修复当actorId为null的bug
            if (StringUtils.isEmpty(actorId)) {
                continue;
            }
            TaskActor taskActor = new TaskActor();
            taskActor.setTaskId(taskId);
            taskActor.setActorId(actorId);
            taskActorMapper.insert(taskActor);
        }
    }

    /**
     * 根据已有任务、任务类型、参与者创建新的任务
     * 适用于转派，动态协办处理
     */
    @Override
    public List<Task> createNewTask(Long taskId, int taskType, String... actors) {
        Task task = taskMapper.selectById(taskId);
        Assert.notNull(task, "指定的任务[id=" + taskId + "]不存在");
        List<Task> tasks = new ArrayList<Task>();
        try {
            Task newTask = (Task) task.clone();
            newTask.setId(null);
            newTask.setTaskType(taskType);
            newTask.setCreateTime(new Date());
            newTask.setParentTaskId(taskId);
            tasks.add(saveTask(newTask, TaskModel.PerformType.ANY, actors));
        } catch (CloneNotSupportedException e) {
            throw new FlowLongException("任务对象不支持复制", e.getCause());
        }
        return tasks;
    }

    /**
     * 获取任务模型
     *
     * @param taskId 任务id
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
        Assert.notNull(nodeModel, "任务id无法找到节点模型.");
        if (nodeModel instanceof TaskModel) {
            return (TaskModel) nodeModel;
        } else {
            throw new IllegalArgumentException("任务id找到的节点模型不匹配");
        }
    }

    /**
     * 由DBAccess实现类创建task，并根据model类型决定是否分配参与者
     *
     * @param taskModel 模型
     * @param execution 执行对象
     * @return List<Task> 任务列表
     */
    @Override
    public List<Task> createTask(TaskModel taskModel, Execution execution) {
        List<Task> tasks = new ArrayList<>();

        Map<String, Object> args = execution.getArgs();
        if (args == null) {
            args = new HashMap<>();
        }
        Date expireDate = DateUtils.processTime(args, taskModel.getExpireTime());
        Date remindDate = DateUtils.processTime(args, taskModel.getReminderTime());
        String form = (String) args.get(taskModel.getForm());
        String actionUrl = StringUtils.isEmpty(form) ? taskModel.getForm() : form;

        String[] actors = getTaskActors(taskModel, execution);

        args.put(Task.KEY_ACTOR, StringUtils.getStringByArray(actors));
        Task task = createTaskBase(taskModel, execution);
        task.setActionUrl(actionUrl);
        task.setExpireTime(expireDate);
        task.setVariable(FlowLongContext.JSON_HANDLER.toJson(args));

        if (taskModel.isPerformAny()) {
            //任务执行方式为参与者中任何一个执行即可驱动流程继续流转，该方法只产生一个task
            task = saveTask(task,TaskModel.PerformType.ANY, actors);
//            task.setRemindDate(remindDate);
            tasks.add(task);
        } else if (taskModel.isPerformAll()) {
            //任务执行方式为参与者中每个都要执行完才可驱动流程继续流转，该方法根据参与者个数产生对应的task数量
            for (String actor : actors) {
                Task singleTask;
                try {
                    singleTask = (Task) task.clone();
                } catch (CloneNotSupportedException e) {
                    singleTask = task;
                }
                singleTask = saveTask(singleTask,TaskModel.PerformType.ALL, actor);
//                singleTask.setRemindDate(remindDate);
                tasks.add(singleTask);
            }
        } else if (taskModel.isPerformPercentage()) {
            //任务执行方式为参与者中执行完数/总参与者数 >= 通过百分比才可驱动流程继续流转，该方法根据参与者个数产生对应的task数量
            for (String actor : actors) {
                Task singleTask;
                try {
                    singleTask = (Task) task.clone();
                } catch (CloneNotSupportedException e) {
                    singleTask = task;
                }
                singleTask = saveTask(singleTask,TaskModel.PerformType.PERCENTAGE, actor);
//                singleTask.setRemindDate(remindDate);
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
        task.setCreateTime(new Date());
        if (model.isMajor()) {
            task.setTaskType(TaskModel.TaskType.Major.ordinal());
        } else {
            task.setTaskType(TaskModel.TaskType.Aidant.ordinal());
        }
        task.setParentTaskId(execution.getTask() == null ? 0L : execution.getTask().getId());
//        task.setTaskModel(model);
        return task;
    }

    /**
     * 由DBAccess实现类持久化task对象
     */
    private Task saveTask(Task task,TaskModel.PerformType performType, String... actors) {
        task.setPerformType(performType.ordinal());
        taskMapper.insert(task);
        assignTask(task.getId(), actors);
//        task.setActorIds(actors);
        return task;
    }

    /**
     * 根据Task模型的assignee、assignmentHandler属性以及运行时数据，确定参与者
     *
     * @param model     模型
     * @param execution 执行对象
     * @return 参与者数组
     */
    private String[] getTaskActors(TaskModel model, Execution execution) {
        Object assigneeObject = null;
        Assignment handler = model.getAssignmentHandlerObject();
        if (StringUtils.isNotEmpty(model.getAssignee())) {
            assigneeObject = execution.getArgs().get(model.getAssignee());
        } else if (handler != null) {
            if (handler instanceof Assignment) {
                assigneeObject = handler.assign(model, execution);
            } else {
                assigneeObject = handler.assign(execution);
            }
        }
        return getTaskActors(assigneeObject == null ? model.getAssignee() : assigneeObject);
    }

    /**
     * 根据taskmodel指定的assignee属性，从args中取值
     * 将取到的值处理为String[]类型。
     *
     * @param actors 参与者对象
     * @return 参与者数组
     */
    private String[] getTaskActors(Object actors) {
        if (actors == null){
            return null;
        }
        String[] results;
        if (actors instanceof String) {
            //如果值为字符串类型，则使用逗号,分隔
            return ((String) actors).split(",");
        } else if (actors instanceof List) {
            //jackson会把stirng[]转成arraylist，此处增加arraylist的逻辑判断,by 红豆冰沙2014.11.21
            List<?> list = (List) actors;
            results = new String[list.size()];
            for (int i = 0; i < list.size(); i++) {
                results[i] = (String) list.get(i);
            }
            return results;
        } else if (actors instanceof Long) {
            //如果为Long类型，则返回1个元素的String[]
            results = new String[1];
            results[0] = String.valueOf(actors);
            return results;
        } else if (actors instanceof Integer) {
            //如果为Integer类型，则返回1个元素的String[]
            results = new String[1];
            results[0] = String.valueOf(actors);
            return results;
        } else if (actors instanceof String[]) {
            //如果为String[]类型，则直接返回
            return (String[]) actors;
        } else {
            //其它类型，抛出不支持的类型异常
            throw new FlowLongException("任务参与者对象[" + actors + "]类型不支持."
                    + "合法参数示例:Long,Integer,new String[]{},'10000,20000',List<String>");
        }
    }

    /**
     * 判断当前创建人createBy是否允许执行taskId指定的任务
     */
    @Override
    public boolean isAllowed(Task task, String createBy) {
        // 如果当前创建人不为空
        if (StringUtils.isNotEmpty(createBy)) {
            // 如果是admin或者auto，直接返回true
            if (FlowLongEngine.ADMIN.equalsIgnoreCase(createBy)
                    || FlowLongEngine.AUTO.equalsIgnoreCase(createBy)) {
                return true;
            }
            // 如果为其他，当前做错人和任务执行人对比
            if (StringUtils.isNotEmpty(task.getCreateBy())) {
                return createBy.equals(task.getCreateBy());
            }
        }

        List<TaskActor> actors = taskActorMapper.selectList(Wrappers.<TaskActor>lambdaQuery().eq(TaskActor::getTaskId, task.getId()));
        if (actors == null || actors.isEmpty()) {
            return true;
        }
        return StringUtils.isNotEmpty(createBy) && taskAccessStrategy.isAllowed(createBy, actors);
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
            if (StringUtils.isNotEmpty(actorStr)) {
                String[] actorArray = actorStr.split(",");
                StringBuilder newActor = new StringBuilder(actorStr.length());
                boolean isMatch;
                for (String actor : actorArray) {
                    isMatch = false;
                    if (StringUtils.isEmpty(actor)) {
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
                task.setVariable(FlowLongContext.JSON_HANDLER.toJson(taskData));
                taskMapper.updateById(task);
            }
        }
    }

}
