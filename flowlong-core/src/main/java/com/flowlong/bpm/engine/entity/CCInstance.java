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

import java.io.Serializable;

/**
 * 抄送实例实体类
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
    protected String finishTime;

    public void setFlowState(FlowState flowState) {
        this.state = flowState.getValue();
    }
}
