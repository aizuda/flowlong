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

import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.flowlong.bpm.engine.Assignment;
import com.flowlong.bpm.engine.QueryService;
import com.flowlong.bpm.engine.assist.Assert;
import com.flowlong.bpm.engine.assist.ClassUtils;
import com.flowlong.bpm.engine.assist.StringUtils;
import com.flowlong.bpm.engine.core.Execution;
import com.flowlong.bpm.engine.core.FlowLongContext;
import com.flowlong.bpm.engine.entity.HisTask;
import com.flowlong.bpm.engine.entity.Instance;
import com.flowlong.bpm.engine.entity.Task;
import com.flowlong.bpm.engine.handler.impl.MergeActorHandler;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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
@Slf4j
@Getter
@Setter
public class TaskModel extends WorkModel {
    /**
     * 类型：普通任务
     */
    public static final String PERFORMTYPE_ANY = "ANY";
    /**
     * 类型：参与者fork任务
     */
    public static final String PERFORMTYPE_ALL = "ALL";

    /**
     * 类型：参与者会签百分比
     */
    public static final String PERFORMTYPE_PERCENTAGE = "PERCENTAGE";
    /**
     * 类型：主办任务
     */
    public static final String TASKTYPE_MAJOR = "Major";
    /**
     * 类型：协办任务
     */
    public static final String TASKTYPE_AIDANT = "Aidant";
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
    private String performType = PERFORMTYPE_ANY;
    /**
     * 任务类型
     * major：主办任务
     * aidant：协办任务
     */
    private String taskType = TASKTYPE_MAJOR;
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
     * 任务执行后回调类
     */
    private String callback;
    /**
     * 分配参与者处理类型
     */
    private String assignmentHandler;
    /**
     * 分配参与者处理对象
     */
    private Assignment assignmentHandlerObject;
    /**
     * 字段模型集合
     */
    private List<FieldModel> fields = null;

    /**
     * 任务通过百分比
     */
    private String taskPassPercentage;

    @Override
    protected void run(FlowLongContext flowLongContext, Execution execution) {
        if (performType == null || performType.equalsIgnoreCase(PERFORMTYPE_ANY)) {
            /**
             * any方式，直接执行输出变迁
             */
            runOutTransition(flowLongContext, execution);
        } else if(performType.equalsIgnoreCase(PERFORMTYPE_ALL)) {
            /**
             * all方式，需要判断是否已全部合并
             * 由于all方式分配任务，是每人一个任务
             * 那么此时需要判断之前分配的所有任务都执行完成后，才可执行下一步，否则不处理
             */
            fire(new MergeActorHandler(getName()), flowLongContext, execution);
            if (execution.isMerged()) {
                runOutTransition(flowLongContext, execution);
            }
        } else if(performType.equalsIgnoreCase(PERFORMTYPE_PERCENTAGE)) {
            /**
             * PERCENTAGE方式 需要判断当前通过人数是否>=通过百分比
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
            if (passNUm >= totalActorNum * Integer.parseInt(taskPassPercentage) / 100) {
                runOutTransition(flowLongContext, execution);
            }
        }
    }

    public boolean isPerformAny() {
        return PERFORMTYPE_ANY.equalsIgnoreCase(this.performType);
    }

    public boolean isPerformAll() {
        return PERFORMTYPE_ALL.equalsIgnoreCase(this.performType);
    }

    public boolean isPerformPercentage() {
        return PERFORMTYPE_PERCENTAGE.equalsIgnoreCase(this.performType);
    }

    public boolean isMajor() {
        return TASKTYPE_MAJOR.equalsIgnoreCase(this.taskType);
    }

    public boolean isAidant() { return TASKTYPE_AIDANT.equalsIgnoreCase(this.taskType); }

    public void setTaskType(String taskType) {
        this.taskType = (StringUtils.isEmpty(taskType) ? TASKTYPE_MAJOR : taskType);
    }

    public String getPerformType() {
        return performType;
    }

    public void setPerformType(String performType) {
        this.performType = (StringUtils.isEmpty(performType) ? PERFORMTYPE_ANY : performType);
    }

    public void setAssignmentHandler(String assignmentHandlerStr) {
        if (StringUtils.isNotEmpty(assignmentHandlerStr)) {
            this.assignmentHandler = assignmentHandlerStr;
            assignmentHandlerObject = (Assignment) ClassUtils.newInstance(assignmentHandlerStr);
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

    /**
     * 参与类型
     */
    public enum PerformType {
        ANY, ALL, PERCENTAGE;
    }

    /**
     * 任务类型(Major:主办的,Aidant:协助的,countersign:会签的,Record:仅仅作为记录的)
     */
    public enum TaskType {
        Major, Aidant,Record;
    }
}
