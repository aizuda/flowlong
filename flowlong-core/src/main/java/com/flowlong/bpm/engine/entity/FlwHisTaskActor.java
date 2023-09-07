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
package com.flowlong.bpm.engine.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 历史任务参与者实体类
 *
 * <p>
 * 尊重知识产权，CV 请保留版权，爱组搭 http://aizuda.com 出品，不允许非法使用，后果自负
 * </p>
 *
 * @author hubin
 * @since 1.0
 */
@Getter
@Setter
@ToString
public class FlwHisTaskActor extends FlwTaskActor {

    public static FlwHisTaskActor of(FlwTaskActor taskActor) {
        FlwHisTaskActor hisTaskActor = new FlwHisTaskActor();
        hisTaskActor.tenantId = taskActor.getTenantId();
        hisTaskActor.instanceId = taskActor.getInstanceId();
        hisTaskActor.taskId = taskActor.getTaskId();
        hisTaskActor.actorType = taskActor.getActorType();
        hisTaskActor.actorId = taskActor.getActorId();
        hisTaskActor.actorName = taskActor.getActorName();
        return hisTaskActor;
    }
}
