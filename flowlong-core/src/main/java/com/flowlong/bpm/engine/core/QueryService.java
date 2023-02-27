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

import com.flowlong.bpm.engine.DBAccess;
import com.flowlong.bpm.engine.IQueryService;
import com.flowlong.bpm.engine.access.Page;
import com.flowlong.bpm.engine.access.QueryFilter;
import com.flowlong.bpm.engine.entity.*;
import com.flowlong.bpm.engine.assist.Assert;

import java.util.List;

/**
 * 查询服务实现类
 *
 * @author hubin
 * @since 1.0
 */
public class QueryService extends AccessService implements IQueryService {

    public QueryService(DBAccess dbAccess) {
        this.dbAccess = dbAccess;
    }

    @Override
    public Instance getInstance(String instanceId) {
        return access().getInstance(instanceId);
    }

    @Override
    public Task getTask(String taskId) {
        return access().getTask(taskId);
    }

    @Override
    public String[] getTaskActorsByTaskId(String taskId) {
        List<TaskActor> actors = access().getTaskActorsByTaskId(taskId);
        if (actors == null || actors.isEmpty()) {
            return null;
        }
        String[] actorIds = new String[actors.size()];
        for (int i = 0; i < actors.size(); i++) {
            TaskActor ta = actors.get(i);
            actorIds[i] = ta.getActorId();
        }
        return actorIds;
    }

    @Override
    public String[] getHistoryTaskActorsByTaskId(String taskId) {
        List<HisTaskActor> actors = access().getHistTaskActorsByTaskId(taskId);
        if (actors == null || actors.isEmpty()) {
            return null;
        }
        String[] actorIds = new String[actors.size()];
        for (int i = 0; i < actors.size(); i++) {
            HisTaskActor ta = actors.get(i);
            actorIds[i] = ta.getActorId();
        }
        return actorIds;
    }

    @Override
    public HisInstance getHistInstance(String instanceId) {
        return access().getHistInstance(instanceId);
    }

    @Override
    public HisTask getHistTask(String taskId) {
        return access().getHistTask(taskId);
    }

    @Override
    public List<Task> getActiveTasks(QueryFilter filter) {
        Assert.notNull(filter);
        return access().getActiveTasks(null, filter);
    }

    @Override
    public List<Task> getActiveTasks(Page<Task> page, QueryFilter filter) {
        Assert.notNull(filter);
        return access().getActiveTasks(page, filter);
    }

    @Override
    public List<Instance> getActiveInstances(QueryFilter filter) {
        Assert.notNull(filter);
        return access().getActiveInstances(null, filter);
    }

    @Override
    public List<Instance> getActiveInstances(Page<Instance> page, QueryFilter filter) {
        Assert.notNull(filter);
        return access().getActiveInstances(page, filter);
    }

    @Override
    public List<HisInstance> getHistoryInstances(QueryFilter filter) {
        Assert.notNull(filter);
        return access().getHistoryInstances(null, filter);
    }

    @Override
    public List<HisInstance> getHistoryInstances(Page<HisInstance> page, QueryFilter filter) {
        Assert.notNull(filter);
        return access().getHistoryInstances(page, filter);
    }

    @Override
    public List<HisTask> getHistoryTasks(QueryFilter filter) {
        Assert.notNull(filter);
        return access().getHistoryTasks(null, filter);
    }

    @Override
    public List<HisTask> getHistoryTasks(Page<HisTask> page, QueryFilter filter) {
        Assert.notNull(filter);
        return access().getHistoryTasks(page, filter);
    }

    @Override
    public List<WorkItem> getWorkItems(Page<WorkItem> page, QueryFilter filter) {
        Assert.notNull(filter);
        return access().getWorkItems(page, filter);
    }

    @Override
    public List<HisInstance> getCCWorks(Page<HisInstance> page, QueryFilter filter) {
        Assert.notNull(filter);
        return access().getCCWorks(page, filter);
    }

    @Override
    public List<WorkItem> getHistoryWorkItems(Page<WorkItem> page, QueryFilter filter) {
        Assert.notNull(filter);
        return access().getHistoryWorkItems(page, filter);
    }

    @Override
    public <T> T nativeQueryObject(Class<T> T, String sql, Object... args) {
        Assert.notEmpty(sql);
        Assert.notNull(T);
        return access().queryObject(T, sql, args);
    }

    @Override
    public <T> List<T> nativeQueryList(Class<T> T, String sql, Object... args) {
        Assert.notEmpty(sql);
        Assert.notNull(T);
        return access().queryList(T, sql, args);
    }

    @Override
    public <T> List<T> nativeQueryList(Page<T> page, Class<T> T, String sql,
                                       Object... args) {
        Assert.notEmpty(sql);
        Assert.notNull(T);
        return access().queryList(page, new QueryFilter(), T, sql, args);
    }
}
