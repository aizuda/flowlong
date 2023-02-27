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

import com.flowlong.bpm.engine.cache.FlowLongCache;
import com.flowlong.bpm.engine.cache.CacheExceptionFlow;
import com.flowlong.bpm.engine.cache.CacheManager;
import com.flowlong.bpm.engine.assist.StringUtils;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 基于虚拟机内存的cache管理器
 *
 * @author hubin
 * @since 1.0
 */
public class MemoryCacheManager implements CacheManager {
    private final ConcurrentMap<String, FlowLongCache> caches;

    public MemoryCacheManager() {
        this.caches = new ConcurrentHashMap<String, FlowLongCache>();
    }

    @Override
    public <K, V> FlowLongCache<K, V> getCache(String name) throws CacheExceptionFlow {
        if (StringUtils.isEmpty(name)) {
            throw new IllegalArgumentException("Cache名称不能为空.");
        }
        FlowLongCache cache;

        cache = caches.get(name);
        if (cache == null) {
            cache = new MemoryCache<Object, Object>(new ConcurrentHashMap<Object, Object>());
            FlowLongCache existing = caches.putIfAbsent(name, cache);
            if (existing != null) {
                cache = existing;
            }
        }
        return cache;
    }

    @Override
    public void destroy() throws CacheExceptionFlow {
        while (!caches.isEmpty()) {
            caches.clear();
        }
    }
}
