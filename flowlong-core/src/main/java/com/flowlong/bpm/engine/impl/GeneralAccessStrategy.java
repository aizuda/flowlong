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

import com.flowlong.bpm.engine.TaskAccessStrategy;
import com.flowlong.bpm.engine.assist.ObjectUtils;
import com.flowlong.bpm.engine.entity.FlwTaskActor;

import java.util.List;
import java.util.Objects;

/**
 * 基于用户或组（角色、部门等）的访问策略类
 * 该策略类适合组作为参与者的情况
 *
 * <p>
 * 尊重知识产权，CV 请保留版权，爱组搭 http://aizuda.com 出品，不允许非法使用，后果自负
 * </p>
 *
 * @author hubin
 * @since 1.0
 */
public class GeneralAccessStrategy implements TaskAccessStrategy {

    /**
     * 如果创建人ID所属的组只要有一项存在于参与者集合中，则表示可访问
     */
    @Override
    public boolean isAllowed(String userId, List<FlwTaskActor> taskActors) {
        if (null == userId || ObjectUtils.isEmpty(taskActors)) {
            return false;
        }
        // 参与者 ID 默认非组，作为用户ID判断是否允许执行
        return taskActors.stream().anyMatch(t -> Objects.equals(t.getActorId(), userId));
    }
}
