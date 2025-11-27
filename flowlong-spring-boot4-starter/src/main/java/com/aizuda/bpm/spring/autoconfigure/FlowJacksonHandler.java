package com.aizuda.bpm.spring.autoconfigure;

import com.aizuda.bpm.engine.handler.FlowJsonHandler;
import tools.jackson.databind.ObjectMapper;

public class FlowJacksonHandler implements FlowJsonHandler {
    private final ObjectMapper objectMapper;

    public FlowJacksonHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public String toJson(Object object) {
        if (null == object) {
            return null;
        }
        return objectMapper.writeValueAsString(object);
    }

    @Override
    public <T> T fromJson(String jsonString, Class<T> clazz) {
        if (null == jsonString || null == clazz) {
            return null;
        }
        return objectMapper.readValue(jsonString, clazz);
    }
}