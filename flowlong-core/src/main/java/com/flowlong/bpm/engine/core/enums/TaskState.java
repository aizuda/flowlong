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
package com.flowlong.bpm.engine.core.enums;

import java.util.Arrays;

/**
 * 流程状态
 *
 * <p>
 * 尊重知识产权，CV 请保留版权，爱组搭 http://aizuda.com 出品
 * </p>
 *
 * @author 江涛
 * @since 1.0
 */
public enum TaskState {
    /**
     * 活动
     */
    active(0),
    /**
     * 结束
     */
    finish(1),
    /**
     * 拒绝
     */
    reject(2),
    /**
     * 超时
     */
    timeout(3),
    /**
     * 终止
     */
    termination(4);

    private final int value;

    TaskState(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static TaskState get(int value) {
        return Arrays.stream(TaskState.values()).filter(s -> s.getValue() == value).findFirst().orElseGet(null);
    }

}
