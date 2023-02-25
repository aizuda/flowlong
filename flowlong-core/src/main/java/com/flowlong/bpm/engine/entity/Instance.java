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

import com.flowlong.bpm.engine.assist.JsonUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;

/**
 * 流程实例实体类
 *
 * @author hubin
 * @since 1.0
 */
@Getter
@Setter
@ToString
public class Instance implements Serializable {
    /**
     * 主键ID
     */
    private String id;
    /**
     * 租户ID
     */
    private String tenantId;
    /**
     * 版本
     */
    private Integer version = 0;
    /**
     * 流程定义ID
     */
    private String processId;
    /**
     * 流程实例创建者ID
     */
    private String creator;
    /**
     * 流程实例创建时间
     */
    private String createTime;
    /**
     * 流程实例为子流程时，该字段标识父流程实例ID
     */
    private String parentId;
    /**
     * 流程实例为子流程时，该字段标识父流程哪个节点模型启动的子流程
     */
    private String parentNodeName;
    /**
     * 流程实例期望完成时间
     */
    private String expireTime;
    /**
     * 流程实例上一次更新时间
     */
    private String lastUpdateTime;
    /**
     * 流程实例上一次更新人员ID
     */
    private String lastUpdator;
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

    @SuppressWarnings("unchecked")
    public Map<String, Object> getVariableMap() {
        Map<String, Object> map = JsonUtils.fromJson(this.variable, Map.class);
        if (map == null) return Collections.emptyMap();
        return map;
    }
}
