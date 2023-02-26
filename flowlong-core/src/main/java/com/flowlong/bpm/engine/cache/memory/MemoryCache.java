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
package com.flowlong.bpm.engine.cache.memory;

import com.flowlong.bpm.engine.cache.Cache;
import com.flowlong.bpm.engine.cache.CacheExceptionFlow;
import com.flowlong.bpm.engine.assist.Assert;

import java.util.Map;

/**
 * 基于内存管理cache
 *
 * @author hubin
 * @since 1.0
 */
public class MemoryCache<K, V> implements Cache<K, V> {
    /**
     * map cache
     */
    private final Map<K, V> map;

    /**
     * 通过Map实现类构造MemoryCache
     *
     * @param backingMap
     */
    public MemoryCache(Map<K, V> backingMap) {
        Assert.notNull(backingMap);
        this.map = backingMap;
    }

    @Override
    public V get(K key) throws CacheExceptionFlow {
        return map.get(key);
    }

    @Override
    public V put(K key, V value) throws CacheExceptionFlow {
        return map.put(key, value);
    }

    @Override
    public V remove(K key) throws CacheExceptionFlow {
        return map.remove(key);
    }

    @Override
    public void clear() throws CacheExceptionFlow {
        map.clear();
    }
}
