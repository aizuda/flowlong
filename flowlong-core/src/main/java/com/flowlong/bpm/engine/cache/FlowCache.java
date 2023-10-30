/* 
 * Copyright 2023-2025 Licensed under the AGPL License
 */
package com.flowlong.bpm.engine.cache;

/**
 * 流程缓存接口
 *
 * <p>
 * 尊重知识产权，不允许非法使用，后果自负
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
