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

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

/**
 * 工作项（待办、已处理任务的查询结果实体）
 *
 * @author hubin
 * @since 1.0
 */
@Getter
@Setter
@ToString
public class WorkItem implements Serializable {
    private String id;
    /**
     * 流程定义ID
     */
    private String processId;
    /**
     * 流程实例ID
     */
    private String orderId;
    /**
     * 任务ID
     */
    private String taskId;
    /**
     * 流程名称
     */
    private String processName;
    /**
     * 流程实例url
     */
    private String instanceUrl;
    /**
     * 流程实例为子流程时，该字段标识父流程实例ID
     */
    private String parentId;
    /**
     * 流程实例创建者ID
     */
    private String creator;
    /**
     * 流程实例创建时间
     */
    private String orderCreateTime;
    /**
     * 流程实例结束时间
     */
    private String orderEndTime;
    /**
     * 流程实例期望完成时间
     */
    private String orderExpireTime;
    /**
     * 流程实例编号
     */
    private String orderNo;
    /**
     * 流程实例附属变量
     */
    private String orderVariable;
    /**
     * 任务名称
     */
    private String taskName;
    /**
     * 任务标识名称
     */
    private String taskKey;
    /**
     * 参与类型（0：普通任务；1：参与者fork任务[即：如果10个参与者，需要每个人都要完成，才继续流转]）
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
     * 任务创建时间
     */
    private String taskCreateTime;
    /**
     * 任务完成时间
     */
    private String taskEndTime;
    /**
     * 期望任务完成时间
     */
    private String taskExpireTime;
    /**
     * 任务附属变量
     */
    private String taskVariable;
    /**
     * 任务处理者ID
     */
    private String operator;
    /**
     * 任务关联的表单url
     */
    private String actionUrl;
    /**
     * 任务参与者列表
     */
    private String[] actorIds;

}
