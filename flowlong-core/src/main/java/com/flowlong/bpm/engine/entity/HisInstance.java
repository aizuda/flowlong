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
 * 历史流程实例实体类
 *
 * @author hubin
 * @since 1.0
 */
@Getter
@Setter
@ToString
public class HisInstance implements Serializable {
    /**
     * 主键ID
     */
    private String id;
    /**
     * 租户ID
     */
    private String tenantId;
    /**
     * 流程定义ID
     */
    private String processId;
    /**
     * 流程实例状态（0：结束；1：活动）
     */
    private Integer instanceState;
    /**
     * 流程实例创建者ID
     */
    private String creator;
    /**
     * 流程实例创建时间
     */
    private String createTime;
    /**
     * 流程实例结束时间
     */
    private String endTime;
    /**
     * 流程实例为子流程时，该字段标识父流程实例ID
     */
    private String parentId;
    /**
     * 流程实例期望完成时间
     */
    private String expireTime;
    /**
     * 流程实例优先级
     */
    private Integer priority;
    /**
     * 流程实例编号
     */
    private String instanceNo;
    /**
     * 流程实例附属变量
     */
    private String variable;

    public HisInstance() {

    }

    public HisInstance(Instance instance, Integer instanceState) {
        this.id = instance.getId();
        this.instanceState = instanceState;
        this.processId = instance.getProcessId();
        this.createTime = instance.getCreateTime();
        this.expireTime = instance.getExpireTime();
        this.creator = instance.getCreator();
        this.parentId = instance.getParentId();
        this.priority = instance.getPriority();
        this.instanceNo = instance.getInstanceNo();
        this.variable = instance.getVariable();
    }

    /**
     * 根据历史实例撤回活动实例
     *
     * @return 活动实例对象
     */
    public Instance undo() {
        Instance instance = new Instance();
        instance.setId(this.id);
        instance.setProcessId(this.processId);
        instance.setParentId(this.parentId);
        instance.setCreator(this.creator);
        instance.setCreateTime(this.createTime);
        instance.setLastUpdator(this.creator);
        instance.setLastUpdateTime(this.endTime);
        instance.setExpireTime(this.expireTime);
        instance.setInstanceNo(this.instanceNo);
        instance.setPriority(this.priority);
        instance.setVariable(this.variable);
        instance.setVersion(0);
        return instance;
    }
}
