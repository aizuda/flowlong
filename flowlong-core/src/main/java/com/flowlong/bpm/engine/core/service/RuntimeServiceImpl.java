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
import com.flowlong.bpm.engine.assist.Assert;
import com.flowlong.bpm.engine.assist.DateUtils;
import com.flowlong.bpm.engine.assist.StringUtils;
import com.flowlong.bpm.engine.core.FlowLongContext;
import com.flowlong.bpm.engine.core.enums.InstanceState;
import com.flowlong.bpm.engine.core.mapper.*;
import com.flowlong.bpm.engine.entity.*;
import com.flowlong.bpm.engine.entity.Process;
import com.flowlong.bpm.engine.listener.InstanceListener;
import com.flowlong.bpm.engine.listener.TaskListener;
import com.flowlong.bpm.engine.model.ProcessModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    private HisTaskMapper hisTaskMapper;
    private TaskMapper taskMapper;
    private HisTaskActorMapper hisTaskActorMapper;
    private TaskActorMapper taskActorMapper;


    public RuntimeServiceImpl(@Autowired(required = false) InstanceListener instanceListener, InstanceMapper instanceMapper,
                              HisInstanceMapper hisInstanceMapper, CCInstanceMapper ccInstanceMapper,
                              HisTaskMapper hisTaskMapper, TaskMapper taskMapper,
                              HisTaskActorMapper hisTaskActorMapper, TaskActorMapper taskActorMapper) {
        this.instanceMapper = instanceMapper;
        this.hisInstanceMapper = hisInstanceMapper;
        this.ccInstanceMapper = ccInstanceMapper;
        this.instanceListener = instanceListener;
        this.hisTaskMapper = hisTaskMapper;
        this.taskMapper = taskMapper;
        this.hisTaskActorMapper = hisTaskActorMapper;
        this.taskActorMapper = taskActorMapper;
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
            CCInstance ccInstance = new CCInstance();
            ccInstance.setInstanceId(instanceId);
            ccInstance.setActorId(actorId);
            ccInstance.setCreateBy(createBy);
            ccInstance.setInstanceState(InstanceState.active);
            ccInstance.setCreateTime(new Date());
            ccInstanceMapper.insert(ccInstance);
        }
    }

    /**
     * 结束抄送实例
     *
     * @param instanceId    流程实例ID
     * @param actorIds      参与者ID
     * @return 更新是否成功
     */
    @Override
    public boolean finishCCInstance(Long instanceId, List<String> actorIds) {
        CCInstance ccInstance = new CCInstance();
        ccInstance.setInstanceState(InstanceState.finish);
        ccInstance.setFinishTime(DateUtils.getTime());
        return ccInstanceMapper.update(ccInstance, Wrappers.<CCInstance>lambdaUpdate()
                .eq(CCInstance::getInstanceId, instanceId)
                .in(CCInstance::getActorId, actorIds)) > 0;
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
     * 级联删除指定流程实例的所有数据
     *
     * 1.flw_instance,flw_his_instance
     * 2.flw_task, flw_his_task
     * 3.flw_task_actor,flw_his_task_actor
     * 4.flw_cc_instance
     *
     * @param id 实例 id
     */
    @Override
    public void cascadeRemove(Long id) {
        // 获取历史实例
        HisInstance hisInstance = hisInstanceMapper.selectById(id);
        Assert.notNull(hisInstance);
        // 删除活动任务相关信息：flw_task, flw_task_actor
        List<Task> activeTasks = taskMapper.selectList(Wrappers.<Task>lambdaQuery().eq(Task::getInstanceId, id));
        if(activeTasks.size() > 0){
            List<Long> taskIds = activeTasks.stream().map(Task::getId).collect(Collectors.toList());
            taskActorMapper.delete(Wrappers.<TaskActor>lambdaQuery().in(TaskActor::getTaskId, taskIds));
            taskMapper.delete(Wrappers.<Task>lambdaQuery().in(Task::getId, taskIds));
        }

        // 删除历史完成任务相关信息：flw_his_task,flw_his_task_actor
        List<HisTask> hisTasks = hisTaskMapper.selectList(Wrappers.<HisTask>lambdaQuery().eq(HisTask::getInstanceId, id));
        if(hisTasks.size() > 0){
            List<Long> hisTaskIds = hisTasks.stream().map(HisTask::getId).collect(Collectors.toList());
            hisTaskActorMapper.delete(Wrappers.<HisTaskActor>lambdaQuery().in(HisTaskActor::getTaskId, hisTaskIds));
            hisTaskMapper.delete(Wrappers.<HisTask>lambdaQuery().in(HisTask::getId, hisTaskIds));
        }

        // 删除抄送实例列表
        List<CCInstance> ccInstances = ccInstanceMapper.selectList(Wrappers.<CCInstance>lambdaQuery().eq(CCInstance::getInstanceId, id));
        if(ccInstances.size() > 0){
            List<Long> ccIds = ccInstances.stream().map(CCInstance::getId).collect(Collectors.toList());
            ccInstanceMapper.delete(Wrappers.<CCInstance>lambdaQuery().in(CCInstance::getId, ccIds));
        }
        // 删除实例以及历史实例
        hisInstanceMapper.deleteById(hisInstance);
        instanceMapper.deleteById(id);

    }
}
