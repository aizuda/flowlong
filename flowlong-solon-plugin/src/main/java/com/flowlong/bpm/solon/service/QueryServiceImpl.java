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
package com.flowlong.bpm.solon.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.flowlong.bpm.engine.QueryService;
import com.flowlong.bpm.engine.entity.*;
import com.flowlong.bpm.mybatisplus.mapper.*;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;

import java.util.List;
import java.util.Optional;

/**
 * 查询服务实现类
 *
 * <p>
 * 尊重知识产权，CV 请保留版权，爱组搭 http://aizuda.com 出品，不允许非法使用，后果自负，不允许非法使用，后果自负
 * </p>
 *
 * @author hubin
 * @since 1.0
 */
@Component
public class QueryServiceImpl implements QueryService {
    @Inject
    private FlwInstanceMapper instanceMapper;
    @Inject
    private FlwHisInstanceMapper hisInstanceMapper;
    @Inject
    private FlwTaskMapper taskMapper;
    @Inject
    private FlwTaskActorMapper taskActorMapper;
    @Inject
    private FlwHisTaskMapper hisTaskMapper;
    @Inject
    private FlwHisTaskActorMapper hisTaskActorMapper;



    @Override
    public FlwInstance getInstance(Long instanceId) {
        return instanceMapper.selectById(instanceId);
    }

    @Override
    public FlwTask getTask(Long taskId) {
        return taskMapper.selectById(taskId);
    }

    @Override
    public FlwHisInstance getHistInstance(Long instanceId) {
        return hisInstanceMapper.selectById(instanceId);
    }

    @Override
    public FlwHisTask getHistTask(Long taskId) {
        return hisTaskMapper.selectById(taskId);
    }

    @Override
    public Optional<List<FlwHisTask>> getHisTasksByName(Long instanceId, String taskName) {
        return Optional.ofNullable(hisTaskMapper.selectList(Wrappers.<FlwHisTask>lambdaQuery()
                .eq(FlwHisTask::getInstanceId, instanceId)
                .eq(FlwHisTask::getTaskName, taskName)
                .orderByDesc(FlwHisTask::getCreateTime)));
    }

    @Override
    public List<FlwTask> getTasksByInstanceId(Long instanceId) {
        return taskMapper.selectList(Wrappers.<FlwTask>lambdaQuery().eq(FlwTask::getInstanceId, instanceId));
    }

    @Override
    public List<FlwTask> getTasksByInstanceIdAndTaskName(Long instanceId, String taskName) {
        return taskMapper.selectList(Wrappers.<FlwTask>lambdaQuery().eq(FlwTask::getInstanceId, instanceId).eq(FlwTask::getTaskName, taskName));
    }

    @Override
    public Optional<List<FlwTask>> getActiveTasksByInstanceId(Long instanceId) {
        return Optional.ofNullable(taskMapper.selectList(Wrappers.<FlwTask>lambdaQuery().eq(FlwTask::getInstanceId, instanceId)));
    }

    @Override
    public Optional<List<FlwTaskActor>> getActiveTaskActorsByInstanceId(Long instanceId) {
        return Optional.ofNullable(taskActorMapper.selectList(Wrappers.<FlwTaskActor>lambdaQuery().eq(FlwTaskActor::getInstanceId, instanceId)));
    }

    @Override
    public List<FlwTaskActor> getTaskActorsByTaskId(Long taskId) {
        return taskActorMapper.selectList(Wrappers.<FlwTaskActor>lambdaQuery().eq(FlwTaskActor::getTaskId, taskId));
    }

    @Override
    public List<FlwTaskActor> getHistoryTaskActorsByTaskId(Long taskId) {
        return hisTaskActorMapper.selectList(Wrappers.<FlwTaskActor>lambdaQuery().eq(FlwTaskActor::getTaskId, taskId));
    }

    @Override
    public List<FlwTask> getActiveTasks(Long instanceId, List<String> taskNames) {
        return taskMapper.selectList(Wrappers.<FlwTask>lambdaQuery()
                .eq(FlwTask::getInstanceId, instanceId)
                .in(FlwTask::getTaskName, taskNames));
    }

    @Override
    public Optional<List<FlwHisTask>> getHisTasksByInstanceId(Long instanceId) {
        return Optional.ofNullable(hisTaskMapper.selectList(Wrappers.<FlwHisTask>lambdaQuery()
                .eq(FlwHisTask::getInstanceId, instanceId)
                .orderByDesc(FlwHisTask::getCreateTime)));
    }

}
