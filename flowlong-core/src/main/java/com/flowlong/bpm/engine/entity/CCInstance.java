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
import com.flowlong.bpm.engine.core.enums.InstanceState;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;

/**
 * 抄送实例实体类
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
@TableName("flw_cc_instance")
public class CCInstance extends BaseEntity {
    /**
     * 流程实例ID
     */
    protected Long instanceId;
    /**
     * 参与者ID
     */
    protected String actorId;
    /**
     * 状态 0，结束 1，活动
     */
    protected Integer state;
    /**
     * 完成时间
     */
    protected Date finishTime;

    public void setInstanceState(InstanceState instanceState) {
        this.state = instanceState.getValue();
    }

    public static CCInstance activeState(Long instanceId, String actorId, String createBy, Date createTime) {
        CCInstance ccInstance = new CCInstance();
        ccInstance.instanceId = instanceId;
        ccInstance.actorId = actorId;
        ccInstance.createBy = createBy;
        ccInstance.state = InstanceState.active.getValue();
        ccInstance.createTime = createTime;
        return ccInstance;
    }
}
