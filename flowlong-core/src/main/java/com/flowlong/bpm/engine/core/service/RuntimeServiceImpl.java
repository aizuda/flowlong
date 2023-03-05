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
package com.flowlong.bpm.engine.core.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.flowlong.bpm.engine.FlowLongEngine;
import com.flowlong.bpm.engine.RuntimeService;
import com.flowlong.bpm.engine.assist.DateUtils;
import com.flowlong.bpm.engine.assist.StringUtils;
import com.flowlong.bpm.engine.core.FlowLongContext;
import com.flowlong.bpm.engine.core.enums.InstanceState;
import com.flowlong.bpm.engine.core.mapper.CCInstanceMapper;
import com.flowlong.bpm.engine.core.mapper.HisInstanceMapper;
import com.flowlong.bpm.engine.core.mapper.InstanceMapper;
import com.flowlong.bpm.engine.entity.CCInstance;
import com.flowlong.bpm.engine.entity.HisInstance;
import com.flowlong.bpm.engine.entity.Instance;
import com.flowlong.bpm.engine.entity.Process;
import com.flowlong.bpm.engine.listener.InstanceListener;
import com.flowlong.bpm.engine.listener.TaskListener;
import com.flowlong.bpm.engine.model.ProcessModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
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
@Service
public class RuntimeServiceImpl implements RuntimeService {
    private InstanceMapper instanceMapper;
    private HisInstanceMapper hisInstanceMapper;
    private CCInstanceMapper ccInstanceMapper;
    private InstanceListener instanceListener;

    public RuntimeServiceImpl(@Autowired(required = false) InstanceListener instanceListener, InstanceMapper instanceMapper,
                              HisInstanceMapper hisInstanceMapper, CCInstanceMapper ccInstanceMapper) {
        this.instanceMapper = instanceMapper;
        this.hisInstanceMapper = hisInstanceMapper;
        this.ccInstanceMapper = ccInstanceMapper;
        this.instanceListener = instanceListener;
    }

    /**
     * 创建活动实例
     *
     * @param process  流程定义对象
     * @param createBy 创建人员ID
     * @param args     参数列表
     * @return
     */
    @Override
    public Instance createInstance(Process process, String createBy, Map<String, Object> args) {
        return createInstance(process, createBy, args, null, null);
    }

    /**
     * 创建活动实例
     *
     * @param process        流程定义对象
     * @param createBy       创建人员ID
     * @param args           参数列表
     * @param parentId       父流程实例ID
     * @param parentNodeName 父流程节点模型
     * @return
     */
    @Override
    public Instance createInstance(Process process, String createBy, Map<String, Object> args,
                                   Long parentId, String parentNodeName) {
        Instance instance = new Instance();
        instance.setParentId(parentId);
        instance.setParentNodeName(parentNodeName);
        instance.setCreateTime(new Date());
        instance.setLastUpdateTime(instance.getCreateTime());
        instance.setCreateBy(createBy);
        instance.setLastUpdateBy(instance.getCreateBy());
        instance.setProcessId(process.getId());
        ProcessModel model = process.getProcessModel();
        if (model != null && args != null) {
            if (StringUtils.isNotEmpty(model.getExpireTime())) {
                instance.setExpireTime(new Date(model.getExpireTime()));
            }
            String instanceNo = (String) args.get(FlowLongEngine.ID);
            if (StringUtils.isNotEmpty(instanceNo)) {
                instance.setInstanceNo(instanceNo);
            } else {
                instance.setInstanceNo(model.getGenerator().generate(model));
            }
        }

        instance.setVariable(FlowLongContext.JSON_HANDLER.toJson(args));
        this.saveInstance(instance);
        return instance;
    }

    /**
     * 创建抄送实例
     *
     * @param instanceId 流程实例ID
     * @param createBy   创建人ID
     * @param actorIds   参与者ID集合
     */
    @Override
    public void createCCInstance(Long instanceId, String createBy, List<String> actorIds) {
        for (String actorId : actorIds) {
            CCInstance ccinstance = new CCInstance();
            ccinstance.setInstanceId(instanceId);
            ccinstance.setActorId(actorId);
            ccinstance.setCreateBy(createBy);
            ccinstance.setInstanceState(InstanceState.active);
            ccinstance.setCreateTime(new Date());
            ccInstanceMapper.insert(ccinstance);
        }
    }

    @Override
    public void updateCCStatus(Long instanceId, List<String> actorIds) {
        CCInstance ccInstance = new CCInstance();
        ccInstance.setInstanceState(InstanceState.finish);
        ccInstance.setFinishTime(DateUtils.getTime());
        ccInstanceMapper.update(ccInstance, Wrappers.<CCInstance>lambdaUpdate()
                .eq(CCInstance::getInstanceId, instanceId)
                .in(CCInstance::getActorId, actorIds));
    }

    @Override
    public void deleteCCInstance(Long instanceId, String actorId) {
        ccInstanceMapper.delete(Wrappers.<CCInstance>lambdaUpdate()
                .eq(CCInstance::getInstanceId, instanceId)
                .eq(CCInstance::getActorId, actorId));
    }

    /**
     * 向活动实例临时添加全局变量数据
     *
     * @param instanceId 实例id
     * @param args       变量数据
     */
    @Override
    public void addVariable(Long instanceId, Map<String, Object> args) {
        Instance instance = instanceMapper.selectById(instanceId);
        Map<String, Object> data = instance.getVariableMap();
        data.putAll(args);
        Instance temp = new Instance();
        temp.setId(instanceId);
        temp.setVariable(FlowLongContext.JSON_HANDLER.toJson(data));
        instanceMapper.updateById(temp);
    }

    /**
     * 流程实例数据会保存至活动实例表、历史实例表
     *
     * @param instance 流程实例对象
     */
    @Override
    public void saveInstance(Instance instance) {
        instanceMapper.insert(instance);
        hisInstanceMapper.insert(new HisInstance(instance, InstanceState.active));
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
    public void complete(Long instanceId) {
        HisInstance his = new HisInstance();
        his.setId(instanceId);
        his.setInstanceState(InstanceState.finish.getValue());
        his.setEndTime(new Date());
        instanceMapper.deleteById(instanceId);
        this.instanceNotify(TaskListener.EVENT_COMPLETE, his);
    }

    protected void instanceNotify(String event, HisInstance hisInstance) {
        if (null != instanceListener) {
            instanceListener.notify(event, hisInstance);
        }
    }

    /**
     * 强制中止流程实例
     *
     * @see RuntimeServiceImpl#terminate(String, String)
     */
    @Override
    public void terminate(String instanceId) {
        this.terminate(instanceId, null);
    }

    /**
     * 强制中止活动实例,并强制完成活动任务
     */
    @Override
    public void terminate(String instanceId, String createBy) {
//        List<Task> tasks = queryService.getActiveTasksByInstanceId(instanceId);
//        for (Task task : tasks) {
//            taskService.complete(task.getId(), createBy);
//        }
        Instance instance = instanceMapper.selectById(instanceId);
        HisInstance his = new HisInstance(instance, InstanceState.termination);
        his.setEndTime(new Date());
        instanceMapper.deleteById(instanceId);
        hisInstanceMapper.updateById(his);
        this.instanceNotify(TaskListener.EVENT_TERMINATE, his);
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
