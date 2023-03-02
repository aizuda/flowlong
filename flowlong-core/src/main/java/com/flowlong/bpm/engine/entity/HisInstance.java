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

import com.baomidou.mybatisplus.annotation.TableName;
import com.flowlong.bpm.engine.core.FlowState;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;

/**
 * 历史流程实例实体类
 *
 * @author hubin
 * @since 1.0
 */
@Getter
@Setter
@ToString
@TableName("flw_his_instance")
public class HisInstance extends Instance {
    /**
     * 状态 0，结束 1，活动
     */
    protected Integer instanceState;
    /**
     * 结束时间
     */
    protected Date endTime;

    public void setInstanceState(FlowState flowState) {
        this.instanceState = flowState.getValue();
    }

    public HisInstance() {

    }

    public HisInstance(Instance instance, FlowState flowState) {
        this.id = instance.getId();
        this.instanceState = flowState.getValue();
        this.processId = instance.getProcessId();
        this.createTime = instance.getCreateTime();
        this.expireTime = instance.getExpireTime();
        this.createBy = instance.getCreateBy();
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
        instance.createBy = instance.getCreateBy();
        instance.setCreateTime(this.createTime);
        instance.setLastUpdateBy(this.createBy);
        instance.setLastUpdateTime(this.endTime);
        instance.setExpireTime(this.expireTime);
        instance.setInstanceNo(this.instanceNo);
        instance.setPriority(this.priority);
        instance.setVariable(this.variable);
        instance.setVersion(0);
        return instance;
    }
}
