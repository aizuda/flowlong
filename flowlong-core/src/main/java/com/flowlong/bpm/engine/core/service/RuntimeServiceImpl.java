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
import com.flowlong.bpm.engine.QueryService;
import com.flowlong.bpm.engine.RuntimeService;
import com.flowlong.bpm.engine.TaskService;
import com.flowlong.bpm.engine.assist.Assert;
import com.flowlong.bpm.engine.assist.DateUtils;
import com.flowlong.bpm.engine.assist.ObjectUtils;
import com.flowlong.bpm.engine.core.enums.InstanceState;
import com.flowlong.bpm.engine.core.mapper.HisInstanceMapper;
import com.flowlong.bpm.engine.core.mapper.InstanceMapper;
import com.flowlong.bpm.engine.core.mapper.SurrogateMapper;
import com.flowlong.bpm.engine.entity.Process;
import com.flowlong.bpm.engine.entity.*;
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
    private InstanceListener instanceListener;
    private QueryService queryService;
    private TaskService taskService;
    private InstanceMapper instanceMapper;
    private HisInstanceMapper hisInstanceMapper;
    private SurrogateMapper surrogateMapper;


    public RuntimeServiceImpl(@Autowired(required = false) InstanceListener instanceListener,
                              QueryService queryService, TaskService taskService, InstanceMapper instanceMapper,
                              HisInstanceMapper hisInstanceMapper, SurrogateMapper surrogateMapper) {
        this.instanceListener = instanceListener;
        this.queryService = queryService;
        this.taskService = taskService;
        this.instanceMapper = instanceMapper;
        this.hisInstanceMapper = hisInstanceMapper;
        this.surrogateMapper = surrogateMapper;
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
        instance.setCreateTime(DateUtils.getCurrentDate());
        instance.setLastUpdateTime(instance.getCreateTime());
        instance.setCreateBy(createBy);
        instance.setLastUpdateBy(instance.getCreateBy());
        instance.setProcessId(process.getId());
        ProcessModel model = process.getProcessModel();
        if (model != null && args != null) {
//            if (ObjectUtils.isNotEmpty(model.getExpireTime())) {
//                instance.setExpireTime(new Date(model.getExpireTime()));
//            }
            String instanceNo = (String) args.get(FlowLongEngine.ID);
            if (ObjectUtils.isNotEmpty(instanceNo)) {
                instance.setInstanceNo(instanceNo);
            }
        }

        instance.setVariable(args);
        this.saveInstance(instance);
        return instance;
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
        temp.setVariable(data);
        instanceMapper.updateById(temp);
    }

    /**
     * 流程实例数据会保存至活动实例表、历史实例表
     *
     * @param instance 流程实例对象
     */
    @Override
    public void saveInstance(Instance instance) {
        // 保存实例
        instanceMapper.insert(instance);

        // 保存历史实例设置为活的状态
        HisInstance hisInstance = HisInstance.of(instance, InstanceState.active);
        hisInstanceMapper.insert(hisInstance);

        // 流程实例监听器通知
        this.instanceNotify(TaskListener.EVENT_CREATE, hisInstance);
    }

    /**
     * 更新活动实例
     */
    @Override
    public void updateInstance(Instance instance) {
        Assert.illegalArgument(null == instance || null == instance.getId(),
                "instance id cannot be empty");
        instanceMapper.updateById(instance);
    }

    /**
     * 删除活动流程实例数据，更新历史流程实例的状态、结束时间
     */
    @Override
    public void complete(Long instanceId) {
        HisInstance hisInstance = new HisInstance();
        hisInstance.setId(instanceId);
        hisInstance.setInstanceState(InstanceState.finish.getValue());
        hisInstance.setEndTime(DateUtils.getCurrentDate());
        instanceMapper.deleteById(instanceId);
        // 流程实例监听器通知
        this.instanceNotify(TaskListener.EVENT_COMPLETE, hisInstance);
    }

    protected void instanceNotify(String event, HisInstance hisInstance) {
        if (null != instanceListener) {
            instanceListener.notify(event, hisInstance);
        }
    }

    /**
     * 强制中止流程实例
     *
     * @param instanceId 流程实例ID
     */
    @Override
    public void terminate(Long instanceId) {
        this.terminate(instanceId, FlowLongEngine.ADMIN);
    }

    /**
     * 强制中止活动实例,并强制完成活动任务
     *
     * @param instanceId 流程实例ID
     * @param createBy   处理人员
     */
    @Override
    public void terminate(Long instanceId, String createBy) {
        Instance instance = instanceMapper.selectById(instanceId);
        if (null != instance) {
            // 实例相关任务强制完成
            List<Task> tasks = queryService.getActiveTasksByInstanceId(instanceId);
            for (Task task : tasks) {
                taskService.complete(task.getId(), createBy);
            }

            // 更新历史实例设置状态为终止
            HisInstance hisInstance = HisInstance.of(instance, InstanceState.termination);
            hisInstance.setEndTime(DateUtils.getCurrentDate());
            hisInstanceMapper.updateById(hisInstance);

            // 删除实例
            instanceMapper.deleteById(instanceId);

            // 流程实例监听器通知
            this.instanceNotify(TaskListener.EVENT_TERMINATE, hisInstance);
        }
    }

    /**
     * 级联删除指定流程实例的所有数据
     *
     * @param processId 流程ID
     */
    @Override
    public void cascadeRemoveByProcessId(Long processId) {
        List<HisInstance> hisInstances = hisInstanceMapper.selectList(Wrappers.<HisInstance>lambdaQuery()
                .eq(HisInstance::getProcessId, processId));
        if (ObjectUtils.isNotEmpty(hisInstances)) {
            hisInstances.forEach(t -> {
                // 删除活动任务相关信息
                taskService.cascadeRemoveByInstanceId(t.getId());
                // 删除抄送实例列表
                // ccInstanceMapper.delete(Wrappers.<CCInstance>lambdaQuery().eq(CCInstance::getInstanceId, t.getId()));
            });
        }

        // 删除历史实例
        hisInstanceMapper.delete(Wrappers.<HisInstance>lambdaQuery().eq(HisInstance::getProcessId, processId));

        // 删除实例
        instanceMapper.delete(Wrappers.<Instance>lambdaQuery().eq(Instance::getProcessId, processId));

        // 删除与流程相关的委托代理
        surrogateMapper.delete(Wrappers.<Surrogate>lambdaQuery().eq(Surrogate::getProcessId, processId));
    }

}
