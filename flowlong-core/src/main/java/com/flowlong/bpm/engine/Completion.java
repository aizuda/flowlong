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

package com.flowlong.bpm.engine;

import com.flowlong.bpm.engine.entity.HisInstance;
import com.flowlong.bpm.engine.entity.HisTask;

/**
 * 任务、实例完成时触发动作的接口
 *
 * @author hubin
 * @since 1.0
 */
public interface Completion {

    /**
     * 任务完成触发执行
     *
     * @param task 任务对象
     */
    void complete(HisTask task);

    /**
     * 实例完成触发执行
     *
     * @param order 实例对象
     */
    void complete(HisInstance order);
}
