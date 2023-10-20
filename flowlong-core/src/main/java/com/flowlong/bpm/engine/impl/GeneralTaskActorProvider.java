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
package com.flowlong.bpm.engine.impl;

import com.flowlong.bpm.engine.TaskActorProvider;
import com.flowlong.bpm.engine.assist.ObjectUtils;
import com.flowlong.bpm.engine.core.Execution;
import com.flowlong.bpm.engine.entity.FlwTaskActor;
import com.flowlong.bpm.engine.model.NodeModel;

import java.util.ArrayList;
import java.util.List;

/**
 * 普遍的任务参与者提供处理类
 *
 * <p>
 * 尊重知识产权，CV 请保留版权，爱组搭 http://aizuda.com 出品，不允许非法使用，后果自负
 * </p>
 *
 * @author hubin
 * @since 1.0
 */
public class GeneralTaskActorProvider implements TaskActorProvider {

    public List<FlwTaskActor> getTaskActors(NodeModel nodeModel, Execution execution) {
        List<FlwTaskActor> flwHisTaskActors = new ArrayList<>();
        if (ObjectUtils.isNotEmpty(nodeModel.getNodeUserList())) {
            // 指定用户审批
            nodeModel.getNodeUserList().forEach(t -> flwHisTaskActors.add(FlwTaskActor.ofUser(t.getId(), t.getName())));
        } else if (ObjectUtils.isNotEmpty(nodeModel.getNodeRoleList())) {
            // 指定角色审批
            nodeModel.getNodeRoleList().forEach(t -> flwHisTaskActors.add(FlwTaskActor.ofRole(t.getId(), t.getName())));
        }
        return ObjectUtils.isEmpty(flwHisTaskActors) ? null : flwHisTaskActors;
    }
}
