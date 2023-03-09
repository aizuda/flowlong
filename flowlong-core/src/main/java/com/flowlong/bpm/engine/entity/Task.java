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
import com.flowlong.bpm.engine.core.FlowLongContext;
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
     * 任务类型（0：主办任务；1：协办任务；2：会签任务）
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
     * 提醒时间
     */
    protected Date remindTime;
    /**
     * 任务完成时间
     */
    protected Date finishTime;

    public boolean major() {
        return this.taskType == TaskModel.TaskType.Major.ordinal();
    }

    public String[] actorIds() {
        return null;
    }

    public Map<String, Object> variableMap() {
        Map<String, Object> map = FlowLongContext.JSON_HANDLER.fromJson(this.variable, Map.class);
        if (map == null) return Collections.emptyMap();
        return map;
    }

    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
