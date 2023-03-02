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

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.flowlong.bpm.engine.assist.JsonUtils;
import com.flowlong.bpm.engine.model.TaskModel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

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
@TableName("flw_task")
public class Task extends BaseEntity {
    public static final String KEY_ACTOR = "S-ACTOR";
    /**
     * 流程实例ID
     */
    protected Long instanceId;
    /**
     * 父任务Id
     */
    protected Long parentTaskId;
    /**
     * 任务名称
     */
    protected String taskName;
    /**
     * 任务显示名称
     */
    protected String displayName;
    /**
     * 任务类型（0：主办任务；1：协办任务）
     */
    protected Integer taskType;
    /**
     * 参与方式（0：普通任务；1：参与者会签任务）
     */
    protected Integer performType;
    /**
     * 任务关联的表单url
     */
    protected String actionUrl;
    /**
     * 变量json
     */
    protected String variable;
    /**
     * 版本，默认 1
     */
    protected Integer version;
    /**
     * 期望任务完成时间
     */
    protected Date expireTime;
    /**
     * 任务完成时间
     */
    protected Date finishTime;
    /**
     * 任务参与者列表
     */
    @TableField(exist = false)
    protected String[] actorIds;
    /**
     * 保持模型对象
     */
    @TableField(exist = false)
    protected TaskModel taskModel;

    public Task() {

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

    public Map<String, Object> getVariableMap() {
        Map<String, Object> map = JsonUtils.fromJson(this.variable, Map.class);
        if (map == null) return Collections.emptyMap();
        return map;
    }

    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
