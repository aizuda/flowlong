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
import com.flowlong.bpm.engine.core.mapper.*;
import com.flowlong.bpm.engine.entity.*;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 查询服务实现类
 *
 * <p>
 * 尊重知识产权，CV 请保留版权，爱组搭 http://aizuda.com 出品
 * </p>
 *
 * @author hubin
 * @since 1.0
 */
@Service
public class QueryServiceImpl implements QueryService {
    private InstanceMapper instanceMapper;
    private HisInstanceMapper hisInstanceMapper;
    private TaskMapper taskMapper;
    private TaskActorMapper taskActorMapper;
    private HisTaskMapper hisTaskMapper;
    private HisTaskActorMapper hisTaskActorMapper;

    public QueryServiceImpl(InstanceMapper instanceMapper, HisInstanceMapper hisInstanceMapper,
                            TaskMapper taskMapper, TaskActorMapper taskActorMapper,
                            HisTaskMapper hisTaskMapper, HisTaskActorMapper hisTaskActorMapper) {
        this.instanceMapper = instanceMapper;
        this.hisInstanceMapper = hisInstanceMapper;
        this.taskMapper = taskMapper;
        this.taskActorMapper = taskActorMapper;
        this.hisTaskMapper = hisTaskMapper;
        this.hisTaskActorMapper = hisTaskActorMapper;
    }

    @Override
    public Instance getInstance(Long instanceId) {
        return instanceMapper.selectById(instanceId);
    }

    @Override
    public Task getTask(Long taskId) {
        return taskMapper.selectById(taskId);
    }

    @Override
    public HisInstance getHistInstance(Long instanceId) {
        return hisInstanceMapper.selectById(instanceId);
    }

    @Override
    public HisTask getHistTask(Long taskId) {
        return hisTaskMapper.selectById(taskId);
    }

    @Override
    public List<Task> getTasksByInstanceId(Long instanceId) {
        return taskMapper.selectList(Wrappers.<Task>lambdaQuery().eq(Task::getInstanceId, instanceId));
    }

    @Override
    public List<Task> getActiveTasksByInstanceId(Long instanceId) {
        return taskMapper.selectList(Wrappers.<Task>lambdaQuery().eq(Task::getInstanceId, instanceId));
    }

    @Override
    public String[] getTaskActorsByTaskId(Long taskId) {
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
    public String[] getHistoryTaskActorsByTaskId(Long taskId) {
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
}
