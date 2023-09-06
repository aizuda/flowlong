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
import com.flowlong.bpm.engine.QueryService;
import com.flowlong.bpm.engine.RuntimeService;
import com.flowlong.bpm.engine.TaskService;
import com.flowlong.bpm.engine.assist.Assert;
import com.flowlong.bpm.engine.assist.DateUtils;
import com.flowlong.bpm.engine.assist.ObjectUtils;
import com.flowlong.bpm.engine.core.FlowCreator;
import com.flowlong.bpm.engine.core.enums.EventType;
import com.flowlong.bpm.engine.core.enums.InstanceState;
import com.flowlong.bpm.engine.core.mapper.HisInstanceMapper;
import com.flowlong.bpm.engine.core.mapper.InstanceMapper;
import com.flowlong.bpm.engine.entity.HisInstance;
import com.flowlong.bpm.engine.entity.Instance;
import com.flowlong.bpm.engine.entity.Process;
import com.flowlong.bpm.engine.entity.Task;
import com.flowlong.bpm.engine.listener.InstanceListener;
import com.flowlong.bpm.engine.model.ProcessModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 流程实例运行业务类
 *
 * <p>
 * 尊重知识产权，CV 请保留版权，爱组搭 http://aizuda.com 出品，不允许非法使用，后果自负
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


    public RuntimeServiceImpl(@Autowired(required = false) InstanceListener instanceListener,
                              QueryService queryService, TaskService taskService, InstanceMapper instanceMapper,
                              HisInstanceMapper hisInstanceMapper) {
        this.instanceListener = instanceListener;
        this.queryService = queryService;
        this.taskService = taskService;
        this.instanceMapper = instanceMapper;
        this.hisInstanceMapper = hisInstanceMapper;
    }

    /**
     * 创建活动实例
     */
    @Override
    public Instance createInstance(Process process, FlowCreator flowCreator, Map<String, Object> args) {
        Instance instance = new Instance();
        instance.setCreateTime(DateUtils.getCurrentDate());
        instance.setLastUpdateTime(instance.getCreateTime());
        instance.setCreateId(flowCreator.getCreateId());
        instance.setCreateBy(flowCreator.getCreateBy());
        instance.setLastUpdateBy(instance.getCreateBy());
        instance.setProcessId(process.getId());
        ProcessModel model = process.getProcessModel();
        if (model != null && args != null) {
//            if (ObjectUtils.isNotEmpty(model.getExpireTime())) {
//                instance.setExpireTime(new Date(model.getExpireTime()));
//            }
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
        this.instanceNotify(EventType.create, hisInstance);
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
        hisInstance.setInstanceState(InstanceState.complete);
        hisInstance.setEndTime(DateUtils.getCurrentDate());
        instanceMapper.deleteById(instanceId);
        // 流程实例监听器通知
        this.instanceNotify(EventType.complete, hisInstance);
    }

    protected void instanceNotify(EventType eventType, HisInstance hisInstance) {
        if (null != instanceListener) {
            instanceListener.notify(eventType, hisInstance);
        }
    }

    /**
     * 强制中止活动实例,并强制完成活动任务
     *
     * @param instanceId  流程实例ID
     * @param flowCreator 处理人员
     */
    @Override
    public void terminate(Long instanceId, FlowCreator flowCreator) {
        Instance instance = instanceMapper.selectById(instanceId);
        if (null != instance) {
            // 实例相关任务强制完成
            queryService.getActiveTasksByInstanceId(instanceId).ifPresent(tasks -> {
                for (Task task : tasks) {
                    taskService.complete(task.getId(), flowCreator);
                }
            });

            // 更新历史实例设置状态为终止
            HisInstance hisInstance = HisInstance.of(instance, InstanceState.termination);
            hisInstance.setEndTime(DateUtils.getCurrentDate());
            hisInstanceMapper.updateById(hisInstance);

            // 删除实例
            instanceMapper.deleteById(instanceId);

            // 流程实例监听器通知
            this.instanceNotify(EventType.terminate, hisInstance);
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
                // 删除抄送任务
                // ccInstanceMapper.delete(Wrappers.<CCInstance>lambdaQuery().eq(CCInstance::getInstanceId, t.getId()));

                // 删除代理任务

            });
        }

        // 删除历史实例
        hisInstanceMapper.delete(Wrappers.<HisInstance>lambdaQuery().eq(HisInstance::getProcessId, processId));

        // 删除实例
        instanceMapper.delete(Wrappers.<Instance>lambdaQuery().eq(Instance::getProcessId, processId));
    }

}
