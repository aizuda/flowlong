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
 * 缓存接口
 *
 * @author hubin
 * @since 1.0
 */
public interface Cache<K, V> {
    /**
     * 根据key从缓存中获取对应的值
     *
     * @param key
     * @return
     * @throws CacheExceptionFlow
     */
    public V get(K key) throws CacheExceptionFlow;

    /**
     * 添加缓存键值对
     *
     * @param key
     * @param value
     * @return
     * @throws CacheExceptionFlow
     */
    public V put(K key, V value) throws CacheExceptionFlow;

    /**
     * 根据key从缓存中删除对象
     *
     * @param key
     * @return
     * @throws CacheExceptionFlow
     */
    public V remove(K key) throws CacheExceptionFlow;

    /**
     * 从缓存中清除所有的对象
     *
     * @throws CacheExceptionFlow
     */
    public void clear() throws CacheExceptionFlow;
}
