/*
 * Copyright 2023-2025 Licensed under the Dual Licensing
 * website: https://aizuda.com
 */
package com.aizuda.bpm.engine.impl;

import com.aizuda.bpm.engine.ProcessModelParser;
import com.aizuda.bpm.engine.assist.Assert;
import com.aizuda.bpm.engine.cache.FlowCache;
import com.aizuda.bpm.engine.cache.FlowSimpleCache;
import com.aizuda.bpm.engine.core.FlowLongContext;
import com.aizuda.bpm.engine.model.ProcessModel;
import lombok.Getter;
import lombok.Setter;

/**
 * FlowLong 默认流程模型解析器
 *
 * <p>
 * <a href="https://aizuda.com">官网</a>尊重知识产权，不允许非法使用，后果自负
 * </p>
 *
 * @author hubin
 * @since 1.0
 */
@Getter
@Setter
public class DefaultProcessModelParser implements ProcessModelParser {

    /**
     * 流程缓存处理类，默认 ConcurrentHashMap 实现
     * 使用其它缓存框架可在初始化时赋值该静态属性
     */
    private FlowCache flowCache;

    public DefaultProcessModelParser(FlowCache flowCache) {
        if (null == flowCache) {
            this.flowCache = new FlowSimpleCache();
        } else {
            this.flowCache = flowCache;
        }
    }

    @Override
    public ProcessModel parse(String content, String cacheKey, boolean redeploy) {
        // 缓存解析逻辑
        if (null != cacheKey) {
            FlowCache flowCache = this.getFlowCache();
            ProcessModel processModel = flowCache.get(cacheKey);
            if (null != content && (null == processModel || redeploy)) {
                processModel = parseProcessModel(content);
                flowCache.put(cacheKey, processModel);
            }
            return processModel;
        }

        // 未缓存解析逻辑
        return parseProcessModel(content);
    }

    private ProcessModel parseProcessModel(String content) {
        ProcessModel processModel = FlowLongContext.fromJson(content, ProcessModel.class);
        Assert.isNull(processModel, "process model json parser error");
        processModel.buildParentNode(processModel.getNodeConfig());
        return processModel;
    }

    @Override
    public void invalidate(String cacheKey) {
        this.getFlowCache().remove(cacheKey);
    }

    @Override
    public FlowCache getFlowCache() {
        return flowCache;
    }
}
