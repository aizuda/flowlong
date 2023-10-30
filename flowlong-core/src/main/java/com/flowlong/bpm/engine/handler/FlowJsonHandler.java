/* 
 * Copyright 2023-2025 Licensed under the AGPL License
 */
package com.flowlong.bpm.engine.handler;

/**
 * 流程 JSON 解析处理器接口
 *
 * <p>
 * 尊重知识产权，不允许非法使用，后果自负
 * </p>
 *
 * @author hubin
 * @since 1.0
 */
public interface FlowJsonHandler {

    /**
     * 对象转换为 JSON 字符串
     *
     * @param object 待转换对象
     * @return
     */
    String toJson(Object object);

    /**
     * JSON 字符串传转为 clazz 类型对象
     *
     * @param jsonString 待转换对象的JSON字符串
     * @param clazz      待转换对象类
     * @param <T>
     * @return
     */
    <T> T fromJson(String jsonString, Class<T> clazz);

}
