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
package com.flowlong.bpm.engine.model;

import com.flowlong.bpm.engine.Assignment;
import com.flowlong.bpm.engine.QueryService;
import com.flowlong.bpm.engine.assist.Assert;
import com.flowlong.bpm.engine.assist.ClassUtils;
import com.flowlong.bpm.engine.assist.ObjectUtils;
import com.flowlong.bpm.engine.core.Execution;
import com.flowlong.bpm.engine.core.FlowLongContext;
import com.flowlong.bpm.engine.entity.HisTask;
import com.flowlong.bpm.engine.entity.Instance;
import com.flowlong.bpm.engine.entity.Task;
import com.flowlong.bpm.engine.handler.impl.MergeActorHandler;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 任务定义task元素
 *
 * <p>
 * 尊重知识产权，CV 请保留版权，爱组搭 http://aizuda.com 出品
 * </p>
 *
 * @author hubin
 * @since 1.0
 */
@Getter
@Setter
public class TaskModel extends WorkModel {
    /**
     * 参与者变量名称
     */
    private String assignee;
    /**
     * 参与方式
     * any：任何一个参与者处理完即执行下一步
     * all：所有参与者都完成，才可执行下一步
     * percentage:完成数/所有参与者数 > 百分比，才可以执行下一步
     */
    private String performType = PERFORM_TYPE_ANY;
    /**
     * 任务类型
     * <p>
     * major 主办任务
     * </p>
     * <p>
     * assist 协办任务
     * </p>
     */
    private String taskType = TASK_TYPE_MAJOR;
    /**
     * 期望完成时间
     */
    private String expireTime;
    /**
     * 提醒时间
     */
    private String reminderTime;
    /**
     * 提醒间隔(分钟)
     */
    private String reminderRepeat;
    /**
     * 是否自动执行
     */
    private String autoExecute;
    /**
     * 分配参与者处理对象
     */
    private Assignment assignmentHandlerObject;
    /**
     * 字段模型集合
     */
    private List<FieldModel> fields;

    /**
     * 任务通过百分比
     */
    private String taskPassPercentage;

    @Override
    protected void run(FlowLongContext flowLongContext, Execution execution) {
        if (performType == null || performType.equalsIgnoreCase(PERFORM_TYPE_ANY)) {
            /**
             * any 方式，直接执行输出变迁
             */
            runOutTransition(flowLongContext, execution);
        } else if(performType.equalsIgnoreCase(PERFORM_TYPE_ALL)) {
            /**
             * all 方式，需要判断是否已全部合并
             * 由于all方式分配任务，是每人一个任务
             * 那么此时需要判断之前分配的所有任务都执行完成后，才可执行下一步，否则不处理
             */
            fire(new MergeActorHandler(getName()), flowLongContext, execution);
            if (execution.isMerged()) {
                runOutTransition(flowLongContext, execution);
            }
        } else if(performType.equalsIgnoreCase(PERFORM_TYPE_PERCENTAGE)) {
            /**
             * percentage 方式 需要判断当前通过人数是否>=通过百分比
             * 需要判断当前通过人数是否>=通过百分比，才可执行下一步，否则不处理
             */
            Task task = execution.getTask();
            String taskName = task.getTaskName();
            Instance instance = execution.getInstance();
            QueryService queryService = flowLongContext.getQueryService();
            List<Task> activeTasks = queryService.getActiveTasks(instance.getId(), Collections.singletonList(taskName));
            List<HisTask> hisActiveTasks = queryService.getHisActiveTasks(instance.getId(), Collections.singletonList(taskName));
            int totalActorNum = activeTasks.size() + hisActiveTasks.size();
            int passNUm = hisActiveTasks.size();
            if (passNUm >= (totalActorNum * (Float.parseFloat(taskPassPercentage) / 100F))) {
                runOutTransition(flowLongContext, execution);
            }
        }
    }

    public boolean isPerformAny() {
        return PERFORM_TYPE_ANY.equalsIgnoreCase(this.performType);
    }

    public boolean isPerformAll() {
        return PERFORM_TYPE_ALL.equalsIgnoreCase(this.performType);
    }

    public boolean isPerformPercentage() {
        return PERFORM_TYPE_PERCENTAGE.equalsIgnoreCase(this.performType);
    }

    public boolean isMajor() {
        return TASK_TYPE_MAJOR.equalsIgnoreCase(this.taskType);
    }

    public boolean isAssist() { return TASK_TYPE_ASSIST.equalsIgnoreCase(this.taskType); }

    public void setTaskType(String taskType) {
        this.taskType = (ObjectUtils.isEmpty(taskType) ? TASK_TYPE_MAJOR : taskType);
    }

    public void setPerformType(String performType) {
        this.performType = (ObjectUtils.isEmpty(performType) ? PERFORM_TYPE_ANY : performType);
    }

    public void setAssignmentHandler(String assignmentHandler) {
        if (ObjectUtils.isNotEmpty(assignmentHandler)) {
            this.assignmentHandlerObject = (Assignment) ClassUtils.newInstance(assignmentHandler);
            Assert.notNull(assignmentHandlerObject, "分配参与者处理类实例化失败");
        }
    }

    /**
     * 获取后续任务模型集合（方便预处理）
     *
     * @return 模型集合
     * @deprecated
     */
    public List<TaskModel> getNextTaskModels() {
        List<TaskModel> models = new ArrayList<>();
        for (TransitionModel tm : this.getOutputs()) {
            addNextModels(models, tm, TaskModel.class);
        }
        return models;
    }

}
