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
 * 委托代理实体类
 *
 * @author hubin
 * @since 1.0
 */
@Getter
@Setter
@ToString
@TableName("flw_surrogate")
public class Surrogate implements Serializable {
    /**
     * 主键ID
     */
    protected String id;
    /**
     * 流程name
     */
    protected String processName;
    /**
     * 授权人
     */
    protected String operator;
    /**
     * 代理人
     */
    protected String surrogate;
    /**
     * 操作时间
     */
    protected String odate;
    /**
     * 开始时间
     */
    protected String sdate;
    /**
     * 结束时间
     */
    protected String edate;
    /**
     * 状态
     */
    protected Integer state;

    public void setState(FlowState flowState) {
        this.state = flowState.getValue();
    }
}
