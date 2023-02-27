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
package com.flowlong.bpm.engine.core.service;

import com.flowlong.bpm.engine.Completion;
import com.flowlong.bpm.engine.FlowLongEngine;
import com.flowlong.bpm.engine.RuntimeService;
import com.flowlong.bpm.engine.assist.DateUtils;
import com.flowlong.bpm.engine.assist.JsonUtils;
import com.flowlong.bpm.engine.assist.StringUtils;
import com.flowlong.bpm.engine.core.FlowLongContext;
import com.flowlong.bpm.engine.core.mapper.CCInstanceMapper;
import com.flowlong.bpm.engine.core.mapper.HisInstanceMapper;
import com.flowlong.bpm.engine.core.mapper.InstanceMapper;
import com.flowlong.bpm.engine.entity.Process;
import com.flowlong.bpm.engine.entity.*;
import com.flowlong.bpm.engine.model.ProcessModel;
import lombok.AllArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * 流程实例运行业务类
 *
 * @author hubin
 * @since 1.0
 */
@AllArgsConstructor
public class RuntimeServiceImpl extends AbstractService implements RuntimeService {
    private FlowLongContext flowLongContext;
    private InstanceMapper instanceMapper;
    private HisInstanceMapper hisInstanceMapper;
    private CCInstanceMapper ccInstanceMapper;

    /**
     * 创建活动实例
     *
     * @see RuntimeServiceImpl#createInstance(Process, String, Map, String, String)
     */
    @Override
    public Instance createInstance(Process process, String operator, Map<String, Object> args) {
        return createInstance(process, operator, args, null, null);
    }

    /**
     * 创建活动实例
     */
    @Override
    public Instance createInstance(Process process, String operator, Map<String, Object> args,
                                   String parentId, String parentNodeName) {
        Instance instance = new Instance();
        instance.setId(StringUtils.getPrimaryKey());
        instance.setParentId(parentId);
        instance.setParentNodeName(parentNodeName);
        instance.setCreateTime(DateUtils.getTime());
        instance.setLastUpdateTime(instance.getCreateTime());
        instance.setCreator(operator);
        instance.setLastUpdator(instance.getCreator());
        instance.setProcessId(process.getId());
        ProcessModel model = process.getModel();
        if (model != null && args != null) {
            if (StringUtils.isNotEmpty(model.getExpireTime())) {
                String expireTime = DateUtils.parseTime(args.get(model.getExpireTime()));
                instance.setExpireTime(expireTime);
            }
            String instanceNo = (String) args.get(FlowLongEngine.ID);
            if (StringUtils.isNotEmpty(instanceNo)) {
                instance.setInstanceNo(instanceNo);
            } else {
                instance.setInstanceNo(model.getGenerator().generate(model));
            }
        }

        instance.setVariable(JsonUtils.toJson(args));
        saveInstance(instance);
        return instance;
    }

    /**
     * 向活动实例临时添加全局变量数据
     *
     * @param instanceId 实例id
     * @param args       变量数据
     */
    @Override
    public void addVariable(String instanceId, Map<String, Object> args) {
        Instance instance = instanceMapper.selectById(instanceId);
        Map<String, Object> data = instance.getVariableMap();
        data.putAll(args);
        Instance temp = new Instance();
        temp.setId(instanceId);
        temp.setVariable(JsonUtils.toJson(data));
        instanceMapper.updateById(temp);
    }

    /**
     * 创建实例的抄送
     */
    @Override
    public void createCCInstance(String instanceId, String creator, String... actorIds) {
        for (String actorId : actorIds) {
            CCInstance ccinstance = new CCInstance();
            ccinstance.setInstanceId(instanceId);
            ccinstance.setActorId(actorId);
            ccinstance.setCreator(creator);
            ccinstance.setStatus(STATE_ACTIVE);
            ccinstance.setCreateTime(DateUtils.getTime());
            ccInstanceMapper.insert(ccinstance);
        }
    }

    /**
     * 流程实例数据会保存至活动实例表、历史实例表
     */
    @Override
    public void saveInstance(Instance instance) {
        instanceMapper.insert(instance);
        hisInstanceMapper.insert(new HisInstance(instance, STATE_ACTIVE));
    }

    /**
     * 更新活动实例的last_Updator、last_Update_Time、expire_Time、version、variable
     */
    @Override
    public void updateInstance(Instance instance) {
        instanceMapper.updateById(instance);
    }

    /**
     * 删除活动流程实例数据，更新历史流程实例的状态、结束时间
     */
    @Override
    public void complete(String instanceId) {
        HisInstance history = new HisInstance();
        history.setId(instanceId);
        history.setInstanceState(STATE_FINISH);
        history.setEndTime(DateUtils.getTime());
        instanceMapper.deleteById(instanceId);
        Completion completion = flowLongContext.getCompletion();
        if (completion != null) {
            completion.complete(history);
        }
    }

    /**
     * 强制中止流程实例
     *
     * @see RuntimeServiceImpl#terminate(String, String)
     */
    @Override
    public void terminate(String instanceId) {
        terminate(instanceId, null);
    }

    /**
     * 强制中止活动实例,并强制完成活动任务
     */
    @Override
    public void terminate(String instanceId, String operator) {
        List<Task> tasks = flowLongContext.getQueryService().getActiveTasksByInstanceId(instanceId);
        for (Task task : tasks) {
            flowLongContext.getTaskService().complete(task.getId(), operator);
        }
        Instance instance = instanceMapper.selectById(instanceId);
        HisInstance history = new HisInstance(instance, STATE_TERMINATION);
        history.setEndTime(DateUtils.getTime());
        instanceMapper.deleteById(instanceId);
        hisInstanceMapper.updateById(history);
        Completion completion = flowLongContext.getCompletion();
        if (completion != null) {
            completion.complete(history);
        }
    }

    /**
     * 级联删除指定流程实例的所有数据：
     * 1.wf_instance,wf_hist_instance
     * 2.wf_task,wf_hist_task
     * 3.wf_task_actor,wf_hist_task_actor
     * 4.wf_cc_instance
     *
     * @param id 实例id
     */
    @Override
    public void cascadeRemove(String id) {
        // 删除所有相关数据
    }
}
