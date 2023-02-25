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
package com.flowlong.bpm.engine.assist;

import lombok.extern.slf4j.Slf4j;
import org.codehaus.jackson.map.ObjectMapper;

/**
 * json处理帮助类
 *
 * @author hubin
 * @since 1.0
 */
@Slf4j
public class JsonUtils {
    /**
     * jackson的ObjectMapper对象
     */
    private static ObjectMapper mapper = new ObjectMapper();

    /**
     * 将对象转换为json字符串
     *
     * @param object
     * @return
     */
    public static String toJson(Object object) {
        if (object == null) return "";
        try {
            return mapper.writeValueAsString(object);
        } catch (Exception e) {
            log.warn("write to json string error:" + object, e);
            return "";
        }
    }

    /**
     * 根据指定类型解析json字符串，并返回该类型的对象
     *
     * @param jsonString
     * @param clazz
     * @return
     */
    public static <T> T fromJson(String jsonString, Class<T> clazz) {
        if (StringUtils.isEmpty(jsonString)) {
            return null;
        }

        try {
            return mapper.readValue(jsonString, clazz);
        } catch (Exception e) {
            log.warn("parse json string error:" + jsonString, e);
            return null;
        }
    }
}
