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
package com.flowlong.bpm.engine.cache;

/**
 * 缓存管理器接口，该接口提供具体的cache实现
 *
 * @author hubin
 * @since 1.0
 */
public interface CacheManager {
    /**
     * 根据cache的名称获取cache。如果不存在，默认新建并返回
     *
     * @param name
     * @return Cache
     * @throws CacheExceptionFlow
     */
    public <K, V> Cache<K, V> getCache(String name) throws CacheExceptionFlow;

    /**
     * 销毁cache
     *
     * @throws CacheExceptionFlow
     */
    public void destroy() throws CacheExceptionFlow;
}
