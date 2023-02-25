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
package com.flowlong.bpm.engine.entity;

import com.flowlong.bpm.engine.model.TaskModel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

/**
 * 历史任务实体类
 *
 * @author hubin
 * @since 1.0
 */
@Getter
@Setter
@ToString
public class HisTask implements Serializable {
    /**
     * 主键ID
     */
    private String id;
    /**
     * 流程实例ID
     */
    private String instanceId;
    /**
     * 任务名称
     */
    private String taskName;
    /**
     * 任务显示名称
     */
    private String displayName;
    /**
     * 参与方式（0：普通任务；1：参与者fork任务[即：如果10个参与者，需要每个人都要完成，才继续流转]）
     */
    private Integer performType;
    /**
     * 任务类型
     */
    private Integer taskType;
    /**
     * 任务状态（0：结束；1：活动）
     */
    private Integer taskState;
    /**
     * 任务处理者ID
     */
    private String operator;
    /**
     * 任务创建时间
     */
    private String createTime;
    /**
     * 任务完成时间
     */
    private String finishTime;
    /**
     * 期望任务完成时间
     */
    private String expireTime;
    /**
     * 任务关联的表单url
     */
    private String actionUrl;
    /**
     * 任务参与者列表
     */
    private String[] actorIds;
    /**
     * 父任务Id
     */
    private String parentTaskId;
    /**
     * 任务附属变量
     */
    private String variable;

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
        this.actorIds = task.getActorIds();
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
        ;
        task.setTaskName(this.getTaskName());
        task.setDisplayName(this.getDisplayName());
        task.setTaskType(this.getTaskType());
        task.setExpireTime(this.getExpireTime());
        task.setActionUrl(this.getActionUrl());
        task.setParentTaskId(this.getParentTaskId());
        task.setVariable(this.getVariable());
        task.setPerformType(this.getPerformType());
        task.setOperator(this.getOperator());
        return task;
    }

    public boolean isPerformAny() {
        return this.performType.intValue() == TaskModel.PerformType.ANY.ordinal();
    }

}
