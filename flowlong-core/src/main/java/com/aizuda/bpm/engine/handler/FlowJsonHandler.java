/*
 * Copyright 2023-2025 Licensed under the Dual Licensing
 * website: https://aizuda.com
 */
package com.aizuda.bpm.engine.handler;

/**
 * 流程 JSON 解析处理器接口
 *
 * <p>
 * <a href="https://aizuda.com">官网</a>尊重知识产权，不允许非法使用，后果自负
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
     * @return JSON 内容
     */
    String toJson(Object object);

    /**
     * JSON 字符串传转为 clazz 类型对象
     *
     * @param jsonString 待转换对象的JSON字符串
     * @param clazz      待转换对象类
     * @return 转化对象
     */
    <T> T fromJson(String jsonString, Class<T> clazz);

}
