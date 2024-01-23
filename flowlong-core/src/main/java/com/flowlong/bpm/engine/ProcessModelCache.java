/*
 * Copyright 2023-2025 Licensed under the AGPL License
 */
package com.flowlong.bpm.engine;

import com.flowlong.bpm.engine.assist.Assert;
import com.flowlong.bpm.engine.core.FlowLongContext;
import com.flowlong.bpm.engine.model.ProcessModel;

/**
 * JSON BPM 模型缓存处理接口
 *
 * <p>
 * 尊重知识产权，不允许非法使用，后果自负
 * </p>
 *
 * @author hubin
 * @since 1.0
 */
public interface ProcessModelCache {

    /**
     * 流程模型缓存KEY
     */
    String modelCacheKey();

    /**
     * 流程模型内容
     */
    String getModelContent();

    /**
     * JSON BPM 模型
     */
    default ProcessModel model() {
        String modelContent = this.getModelContent();
        Assert.isEmpty(modelContent, "The process modelContent is Empty.");
        return FlowLongContext.parseProcessModel(modelContent, this.modelCacheKey(), false);
    }
}
