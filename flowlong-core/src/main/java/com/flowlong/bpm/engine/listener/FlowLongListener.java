/* Copyright 2023-2025 www.flowlong.com
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
package com.flowlong.bpm.engine.listener;

/**
 * 流程引擎监听接口
 *
 * @author hubin
 * @since 1.0
 */
public interface FlowLongListener<T> {
    String EVENT_CREATE = "create";
    String EVENT_ASSIGNMENT = "assignment";
    String EVENT_COMPLETE = "complete";
    String EVENT_TERMINATE = "terminate";
    String EVENT_UPDATE = "update";
    String EVENT_DELETE = "delete";
    String EVENT_TIMEOUT = "timeout";

    void notify(String event, T t);

}
