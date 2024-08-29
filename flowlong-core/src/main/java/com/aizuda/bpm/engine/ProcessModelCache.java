/*
 * Copyright 2023-2025 Licensed under the AGPL License
 */
package com.aizuda.bpm.engine;

import com.aizuda.bpm.engine.assist.Assert;
import com.aizuda.bpm.engine.core.FlowLongContext;
import com.aizuda.bpm.engine.model.ProcessModel;

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
     *
     * @return 缓存 KEY
     */
    String modelCacheKey();

    /**
     * 流程模型内容
     *
     * @return 缓存内容
     */
    String getModelContent();

    /**
     * JSON BPM 模型
     *
     * @return JSON BPM 模型
     */
    default ProcessModel model() {
        return this.model(false);
    }

    /**
     * JSON BPM 模型
     *
     * @param redeploy 重新部署 true 是 false 否
     * @return JSON BPM 模型
     */
    default ProcessModel model(boolean redeploy) {
        String modelContent = this.getModelContent();
        Assert.isEmpty(modelContent, "The process modelContent is Empty.");
        return FlowLongContext.parseProcessModel(modelContent, this.modelCacheKey(), redeploy);
    }
}
