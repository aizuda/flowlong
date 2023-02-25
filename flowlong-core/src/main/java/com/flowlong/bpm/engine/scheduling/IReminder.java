/*
 *  Copyright 2023-2025 www.flowlong.com
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package com.flowlong.bpm.engine.scheduling;

import com.flowlong.bpm.engine.entity.Process;
import com.flowlong.bpm.engine.model.NodeModel;

import java.util.Map;

/**
 * 提醒接口
 *
 * @author hubin
 * @since 1.0
 */
public interface IReminder {

    /**
     * 提醒操作
     *
     * @param process   流程定义对象
     * @param orderId   流程实例id
     * @param taskId    任务id
     * @param nodeModel 节点模型
     * @param data      数据
     */
    void remind(Process process, String orderId, String taskId, NodeModel nodeModel, Map<String, Object> data);
}
