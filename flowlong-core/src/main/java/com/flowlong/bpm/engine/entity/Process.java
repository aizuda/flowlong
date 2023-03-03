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

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.flowlong.bpm.engine.core.FlowState;
import com.flowlong.bpm.engine.model.ProcessModel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 流程定义实体类
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
@TableName("flw_process")
public class Process extends BaseEntity {
    /**
     * 流程定义名称
     */
    protected String name;
    /**
     * 流程定义显示名称
     */
    protected String displayName;
    /**
     * 流程定义类型（预留字段）
     */
    protected String type;
    /**
     * 版本
     */
    protected Integer version;
    /**
     * 当前流程的实例url（一般为流程第一步的url）
     * 该字段可以直接打开流程申请的表单
     */
    protected String instanceUrl;
    /**
     * 是否可用的开关
     */
    protected Integer state;
    /**
     * 流程定义xml
     */
    protected byte[] content;
    /**
     * 流程定义模型
     */
    @TableField(exist = false)
    protected ProcessModel processModel;

    public void setFlowState(FlowState flowState) {
        this.state = flowState.getValue();
    }

    public void setProcessModel(ProcessModel processModel) {
        this.processModel = processModel;
        this.name = processModel.getName();
        this.displayName = processModel.getDisplayName();
        this.instanceUrl = processModel.getInstanceUrl();
    }

}
