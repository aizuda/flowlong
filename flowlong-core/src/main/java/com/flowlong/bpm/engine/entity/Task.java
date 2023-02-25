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

import com.flowlong.bpm.engine.assist.JsonUtils;
import com.flowlong.bpm.engine.model.TaskModel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.Collections;
import java.util.Date;
import java.util.Map;

/**
 * 任务实体类
 *
 * @author hubin
 * @since 1.0
 */
@Getter
@Setter
@ToString
public class Task implements Serializable, Cloneable {
    public static final String KEY_ACTOR = "S-ACTOR";
    /**
     * 主键ID
     */
    private String id;
    /**
     * 租户ID
     */
    private String tenantId;
    /**
     * 版本
     */
    private Integer version = 0;
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
     * 参与方式（0：普通任务；1：参与者会签任务）
     */
    private Integer performType;
    /**
     * 任务类型（0：主办任务；1：协办任务）
     */
    private Integer taskType;
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
     * 期望的完成时间date类型
     */
    private Date expireDate;
    /**
     * 提醒时间date类型
     */
    private Date remindDate;
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
    /**
     * 保持模型对象
     */
    private TaskModel model;

    public Task() {

    }

    public Task(String id) {
        this.id = id;
    }

    public boolean isMajor() {
        return this.taskType == TaskModel.TaskType.Major.ordinal();
    }

    public String[] getActorIds() {
        if (actorIds == null) {
            String actorStr = (String) getVariableMap().get(KEY_ACTOR);
            if (actorStr != null) {
                actorIds = actorStr.split(",");
            }
        }
        return actorIds;
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> getVariableMap() {
        Map<String, Object> map = JsonUtils.fromJson(this.variable, Map.class);
        if (map == null) return Collections.emptyMap();
        return map;
    }

    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
