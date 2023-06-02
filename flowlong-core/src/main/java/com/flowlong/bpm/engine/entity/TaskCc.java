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
import java.util.Date;

/**
 * 抄送任务实体类
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
@TableName("flw_task_cc")
public class TaskCc implements Serializable {
    /**
     * 主键ID
     */
    protected Long id;
    /**
     * 租户ID
     */
    protected String tenantId;
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
     * 类型 0，用户 1，角色 2，部门
     */
    protected Integer type;
    /**
     * 状态 0，结束 1，活动
     */
    protected Integer state;
    /**
     * 完成时间
     */
    protected Date finishTime;

}
