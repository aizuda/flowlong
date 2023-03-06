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

import com.flowlong.bpm.engine.core.enums.InstanceState;
import com.flowlong.bpm.engine.entity.Instance;
import com.flowlong.bpm.engine.entity.Process;

import java.util.Arrays;
import java.util.List;
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
     * @param process  流程定义对象
     * @param createBy 创建人员ID
     * @param args     参数列表
     * @return Instance 活动流程实例对象
     */
    Instance createInstance(Process process, String createBy, Map<String, Object> args);

    /**
     * 根据流程、创建人员、父流程实例ID创建流程实例
     *
     * @param process        流程定义对象
     * @param createBy       创建人员ID
     * @param args           参数列表
     * @param parentId       父流程实例ID
     * @param parentNodeName 父流程节点模型
     * @return 活动流程实例对象
     */
    Instance createInstance(Process process, String createBy, Map<String, Object> args, Long parentId, String parentNodeName);

    /**
     * 向指定实例id添加全局变量数据
     *
     * @param instanceId 实例id
     * @param args       变量数据
     */
    void addVariable(Long instanceId, Map<String, Object> args);

    /**
     * 创建抄送实例
     *
     * @param instanceId 流程实例ID
     * @param createBy   创建人ID
     * @param actorIds   参与者ID集合
     */
    void createCCInstance(Long instanceId, String createBy, List<String> actorIds);

    /**
     * 创建抄送实例
     *
     * @param instanceId 流程实例ID
     * @param createBy   创建人ID
     * @param actorId    参与者ID
     */
    default void createCCInstance(Long instanceId, String createBy, String actorId) {
        this.createCCInstance(instanceId, createBy, Arrays.asList(actorId));
    }

    /**
     * 结束抄送实例
     *
     * @param instanceId    流程实例ID
     * @param actorIds      参与者ID
     * @return 更新是否成功
     */
    boolean finishCCInstance(Long instanceId, List<String> actorIds);

    default boolean finishCCInstance(Long instanceId, String actorId) {
        return this.finishCCInstance(instanceId, Arrays.asList(actorId));
    }

    /**
     * 删除抄送记录
     *
     * @param instanceId 流程实例id
     * @param actorId    参与者id
     */
    void deleteCCInstance(Long instanceId, String actorId);

    /**
     * 流程实例正常完成
     *
     * @param instanceId 流程实例id
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
     * @param instanceId 流程实例id
     */
    void terminate(String instanceId);

    /**
     * 流程实例强制终止
     *
     * @param instanceId 流程实例id
     * @param createBy   处理人员
     */
    void terminate(String instanceId, String createBy);

    /**
     * 更新流程实例
     *
     * @param instance 流程实例对象
     */
    void updateInstance(Instance instance);

    /**
     * 级联删除指定流程实例的所有数据：
     * 1.wf_instance,wf_hist_instance
     * 2.wf_task,wf_hist_task
     * 3.wf_task_actor,wf_hist_task_actor
     * 4.wf_cc_instance
     *
     * @param id
     */
    void cascadeRemove(String id);
}
