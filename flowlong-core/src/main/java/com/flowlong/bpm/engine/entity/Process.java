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

import com.flowlong.bpm.engine.exception.FlowLongException;
import com.flowlong.bpm.engine.assist.StreamUtils;
import com.flowlong.bpm.engine.model.ProcessModel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.InputStream;
import java.io.Serializable;
import java.sql.Blob;

/**
 * 流程定义实体类
 *
 * @author hubin
 * @since 1.0
 */
@Getter
@Setter
@ToString
public class Process implements Serializable {
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
    private Integer version;
    /**
     * 流程定义名称
     */
    private String name;
    /**
     * 流程定义显示名称
     */
    private String displayName;
    /**
     * 流程定义类型（预留字段）
     */
    private String type;
    /**
     * 当前流程的实例url（一般为流程第一步的url）
     * 该字段可以直接打开流程申请的表单
     */
    private String instanceUrl;
    /**
     * 是否可用的开关
     */
    private Integer state;
    /**
     * 创建时间
     */
    private String createTime;
    /**
     * 创建人
     */
    private String creator;
    /**
     * 流程定义模型
     */
    private ProcessModel model;
    /**
     * 流程定义xml
     */
    private Blob content;
    /**
     * 流程定义字节数组
     */
    private byte[] bytes;

    /**
     * setter name/displayName/instanceUrl
     *
     * @param processModel
     */
    public void setModel(ProcessModel processModel) {
        this.model = processModel;
        this.name = processModel.getName();
        this.displayName = processModel.getDisplayName();
        this.instanceUrl = processModel.getInstanceUrl();
    }

    public byte[] getDBContent() {
        if (this.content != null) {
            try {
                return this.content.getBytes(1L, Long.valueOf(this.content.length()).intValue());
            } catch (Exception e) {
                try {
                    InputStream is = content.getBinaryStream();
                    return StreamUtils.readBytes(is);
                } catch (Exception e1) {
                    throw new FlowLongException("couldn't extract stream out of blob", e1);
                }
            }
        }
        return bytes;
    }

}
