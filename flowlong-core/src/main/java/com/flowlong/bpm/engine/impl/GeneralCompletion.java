/*
 *  Copyright 2023-2025 www.flowlong.com
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package com.flowlong.bpm.engine.impl;

import com.flowlong.bpm.engine.Completion;
import com.flowlong.bpm.engine.entity.HisInstance;
import com.flowlong.bpm.engine.entity.HisTask;
import lombok.extern.slf4j.Slf4j;

/**
 * 默认的任务、实例完成时触发的动作
 *
 * @author hubin
 * @since 1.0
 */
@Slf4j
public class GeneralCompletion implements Completion {

    public void complete(HisTask task) {
        log.info("The task[{}] has been user[{}] has completed", task.getId(), task.getOperator());
    }

    public void complete(HisInstance order) {
        log.info("The order[{}] has completed", order.getId());
    }
}
