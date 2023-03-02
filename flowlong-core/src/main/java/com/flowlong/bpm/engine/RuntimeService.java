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
package com.flowlong.bpm.engine;

import com.flowlong.bpm.engine.entity.Instance;
import com.flowlong.bpm.engine.entity.Process;

import java.util.Map;

/**
 * 流程实例运行业务类
 *
 * @author hubin
 * @since 1.0
 */
public interface RuntimeService {

    /**
     * 根据流程、操作人员、父流程实例ID创建流程实例
     *
     * @param process  流程定义对象
     * @param operator 操作人员ID
     * @param args     参数列表
     * @return Instance 活动流程实例对象
     */
    Instance createInstance(Process process, String operator, Map<String, Object> args);

    /**
     * 根据流程、操作人员、父流程实例ID创建流程实例
     *
     * @param process        流程定义对象
     * @param createBy       操作人员ID
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
     * @param args    变量数据
     */
    void addVariable(Long instanceId, Map<String, Object> args);

    /**
     * 创建抄送实例
     *
     * @param instanceId  流程实例id
     * @param actorIds 参与者id
     * @param creator  创建人id
     */
    void createCCInstance(Long instanceId, String creator, String... actorIds);

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
     * @param instanceId  流程实例id
     * @param operator 处理人员
     */
    void terminate(String instanceId, String operator);

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
