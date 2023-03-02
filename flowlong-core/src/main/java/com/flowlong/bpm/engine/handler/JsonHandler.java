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
package com.flowlong.bpm.engine.handler;

/**
 * JSON 解析处理器接口
 *
 * @author hubin
 * @since 1.0
 */
public interface JsonHandler {

    /**
     * 对象转换为 JSON 字符串
     *
     * @param object 待转换对象
     * @return
     */
    String toJson(Object object);

    /**
     * JSON 字符串传转为 clazz 类型对象
     *
     * @param jsonString 待转换对象的JSON字符串
     * @param clazz      待转换对象类
     * @param <T>
     * @return
     */
    <T> T fromJson(String jsonString, Class<T> clazz);

}
