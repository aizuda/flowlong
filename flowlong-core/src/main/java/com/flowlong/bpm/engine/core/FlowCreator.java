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
package com.flowlong.bpm.engine.core;

import lombok.Getter;

import java.io.Serializable;

/**
 * 流程创建者
 *
 * <p>
 * 尊重知识产权，CV 请保留版权，爱组搭 http://aizuda.com 出品，不允许非法使用，后果自负
 * </p>
 *
 * @author hubin
 * @since 1.0
 */
@Getter
public class FlowCreator implements Serializable {
    /**
     * 租户ID
     */
    private String tenantId;
    /**
     * 创建人ID
     */
    private String createId;
    /**
     * 创建人
     */
    private String createBy;

    public static FlowCreator ADMIN = new FlowCreator("0", "管理员");

    public FlowCreator(String createId, String createBy) {
        this.createId = createId;
        this.createBy = createBy;
    }

    public FlowCreator tenantId(String tenantId) {
        this.tenantId = tenantId;
        return this;
    }

    public static FlowCreator of(String createId, String createBy) {
        return new FlowCreator(createId, createBy);
    }
}
