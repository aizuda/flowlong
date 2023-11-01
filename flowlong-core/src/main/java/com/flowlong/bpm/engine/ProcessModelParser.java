/*
 * Copyright 2023-2025 Licensed under the AGPL License
 */
package com.flowlong.bpm.engine;

import com.flowlong.bpm.engine.model.ProcessModel;

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
     * @param content   模型内容 JSON 格式
     * @param processId 流程 ID
     * @param redeploy  重新部署 true 是 false 否
     * @return
     */
    ProcessModel parse(String content, Long processId, boolean redeploy);

}
