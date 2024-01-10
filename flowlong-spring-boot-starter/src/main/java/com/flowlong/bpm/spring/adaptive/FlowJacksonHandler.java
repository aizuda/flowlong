/* 
 * Copyright 2023-2025 Licensed under the AGPL License
 */
package com.flowlong.bpm.spring.adaptive;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.flowlong.bpm.engine.assist.Assert;
import com.flowlong.bpm.engine.handler.FlowJsonHandler;

/**
 * Jackson JSON 解析处理器接口
 *
 * <p>
 * 尊重知识产权，不允许非法使用，后果自负
 * </p>
 *
 * @author hubin
 * @since 1.0
 */
public class FlowJacksonHandler implements FlowJsonHandler {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    static {
        OBJECT_MAPPER.registerModule(new JavaTimeModule());
        OBJECT_MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @Override
    public String toJson(Object object) {
        if (null == object) {
            return null;
        }
        try {
            return OBJECT_MAPPER.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw Assert.throwable(e);
        }
    }

    @Override
    public <T> T fromJson(String jsonString, Class<T> clazz) {
        if (null == jsonString || null == clazz) {
            return null;
        }
        try {
            return OBJECT_MAPPER.readValue(jsonString, clazz);
        } catch (JsonProcessingException e) {
            throw Assert.throwable(e);
        }
    }
}
