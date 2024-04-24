/*
 * Copyright 2023-2025 Licensed under the AGPL License
 */
package com.aizuda.bpm.engine;

import com.aizuda.bpm.engine.core.Execution;
import com.aizuda.bpm.engine.core.FlowCreator;
import com.aizuda.bpm.engine.core.enums.EventType;
import com.aizuda.bpm.engine.core.enums.PerformType;
import com.aizuda.bpm.engine.core.enums.TaskState;
import com.aizuda.bpm.engine.core.enums.TaskType;
import com.aizuda.bpm.engine.entity.FlwTask;
import com.aizuda.bpm.engine.entity.FlwTaskActor;
import com.aizuda.bpm.engine.model.NodeModel;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

/**
 * 任务业务类接口
 *
 * <p>
 * 尊重知识产权，不允许非法使用，后果自负
 * </p>
 *
 * @author hubin
 * @since 1.0
 */
public interface TaskService {

    /**
     * 根据任务ID，创建人ID完成任务
     * <p>
     * 该方法仅仅结束活动任务，并不能驱动流程继续执行
     * </p>
     *
     * @param taskId      任务ID
     * @param flowCreator 任务完成者
     * @param args        任务参数
     * @return Task 任务对象
     */
    default FlwTask complete(Long taskId, FlowCreator flowCreator, Map<String, Object> args) {
        return this.executeTask(taskId, flowCreator, args, TaskState.complete, EventType.complete);
    }

    default FlwTask complete(Long taskId, FlowCreator flowCreator) {
        return this.complete(taskId, flowCreator, null);
    }

    /**
     * 根据任务ID，创建人ID完成任务
     *
     * @param taskId      任务ID
     * @param flowCreator 任务完成者
     * @param args        任务参数
     * @param taskState   任务状态
     * @param eventType   任务执行事件类型
     * @return Task 任务对象
     */
    FlwTask executeTask(Long taskId, FlowCreator flowCreator, Map<String, Object> args, TaskState taskState, EventType eventType);

    /**
     * 执行节点跳转任务
     *
     * @param taskId            任务ID
     * @param flowCreator       任务创建者
     * @param args              任务参数
     * @param nodeName          跳转至目标节点名称
     * @param executionFunction 执行函数
     * @return 当前 flowCreator 所在的任务
     */
    boolean executeJumpTask(Long taskId, String nodeName, FlowCreator flowCreator, Map<String, Object> args, Function<FlwTask, Execution> executionFunction);

    default boolean executeJumpTask(Long taskId, String nodeName, FlowCreator flowCreator, Function<FlwTask, Execution> executionFunction) {
        return executeJumpTask(taskId, nodeName, flowCreator, null, executionFunction);
    }

    /**
     * 完成指定实例ID活动任务
     *
     * @param instanceId 实例ID
     * @return true 成功 false 失败
     */
    boolean completeActiveTasksByInstanceId(Long instanceId, FlowCreator flowCreator);

    /**
     * 更新任务对象
     *
     * @param flwTask     任务对象
     * @param flowCreator 处理人员
     */
    void updateTaskById(FlwTask flwTask, FlowCreator flowCreator);

    /**
     * 查看任务设置为已阅状态
     *
     * @param taskId    任务ID
     * @param taskActor 任务参与者
     * @return true 成功 false 失败
     */
    boolean viewTask(Long taskId, FlwTaskActor taskActor);

    /**
     * 根据 任务ID 认领任务，删除其它任务参与者
     *
     * @param taskId      任务ID
     * @param flowCreator 任务认领者
     * @return Task 任务对象
     */
    FlwTask claim(Long taskId, FlowCreator flowCreator);

    /**
     * 根据 任务ID 指定代理人
     *
     * @param taskId           任务ID
     * @param flowCreator      任务参与者
     * @param agentFlowCreator 指定代理人
     * @return true 成功 false 失败
     */
    default boolean agentTask(Long taskId, FlowCreator flowCreator, FlowCreator agentFlowCreator) {
        return this.assigneeTask(taskId, TaskType.agent, flowCreator, agentFlowCreator);
    }

    /**
     * 根据 任务ID 转办任务
     *
     * @param taskId              任务ID
     * @param flowCreator         任务参与者
     * @param assigneeFlowCreator 指定办理人
     * @return true 成功 false 失败
     */
    default boolean transferTask(Long taskId, FlowCreator flowCreator, FlowCreator assigneeFlowCreator) {
        return this.assigneeTask(taskId, TaskType.transfer, flowCreator, assigneeFlowCreator);
    }

    /**
     * 根据 任务ID 委派任务、代理人办理完任务该任务重新归还给原处理人
     *
     * @param taskId              任务ID
     * @param flowCreator         任务参与者
     * @param assigneeFlowCreator 指定办理人
     * @return true 成功 false 失败
     */
    default boolean delegateTask(Long taskId, FlowCreator flowCreator, FlowCreator assigneeFlowCreator) {
        return this.assigneeTask(taskId, TaskType.delegate, flowCreator, assigneeFlowCreator);
    }

