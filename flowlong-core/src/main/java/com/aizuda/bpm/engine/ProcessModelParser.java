/*
 * Copyright 2023-2025 Licensed under the AGPL License
 */
package com.aizuda.bpm.engine;

import com.aizuda.bpm.engine.cache.FlowCache;
import com.aizuda.bpm.engine.model.ProcessModel;

/**
 * FlowLong 流程模型解析器接口
 *
 * <p>
 * 尊重知识产权，不允许非法使用，后果自负
 * </p>
 *
 * @author hubin
 * @since 1.0
 */
public interface ProcessModelParser {

    /**
     * 流程模型 JSON 解析
     *
     * @param content  模型内容 JSON 格式
     * @param cacheKey 缓存 KEY
     * @param redeploy 重新部署 true 是 false 否
     * @return 流程模型
     */
    ProcessModel parse(String content, String cacheKey, boolean redeploy);

    /**
     * 使缓存失效
     *
     * @param cacheKey 缓存 KEY
     */
    void invalidate(String cacheKey);

    /**
     * 流程缓存实现类
     *
     * @return 流程缓存
     */
    FlowCache getFlowCache();
}
