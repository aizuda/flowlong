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
package com.flowlong.bpm.engine.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.flowlong.bpm.engine.core.FlowState;
import com.flowlong.bpm.engine.model.TaskModel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 历史任务实体类
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
@ToString
@TableName("flw_his_task")
public class HisTask extends Task {
    /**
     * 任务状态（0：结束；1：活动）
     */
    protected Integer taskState;

    public void setTaskState(FlowState flowState) {
        this.taskState = flowState.getValue();
    }

    public HisTask() {

    }

    public HisTask(Task task) {
        this.id = task.getId();
        this.instanceId = task.getInstanceId();
        this.createTime = task.getCreateTime();
        this.displayName = task.getDisplayName();
        this.taskName = task.getTaskName();
        this.taskType = task.getTaskType();
        this.expireTime = task.getExpireTime();
        this.actionUrl = task.getActionUrl();
        this.parentTaskId = task.getParentTaskId();
        this.variable = task.getVariable();
        this.performType = task.getPerformType();
    }

    /**
     * 根据历史任务产生撤回的任务对象
     *
     * @return 任务对象
     */
    public Task undoTask() {
        Task task = new Task();
        task.setInstanceId(this.getInstanceId());
        task.setTaskName(this.getTaskName());
        task.setDisplayName(this.getDisplayName());
        task.setTaskType(this.getTaskType());
        task.setExpireTime(this.getExpireTime());
        task.setActionUrl(this.getActionUrl());
        task.setParentTaskId(this.getParentTaskId());
        task.setVariable(this.getVariable());
        task.setPerformType(this.getPerformType());
        task.setCreateBy(this.getCreateBy());
        return task;
    }

    public boolean isPerformAny() {
        return this.performType.intValue() == TaskModel.PerformType.ANY.ordinal();
    }

}
