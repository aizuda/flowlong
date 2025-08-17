/*
 * Copyright 2023-2025 Licensed under the Dual Licensing
 * website: https://aizuda.com
 */
package com.aizuda.bpm.engine;

import com.aizuda.bpm.engine.assist.Assert;
import com.aizuda.bpm.engine.assist.ObjectUtils;
import com.aizuda.bpm.engine.core.Execution;
import com.aizuda.bpm.engine.core.FlowCreator;
import com.aizuda.bpm.engine.core.enums.*;
import com.aizuda.bpm.engine.entity.FlwTask;
import com.aizuda.bpm.engine.entity.FlwTaskActor;
import com.aizuda.bpm.engine.model.NodeAssignee;
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
 * <a href="https://aizuda.com">官网</a>尊重知识产权，不允许非法使用，后果自负
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
        return this.executeTask(taskId, flowCreator, args, TaskState.complete, TaskEventType.complete);
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
    FlwTask executeTask(Long taskId, FlowCreator flowCreator, Map<String, Object> args, TaskState taskState, TaskEventType eventType);

    /**
     * 强制完成所有任务
     *
     * @param instanceId     流程实例ID
     * @param currentFlwTask 当前任务
     * @param flowCreator    处理人员
     * @param instanceState  流程实例最终状态
     * @param eventType      监听事件类型
     * @return true 成功 false 失败
     */
    boolean forceCompleteAllTask(Long instanceId, FlwTask currentFlwTask, FlowCreator flowCreator, InstanceState instanceState, TaskEventType eventType);

    /**
     * 强制完成某个任务
     *
     * @param flwTask     审批任务
     * @param flowCreator 处理人员
     * @param taskState   流任务状态
     * @param eventType   监听事件类型
     * @return true 成功 false 失败
     */
    boolean forceCompleteTask(FlwTask flwTask, FlowCreator flowCreator, TaskState taskState, TaskEventType eventType);

    /**
     * 执行节点跳转任务
     *
     * @param taskId            任务ID
     * @param flowCreator       任务创建者
     * @param args              任务参数
     * @param nodeKey           跳转至目标节点key
     * @param executionFunction 执行函数
     * @param taskTye           任务类型，仅支持 jump rejectJump routeJump
     * @return 当前 flowCreator 所在的任务
     */
    Optional<List<FlwTask>> executeJumpTask(Long taskId, String nodeKey, FlowCreator flowCreator, Map<String, Object> args, Function<FlwTask, Execution> executionFunction, TaskType taskTye);

    default boolean executeJumpTask(Long taskId, String nodeKey, FlowCreator flowCreator, Map<String, Object> args, Function<FlwTask, Execution> executionFunction) {
        return executeJumpTask(taskId, nodeKey, flowCreator, args, executionFunction, TaskType.jump).isPresent();
    }

    default boolean executeJumpTask(Long taskId, String nodeKey, FlowCreator flowCreator, Function<FlwTask, Execution> executionFunction) {
        return executeJumpTask(taskId, nodeKey, flowCreator, null, executionFunction);
    }

    /**
     * 执行触发器任务
     *
     * @param execution {@link Execution}
     * @param flwTask   触发器任务
     * @return true 成功 false 失败
     */
    boolean executeTaskTrigger(Execution execution, FlwTask flwTask);

    /**
     * 完成指定实例ID活动任务
     *
     * @param instanceId  实例ID
     * @param flowCreator 处理人员
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
     * @param taskId      任务ID
     * @param flowCreator 处理人员
     * @return true 成功 false 失败
     */
    boolean viewTask(Long taskId, FlowCreator flowCreator);

    /**
     * 角色根据 任务ID 认领任务，删除其它任务参与者
     *
     * @param taskId      任务ID
     * @param flowCreator 任务认领者
     * @return Task 任务对象
     */
    FlwTask claimRole(Long taskId, FlowCreator flowCreator);

    /**
     * 部门根据 任务ID 认领任务，删除其它任务参与者
     *
     * @param taskId      任务ID
     * @param flowCreator 任务认领者
     * @return Task 任务对象
     */
    FlwTask claimDepartment(Long taskId, FlowCreator flowCreator);

    /**
     * 根据 任务ID 指定代理人
     *
     * @param taskId            任务ID
     * @param flowCreator       任务参与者
     * @param agentFlowCreators 指定代理人列表
     * @param args              任务参数
     * @return true 成功 false 失败
     */
    default boolean agentTask(Long taskId, FlowCreator flowCreator, List<FlowCreator> agentFlowCreators, Map<String, Object> args) {
        return this.assigneeTask(taskId, TaskType.agent, TaskEventType.agent, flowCreator, agentFlowCreators, args, null);
    }

    default boolean agentTask(Long taskId, FlowCreator flowCreator, List<FlowCreator> agentFlowCreators) {
        return this.agentTask(taskId, flowCreator, agentFlowCreators, null);
    }

    /**
     * 根据 任务ID 转办任务
     *
     * @param taskId              任务ID
     * @param flowCreator         任务参与者
     * @param assigneeFlowCreator 指定办理人
     * @param args                任务参数
     * @return true 成功 false 失败
     */
    default boolean transferTask(Long taskId, FlowCreator flowCreator, FlowCreator assigneeFlowCreator, Map<String, Object> args) {
        return this.assigneeTask(taskId, TaskType.transfer, TaskEventType.transfer, flowCreator, Collections.singletonList(assigneeFlowCreator), args, null);
    }

    default boolean transferTask(Long taskId, FlowCreator flowCreator, FlowCreator assigneeFlowCreator) {
        return this.transferTask(taskId, flowCreator, assigneeFlowCreator, null);
    }

    /**
     * 参与者的所有任务【离职转办】给指定办理人
     * <p>
     * 用于任务参与者离职
     * </p>
     *
     * @param flowCreator         任务参与者
     * @param assigneeFlowCreator 指定办理人
     * @return true 成功 false 失败
     */
    boolean transferTask(FlowCreator flowCreator, FlowCreator assigneeFlowCreator);

    /**
     * 根据 任务ID 委派任务、代理人办理完任务该任务重新归还给原处理人
     *
     * @param taskId              任务ID
     * @param flowCreator         任务参与者
     * @param assigneeFlowCreator 指定办理人
     * @param args                任务参数
     * @return true 成功 false 失败
     */
    default boolean delegateTask(Long taskId, FlowCreator flowCreator, FlowCreator assigneeFlowCreator, Map<String, Object> args) {
        return this.assigneeTask(taskId, TaskType.delegate, TaskEventType.delegate, flowCreator, Collections.singletonList(assigneeFlowCreator), args, null);
    }

    default boolean delegateTask(Long taskId, FlowCreator flowCreator, FlowCreator assigneeFlowCreator) {
        return this.delegateTask(taskId, flowCreator, assigneeFlowCreator, null);
    }

    /**
     * 根据 任务ID 分配任务给指定办理人、重置任务类型
     *
     * @param taskId               任务ID
     * @param taskType             任务类型
     * @param taskEventType        任务事件类型
     * @param flowCreator          任务参与者
     * @param assigneeFlowCreators 指定办理人列表
     * @param args                 任务参数
     * @param check                校验函数，可以根据 dbFlwTask.getAssignorId() 是否存在判断为重发分配
     * @return true 成功 false 失败
     */
    boolean assigneeTask(Long taskId, TaskType taskType, TaskEventType taskEventType, FlowCreator flowCreator, List<FlowCreator> assigneeFlowCreators, Map<String, Object> args, Function<FlwTask, Boolean> check);

    default boolean assigneeTask(Long taskId, TaskType taskType, TaskEventType taskEventType, FlowCreator flowCreator, List<FlowCreator> assigneeFlowCreators) {

        // 校验存在重复分配抛出异常
        return this.assigneeTask(taskId, taskType, taskEventType, flowCreator, assigneeFlowCreators, null, t -> {
            if (ObjectUtils.isNotEmpty(t.getAssignorId())) {
                Assert.illegal("Do not allow duplicate assign , taskId = " + taskId);
            }
            return true;
        });
    }

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
    Optional<List<FlwTask>> reclaimTask(Long taskId, FlowCreator flowCreator);

    /**
     * 唤醒撤回或拒绝终止历史任务（只有实例发起人可操作）
     *
     * @param instanceId  历史实例ID
     * @param flowCreator 任务唤醒者
     * @return true 成功 false 失败
     */
    default boolean resume(Long instanceId, FlowCreator flowCreator) {
        return this.resume(instanceId, null, flowCreator);
    }

    /**
     * 唤醒撤回或拒绝终止历史任务（只有实例发起人可操作）
     *
     * @param instanceId  历史实例ID
     * @param nodeKey     节点key历史任务
     * @param flowCreator 任务唤醒者
     * @return true 成功 false 失败
     */
    boolean resume(Long instanceId, String nodeKey, FlowCreator flowCreator);

    /**
     * 根据任务ID、创建人撤回任务（该任务后续任务未执行前有效）
     *
     * @param taskId      待撤回历史任务ID
     * @param flowCreator 任务创建者
     * @return Task 任务对象
     */
    Optional<List<FlwTask>> withdrawTask(Long taskId, FlowCreator flowCreator);

    /**
     * 根据当前任务对象驳回至上一步处理
     *
     * @param currentFlwTask 当前任务对象
     * @param flowCreator    任务创建者
     * @param args           任务参数
     * @return Task 任务对象
     */
    Optional<List<FlwTask>> rejectTask(FlwTask currentFlwTask, FlowCreator flowCreator, Map<String, Object> args);

    default Optional<List<FlwTask>> rejectTask(FlwTask currentFlwTask, FlowCreator flowCreator) {
        return rejectTask(currentFlwTask, flowCreator, null);
    }

    /**
     * 执行完成触发器操作流程继续往下执行
     *
     * @param nodeModel 节点模型 {@link NodeModel}
     * @param execution 执行对象 {@link Execution}
     * @param flowCreator 任务创建者
     * @return true 成功 false 失败
     */
    boolean executeFinishTrigger(NodeModel nodeModel, Execution execution, FlowCreator flowCreator);

    /**
     * 根据 taskId、createBy 判断创建人createBy是否允许执行任务
     *
     * @param flwTask 任务对象
     * @param userId  用户ID
     * @return 被允许参与者 {@link FlwTaskActor}
     */
    FlwTaskActor isAllowed(FlwTask flwTask, String userId);

    /**
     * 根据任务模型、执行对象创建新的任务
     *
     * @param taskModel    任务模型
     * @param execution    执行对象
     * @param taskFunction 任务处理函数，如果自定义 CreateTaskHandler 可用于控制任务创建属性设置
     * @return 创建任务集合
     */
    List<FlwTask> createTask(NodeModel taskModel, Execution execution, Function<FlwTask, FlwTask> taskFunction);

    default List<FlwTask> createTask(NodeModel taskModel, Execution execution) {
        return createTask(taskModel, execution, null);
    }

    /**
     * 根据已有任务、参与者创建新的任务
     * <p>
     * 适用于动态转派，动态协办等处理且流程图中不体现节点情况
     * </p>
     *
     * @param taskId            主办任务ID
     * @param taskActors        参与者集合
     * @param taskType          任务类型
     * @param performType       参与类型
     * @param flowCreator       任务创建者
     * @param executionFunction 执行函数
     * @return 创建任务集合
     */
    List<FlwTask> createNewTask(Long taskId, TaskType taskType, PerformType performType, List<FlwTaskActor> taskActors, FlowCreator flowCreator, Function<FlwTask, Execution> executionFunction);

    /**
     * 创建抄送任务
     * <p>默认不校验是否重复抄送</p>
     *
     * @param taskModel   任务模型
     * @param flwTask     当前任务
     * @param ccUserList  抄送任务分配到任务的人或角色列表
     * @param flowCreator 任务创建者
     */
    boolean createCcTask(NodeModel taskModel, FlwTask flwTask, List<NodeAssignee> ccUserList, FlowCreator flowCreator);

    /**
     * 获取超时或者提醒的任务
     *
     * @return 任务列表
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
     * @param taskId      任务ID
     * @param performType 参与类型 {@link PerformType}
     * @param taskActors  参与者列表
     * @param flowCreator 执行操作人员
     * @return true 成功 false 失败
     */
    boolean addTaskActor(Long taskId, PerformType performType, List<FlwTaskActor> taskActors, FlowCreator flowCreator);

    default boolean addTaskActor(Long taskId, PerformType performType, FlwTaskActor taskActor, FlowCreator flowCreator) {
        return this.addTaskActor(taskId, performType, Collections.singletonList(taskActor), flowCreator);
    }

    default boolean addTaskActor(Long taskId, List<FlwTaskActor> taskActors, FlowCreator flowCreator) {
        return this.addTaskActor(taskId, null, taskActors, flowCreator);
    }

    default boolean addTaskActor(Long taskId, FlwTaskActor taskActor, FlowCreator flowCreator) {
        return this.addTaskActor(taskId, null, taskActor, flowCreator);
    }

    /**
     * 对指定的任务ID删除参与者【减签】
     *
     * @param taskId      任务ID
     * @param actorIds    参与者ID列表
     * @param flowCreator 执行操作人员
     * @return true 成功 false 失败
     */
    boolean removeTaskActor(Long taskId, List<String> actorIds, FlowCreator flowCreator);

    default boolean removeTaskActor(Long taskId, String actorId, FlowCreator flowCreator) {
        return removeTaskActor(taskId, Collections.singletonList(actorId), flowCreator);
    }

    /**
     * 修改 taskId 任务办理人为指定 taskActor 参与者
     *
     * @param taskId      任务ID
     * @param taskActor    参与者
     * @return true 成功 false 失败
     */
    boolean changeTaskActor(Long taskId, FlwTaskActor taskActor);

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
    boolean cascadeRemoveByInstanceIds(List<Long> instanceIds);
}
