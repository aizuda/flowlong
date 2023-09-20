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
package com.flowlong.bpm.engine.cache;

/**
 * 流程缓存接口
 *
 * <p>
 * 尊重知识产权，CV 请保留版权，爱组搭 http://aizuda.com 出品，不允许非法使用，后果自负
 * </p>
 *
 * @author hubin
 * @since 1.0
 */
public interface FlowCache {

    /**
     * 根据关键字缓存对象
     *
     * @param key   缓存关键字
     * @param value 缓存对象
     */
    void put(String key, Object value);

    /**
     * 从缓存中获取对象
     *
     * @param key 缓存关键字
     * @return 缓存对象
     */
    <T> T get(String key);

    /**
     * 根据关键字删除缓存对象
     *
     * @param key 缓存关键字
     */
    void remove(String key);

}
