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
 * 参与类型
 *
 * <p>
 * 尊重知识产权，CV 请保留版权，爱组搭 http://aizuda.com 出品，不允许非法使用，后果自负
 * </p>
 *
 * @author hubin
 * @since 1.0
 */
public enum PerformType {
    /**
     * 发起、其它
     */
    unknown(0),
    /**
     * 按顺序依次审批
     */
    sort(1),
    /**
     * 会签 (可同时审批，每个人必须审批通过)
     */
    countersign(2),
    /**
     * 或签 (有一人审批通过即可)
     */
    orSign(3),
    /**
     * 抄送
     */
    copy(4);

    private final int value;

    PerformType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static PerformType get(Integer value) {
        if (null == value) {
            return unknown;
        }
        return Arrays.stream(PerformType.values()).filter(s -> s.getValue() == value).findFirst().orElse(unknown);
    }

}
