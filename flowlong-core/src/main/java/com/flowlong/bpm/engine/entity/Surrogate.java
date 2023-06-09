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
 * 委托代理实体类
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
@TableName("flw_surrogate")
public class Surrogate extends BaseEntity {
    /**
     * 流程ID
     */
    protected Long processId;
    /**
     * 流程名称
     */
    protected String processName;
    /**
     * 授权人
     */
    protected String empower;
    /**
     * 代理人
     */
    protected String surrogate;
    /**
     * 状态
     */
    protected Integer state;
    /**
     * 开始时间
     */
    protected Date startTime;
    /**
     * 结束时间
     */
    protected Date endTime;
    /**
     * 操作时间
     */
    protected Date operationTime;

    public void setState(InstanceState instanceState) {
        this.state = instanceState.getValue();
    }
}
