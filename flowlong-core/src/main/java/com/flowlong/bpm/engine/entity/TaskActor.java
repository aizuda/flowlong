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
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

/**
 * 任务参与者实体类
 *
 * <p>
 * 尊重知识产权，CV 请保留版权，爱组搭 http://aizuda.com 出品，不允许非法使用，后果自负
 * </p>
 *
 * @author hubin
 * @since 1.0
 */
@Getter
@Setter
@ToString
@TableName("flw_task_actor")
public class TaskActor implements Serializable {
    /**
     * 主键ID
     */
    protected Long id;
    /**
     * 租户ID
     */
    protected String tenantId;
    /**
     * 流程实例ID
     */
    protected Long instanceId;
    /**
     * 关联的任务ID
     */
    protected Long taskId;
    /**
     * 关联的参与者ID（参与者可以为用户、部门、角色）
     */
    protected String actorId;
    /**
     * 关联的参与者名称
     */
    protected String actorName;
    /**
     * 参与者类型 0，用户 1，角色 2，部门
     */
    protected Integer actorType;

    public static TaskActor ofUser(String actorId, String actorName) {
        return of(actorId, actorName, 0);
    }

    public static TaskActor ofRole(String actorId, String actorName) {
        return of(actorId, actorName, 1);
    }

    public static TaskActor ofDepartment(String actorId, String actorName) {
        return of(actorId, actorName, 2);
    }

    protected static TaskActor of(String actorId, String actorName, Integer actorType) {
        TaskActor taskActor = new TaskActor();
        taskActor.setActorId(actorId);
        taskActor.setActorName(actorName);
        taskActor.setActorType(actorType);
        return taskActor;
    }
}
