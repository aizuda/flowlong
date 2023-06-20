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

import com.flowlong.bpm.engine.core.FlowCreator;
import com.flowlong.bpm.engine.entity.Instance;
import com.flowlong.bpm.engine.entity.Process;

import java.util.Map;

/**
 * 流程实例运行业务类
 *
 * <p>
 * 尊重知识产权，CV 请保留版权，爱组搭 http://aizuda.com 出品
 * </p>
 *
 * @author hubin
 * @since 1.0
 */
public interface RuntimeService {

    /**
     * 根据流程、创建人员、父流程实例ID创建流程实例
     *
     * @param process     流程定义对象
     * @param flowCreator 流程实例任务创建者
     * @param args        参数列表
     * @return 活动流程实例对象
     */
    Instance createInstance(Process process, FlowCreator flowCreator, Map<String, Object> args);

    /**
     * 向指定实例id添加全局变量数据
     *
     * @param instanceId 实例id
     * @param args       变量数据
     */
    void addVariable(Long instanceId, Map<String, Object> args);

    /**
     * 流程实例正常完成
     *
     * @param instanceId 流程实例ID
     */
    void complete(Long instanceId);

    /**
     * 保存流程实例
     *
     * @param instance 流程实例对象
     */
    void saveInstance(Instance instance);

    /**
     * 流程实例强制终止
     *
     * @param instanceId 流程实例ID
     */
    default void terminate(Long instanceId) {
        this.terminate(instanceId, FlowCreator.ADMIN);
    }

    /**
     * 流程实例强制终止
     *
     * @param instanceId  流程实例ID
     * @param flowCreator 处理人员
     */
    void terminate(Long instanceId, FlowCreator flowCreator);

    /**
     * 更新流程实例
     *
     * @param instance 流程实例对象
     */
    void updateInstance(Instance instance);

    /**
     * 级联删除指定流程实例的所有数据
     *
     * @param processId 流程ID
     */
    void cascadeRemoveByProcessId(Long processId);
}
