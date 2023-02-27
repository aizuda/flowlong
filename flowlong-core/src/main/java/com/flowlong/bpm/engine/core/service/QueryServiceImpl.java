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

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.flowlong.bpm.engine.QueryService;
import com.flowlong.bpm.engine.core.mapper.*;
import com.flowlong.bpm.engine.entity.*;
import lombok.AllArgsConstructor;

import java.util.List;

/**
 * 查询服务实现类
 *
 * @author hubin
 * @since 1.0
 */
@AllArgsConstructor
public class QueryServiceImpl extends AbstractService implements QueryService {
    private InstanceMapper instanceMapper;
    private HisInstanceMapper hisInstanceMapper;
    private TaskMapper taskMapper;
    private TaskActorMapper taskActorMapper;
    private HisTaskMapper hisTaskMapper;
    private HisTaskActorMapper hisTaskActorMapper;

    @Override
    public Instance getInstance(String instanceId) {
        return instanceMapper.selectById(instanceId);
    }

    @Override
    public Task getTask(String taskId) {
        return taskMapper.selectById(taskId);
    }

    @Override
    public String[] getTaskActorsByTaskId(String taskId) {
        List<TaskActor> actors = taskActorMapper.selectList(Wrappers.<TaskActor>lambdaQuery().eq(TaskActor::getTaskId, taskId));
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
        List<HisTaskActor> actors = hisTaskActorMapper.selectList(Wrappers.<HisTaskActor>lambdaQuery().eq(HisTaskActor::getTaskId, taskId));
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
        return hisInstanceMapper.selectById(instanceId);
    }

    @Override
    public HisTask getHistTask(String taskId) {
        return hisTaskMapper.selectById(taskId);
    }

    @Override
    public List<Task> getTasksByInstanceId(String instanceId) {
        return taskMapper.selectList(Wrappers.<Task>lambdaQuery().eq(Task::getInstanceId, instanceId));
    }

    @Override
    public List<Task> getActiveTasksByInstanceId(String instanceId) {
        return taskMapper.selectList(Wrappers.<Task>lambdaQuery().eq(Task::getInstanceId, instanceId)
                .eq(Task::getModel, 1));
    }
}
