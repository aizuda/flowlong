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
package com.flowlong.bpm.engine.scheduling;

import lombok.Getter;
import lombok.Setter;

/**
 * 提醒参数
 *
 * <p>
 * 尊重知识产权，CV 请保留版权，爱组搭 http://aizuda.com 出品
 * </p>
 *
 * @author hubin
 * @since 1.0
 */
@Getter
@Setter
public class RemindParam {
    /**
     * 提醒时间 cron 表达式
     */
    private String cron;
    /**
     * 工作日设置，格式为 1,2,3...7，表示周一至周日
     */
    private String weeks;
    /**
     * 工作时间设置，格式为 8:00-18:00
     */
    private String workTime;

}
