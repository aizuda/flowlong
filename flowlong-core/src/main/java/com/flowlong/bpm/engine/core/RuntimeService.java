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
package com.flowlong.bpm.engine.core;

import com.flowlong.bpm.engine.Completion;
import com.flowlong.bpm.engine.DBAccess;
import com.flowlong.bpm.engine.FlowLongEngine;
import com.flowlong.bpm.engine.IRuntimeService;
import com.flowlong.bpm.engine.access.QueryFilter;
import com.flowlong.bpm.engine.assist.Assert;
import com.flowlong.bpm.engine.assist.DateUtils;
import com.flowlong.bpm.engine.assist.JsonUtils;
import com.flowlong.bpm.engine.assist.StringUtils;
import com.flowlong.bpm.engine.entity.Process;
import com.flowlong.bpm.engine.entity.*;
import com.flowlong.bpm.engine.model.ProcessModel;

import java.util.List;
import java.util.Map;

/**
 * 流程实例运行业务类
 *
 * @author hubin
 * @since 1.0
 */
public class RuntimeService extends AccessService implements IRuntimeService {
    private FlowLongContext flowLongContext;

    public RuntimeService(FlowLongContext flowLongContext, DBAccess dbAccess) {
        this.flowLongContext = flowLongContext;
        this.dbAccess = dbAccess;
    }

    /**
     * 创建活动实例
     *
     * @see RuntimeService#createInstance(Process, String, Map, String, String)
     */
    public Instance createInstance(Process process, String operator, Map<String, Object> args) {
        return createInstance(process, operator, args, null, null);
    }

    /**
     * 创建活动实例
     */
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
    public void addVariable(String instanceId, Map<String, Object> args) {
        Instance instance = access().getInstance(instanceId);
        Map<String, Object> data = instance.getVariableMap();
        data.putAll(args);
        instance.setVariable(JsonUtils.toJson(data));
        access().updateInstanceVariable(instance);
    }

    /**
     * 创建实例的抄送
     */
    public void createCCInstance(String instanceId, String creator, String... actorIds) {
        for (String actorId : actorIds) {
            CCInstance ccinstance = new CCInstance();
            ccinstance.setInstanceId(instanceId);
            ccinstance.setActorId(actorId);
            ccinstance.setCreator(creator);
            ccinstance.setStatus(STATE_ACTIVE);
            ccinstance.setCreateTime(DateUtils.getTime());
            access().saveCCInstance(ccinstance);
        }
    }

    /**
     * 流程实例数据会保存至活动实例表、历史实例表
     */
    public void saveInstance(Instance instance) {
        HisInstance history = new HisInstance(instance, STATE_ACTIVE);
        access().saveInstance(instance);
        access().saveHistory(history);
    }

    /**
     * 更新活动实例的last_Updator、last_Update_Time、expire_Time、version、variable
     */
    public void updateInstance(Instance instance) {
        access().updateInstance(instance);
    }

    /**
     * 更新抄送记录状态为已阅
     */
    public void updateCCStatus(String instanceId, String... actorIds) {
        List<CCInstance> ccInstances = access().getCCInstance(instanceId, actorIds);
        Assert.notNull(ccInstances);
        for (CCInstance ccinstance : ccInstances) {
            ccinstance.setStatus(STATE_FINISH);
            ccinstance.setFinishTime(DateUtils.getTime());
            access().updateCCInstance(ccinstance);
        }
    }

    /**
     * 删除指定的抄送记录
     */
    public void deleteCCInstance(String instanceId, String actorId) {
        List<CCInstance> ccinstances = access().getCCInstance(instanceId, actorId);
        Assert.notNull(ccinstances);
        for (CCInstance ccinstance : ccinstances) {
            access().deleteCCInstance(ccinstance);
        }
    }

    /**
     * 删除活动流程实例数据，更新历史流程实例的状态、结束时间
     */
    public void complete(String instanceId) {
        Instance instance = access().getInstance(instanceId);
        HisInstance history = access().getHistInstance(instanceId);
        history.setInstanceState(STATE_FINISH);
        history.setEndTime(DateUtils.getTime());

        access().updateHistory(history);
        access().deleteInstance(instance);
        Completion completion = flowLongContext.getCompletion();
        if (completion != null) {
            completion.complete(history);
        }
    }

    /**
     * 强制中止流程实例
     *
     * @see RuntimeService#terminate(String, String)
     */
    public void terminate(String instanceId) {
        terminate(instanceId, null);
    }

    /**
     * 强制中止活动实例,并强制完成活动任务
     */
    public void terminate(String instanceId, String operator) {
        List<Task> tasks = flowLongContext.getQueryService().getActiveTasks(new QueryFilter().setInstanceId(instanceId));
        for (Task task : tasks) {
            flowLongContext.getTaskService().complete(task.getId(), operator);
        }
        Instance instance = access().getInstance(instanceId);
        HisInstance history = new HisInstance(instance, STATE_TERMINATION);
        history.setEndTime(DateUtils.getTime());

        access().updateHistory(history);
        access().deleteInstance(instance);
        Completion completion = flowLongContext.getCompletion();
        if (completion != null) {
            completion.complete(history);
        }
    }

    /**
     * 激活已完成的历史流程实例
     *
     * @param instanceId 实例id
     * @return 活动实例对象
     */
    public Instance resume(String instanceId) {
        HisInstance hisInstance = access().getHistInstance(instanceId);
        Instance instance = hisInstance.undo();
        access().saveInstance(instance);
        hisInstance.setInstanceState(STATE_ACTIVE);
        access().updateHistory(hisInstance);

        List<HisTask> histTasks = access().getHistoryTasks(null,
                new QueryFilter().setInstanceId(instanceId));
        if (histTasks != null && !histTasks.isEmpty()) {
            HisTask histTask = histTasks.get(0);
            flowLongContext.getTaskService().resume(histTask.getId(), histTask.getOperator());
        }
        return instance;
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
    public void cascadeRemove(String id) {
        HisInstance hisInstance = access().getHistInstance(id);
        Assert.notNull(hisInstance);
        List<Task> activeTasks = access().getActiveTasks(null, new QueryFilter().setInstanceId(id));
        List<HisTask> hisTasks = access().getHistoryTasks(null, new QueryFilter().setInstanceId(id));
        for (Task task : activeTasks) {
            access().deleteTask(task);
        }
        for (HisTask hisTask : hisTasks) {
            access().deleteHistoryTask(hisTask);
        }
        List<CCInstance> ccInstances = access().getCCInstance(id);
        for (CCInstance ccInstance : ccInstances) {
            access().deleteCCInstance(ccInstance);
        }

        Instance instance = access().getInstance(id);
        access().deleteHistoryInstance(hisInstance);
        if (instance != null) {
            access().deleteInstance(instance);
        }
    }
}
