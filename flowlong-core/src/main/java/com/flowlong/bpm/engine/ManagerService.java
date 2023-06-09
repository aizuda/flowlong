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
package com.flowlong.bpm.engine;

import com.flowlong.bpm.engine.entity.TaskDelegate;

/**
 * 管理服务接口,用于流程管理控制服务
 * 委托管理
 * 时限控制
 *
 * <p>
 * 尊重知识产权，CV 请保留版权，爱组搭 http://aizuda.com 出品
 * </p>
 *
 * @author hubin
 * @since 1.0
 */
public interface ManagerService {
    /**
     * 保存或更新委托代理对象
     *
     * @param taskDelegate 委托代理对象
     */
    void saveOrUpdate(TaskDelegate taskDelegate);

    /**
     * 删除委托代理对象
     *
     * @param id 委托代理ID
     */
    void deleteSurrogate(String id);

    /**
     * 根据ID查询委托代理对象
     *
     * @param id ID
     * @return surrogate 委托代理对象
     */
    TaskDelegate getSurrogate(String id);

    /**
     * 根据授权人、流程名称获取最终代理人
     * 如存在user1->user2->user3，那么最终返回user3
     *
     * @param createBy    授权人
     * @param processName 流程名称
     * @return String 代理人
     */
    String getSurrogate(String createBy, String processName);
}
