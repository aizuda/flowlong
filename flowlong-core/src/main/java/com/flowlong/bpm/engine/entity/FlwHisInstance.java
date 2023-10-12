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

import com.flowlong.bpm.engine.assist.Assert;
import com.flowlong.bpm.engine.core.enums.InstanceState;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;

/**
 * 历史流程实例实体类
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
public class FlwHisInstance extends FlwInstance {
    /**
     * 状态 0，活动 1，结束
     */
    protected Integer instanceState;
    /**
     * 结束时间
     */
    protected Date endTime;

    public void setVariable(Integer instanceState) {
        this.instanceState = instanceState;
    }

    public void setInstanceState(InstanceState instanceState) {
        this.instanceState = instanceState.getValue();
    }

    public void setInstanceState(Integer instanceState) {
        Assert.notNull(InstanceState.get(instanceState), "插入的实例状态异常 [instanceState=" + instanceState + "]");
        this.instanceState = instanceState;
    }

    public static FlwHisInstance of(FlwInstance flwInstance, InstanceState instanceState) {
        FlwHisInstance hisInstance = new FlwHisInstance();
        hisInstance.id = flwInstance.getId();
        hisInstance.instanceState = instanceState.getValue();
        hisInstance.processId = flwInstance.getProcessId();
        hisInstance.createTime = flwInstance.getCreateTime();
        hisInstance.expireTime = flwInstance.getExpireTime();
        hisInstance.createId = flwInstance.getCreateId();
        hisInstance.createBy = flwInstance.getCreateBy();
        hisInstance.priority = flwInstance.getPriority();
        hisInstance.instanceNo = flwInstance.getInstanceNo();
        hisInstance.variable = flwInstance.getVariable();
        return hisInstance;
    }
}
