/*
 * Copyright 2023-2025 Licensed under the Dual Licensing
 * website: https://aizuda.com
 */
package com.aizuda.bpm.solon.adaptive;

import com.aizuda.bpm.engine.handler.FlowJsonHandler;
import org.noear.snack4.ONode;
import org.noear.solon.annotation.Component;

/**
 * Solon Snack JSON 解析处理器接口适配
 *
 * <p>
 * <a href="https://aizuda.com">官网</a>尊重知识产权，不允许非法使用，后果自负
 * </p>
 *
 * @author noear
 * @since 1.0
 */
@Component
public class FlowSnackjsonHandler implements FlowJsonHandler {

    @Override
    public String toJson(Object object) {
        if (null == object) {
            return null;
        }

        return ONode.serialize(object);
    }

    @Override
    public <T> T fromJson(String jsonString, Class<T> clazz) {
        if (null == jsonString || null == clazz) {
            return null;
        }

        return ONode.deserialize(jsonString, clazz);
    }
}
