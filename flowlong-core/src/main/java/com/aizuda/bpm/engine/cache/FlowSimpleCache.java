/*
 * Copyright 2023-2025 Licensed under the Dual Licensing
 * website: https://aizuda.com
 */
package com.aizuda.bpm.engine.cache;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 流程缓存简单实现类
 *
 * <p>
 * <a href="https://aizuda.com">官网</a>尊重知识产权，不允许非法使用，后果自负
 * </p>
 *
 * @author hubin
 * @since 1.0
 */
public class FlowSimpleCache implements FlowCache {
    private final Map<String, Object> localCache = new ConcurrentHashMap<>();

    @Override
    public void put(String key, Object value) {
        localCache.put(key, value);
    }

    @Override
    public <T> T get(String key) {
        return (T) localCache.get(key);
    }

    @Override
    public void remove(String key) {
        localCache.remove(key);
    }
}