    /**
     * 根据 任务ID 分配任务给指定办理人、重置任务类型
     *
     * @param taskId              任务ID
     * @param taskType            任务类型
     * @param flowCreator         任务参与者
     * @param assigneeFlowCreator 指定办理人
     * @return true 成功 false 失败
     */
    boolean assigneeTask(Long taskId, TaskType taskType, FlowCreator flowCreator, FlowCreator assigneeFlowCreator);

    /**
     * 根据 任务ID 解决委派任务
     *
     * @param taskId      任务ID
     * @param flowCreator 任务参与者
     * @return true 成功 false 失败
     */
    boolean resolveTask(Long taskId, FlowCreator flowCreator);

    /**
     * 拿回任务、在当前办理人尚未处理文件前，允许上一节点提交人员执行拿回
     *
     * @param taskId      任务ID（当前节点的父任务ID属于历史任务）
     * @param flowCreator 任务创建者
     * @return 拿回任务
     */
    Optional<FlwTask> reclaimTask(Long taskId, FlowCreator flowCreator);

    /**
     * 唤醒历史任务
     * <p>
     * 该方法会导致流程状态不可控，请慎用
     * </p>
     *
     * @param taskId      历史任务ID
     * @param flowCreator 任务唤醒者
     * @return {@link FlwTask} 唤醒后的任务对象
     */
    FlwTask resume(Long taskId, FlowCreator flowCreator);

    /**
     * 根据任务ID、创建人撤回任务（该任务后续任务未执行前有效）
     *
     * @param taskId      待撤回历史任务ID
     * @param flowCreator 任务创建者
     * @return Task 任务对象
     */
    Optional<FlwTask> withdrawTask(Long taskId, FlowCreator flowCreator);

    /**
     * 根据当前任务对象驳回至上一步处理
     *
     * @param currentFlwTask 当前任务对象
     * @param flowCreator    任务创建者
     * @param args           任务参数
     * @return Task 任务对象
     */
    Optional<FlwTask> rejectTask(FlwTask currentFlwTask, FlowCreator flowCreator, Map<String, Object> args);

    default Optional<FlwTask> rejectTask(FlwTask currentFlwTask, FlowCreator flowCreator) {
        return rejectTask(currentFlwTask, flowCreator, null);
    }

    /**
     * 根据 taskId、createBy 判断创建人createBy是否允许执行任务
     *
     * @param flwTask 任务对象
     * @param userId  用户ID
     * @return boolean 是否允许操作
     */
    boolean isAllowed(FlwTask flwTask, String userId);

    /**
     * 根据任务模型、执行对象创建新的任务
     *
     * @param taskModel 任务模型
     * @param execution 执行对象
     * @return List<Task> 创建任务集合
     */
    List<FlwTask> createTask(NodeModel taskModel, Execution execution);

    /**
     * 根据已有任务ID、任务类型、参与者创建新的任务
     *
     * @param taskId     主办任务ID
     * @param taskType   任务类型 {@link TaskType}
     * @param taskActors 参与者集合
     * @return List<Task> 创建任务集合
     */
    List<FlwTask> createNewTask(Long taskId, TaskType taskType, List<FlwTaskActor> taskActors);

    default List<FlwTask> createNewTask(Long taskId, TaskType taskType, FlwTaskActor taskActor) {
        return this.createNewTask(taskId, taskType, Collections.singletonList(taskActor));
    }

    /**
     * 获取超时或者提醒的任务
     *
     * @return List<Task> 任务列表
     */
    List<FlwTask> getTimeoutOrRemindTasks();

    /**
     * 根据任务ID获取任务模型
     *
     * @param taskId 任务ID
     * @return 流程模型
     */
    NodeModel getTaskModel(Long taskId);

    /**
     * 向指定的任务ID添加参与者【加签】
     *
     * @param taskId        任务ID
     * @param performType   参与类型 {@link PerformType}
     * @param flwTaskActors 参与者列表
     * @param flowCreator   执行操作人员
     */
    boolean addTaskActor(Long taskId, PerformType performType, List<FlwTaskActor> flwTaskActors, FlowCreator flowCreator);

    default boolean addTaskActor(Long taskId, PerformType performType, FlwTaskActor flwTaskActor, FlowCreator flowCreator) {
        return this.addTaskActor(taskId, performType, Collections.singletonList(flwTaskActor), flowCreator);
    }

    /**
     * 对指定的任务ID删除参与者【减签】
     *
     * @param taskId      任务ID
     * @param actorIds    参与者ID列表
     * @param flowCreator 执行操作人员
     */
    boolean removeTaskActor(Long taskId, List<String> actorIds, FlowCreator flowCreator);

    default boolean removeTaskActor(Long taskId, String actorId, FlowCreator flowCreator) {
        return removeTaskActor(taskId, Collections.singletonList(actorId), flowCreator);
    }

    /**
     * 结束调用外部流程任务
     *
     * @param callProcessId  调用外部流程定义ID
     * @param callInstanceId 调用外部流程实例ID
     */
    void endCallProcessTask(Long callProcessId, Long callInstanceId);

    /**
     * 级联删除 flw_his_task, flw_his_task_actor, flw_task, flw_task_actor
     *
     * @param instanceIds 流程实例ID列表
     */
    void cascadeRemoveByInstanceIds(List<Long> instanceIds);
}
