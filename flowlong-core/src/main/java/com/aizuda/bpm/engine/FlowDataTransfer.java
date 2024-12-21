/*
 * Copyright 2023-2025 Licensed under the Dual Licensing
 * website: https://aizuda.com
 */
package com.aizuda.bpm.engine;

import java.util.HashMap;
import java.util.Map;

/**
 * JSON BPM 流程数据传输类
 *
 * <p>
 * <a href="https://aizuda.com">官网</a>尊重知识产权，不允许非法使用，后果自负
 * </p>
 *
 * @author hubin
 * @since 1.0
 */
public class FlowDataTransfer {

    /**
     * 传递参数存取
     */
    private static final ThreadLocal<Map<String, Object>> flowData = new ThreadLocal<>();

    /**
     * 设置传递参数
     *
     * @param requestData 传递参数 MAP 对象
     */
    public static void put(Map<String, Object> requestData) {
        flowData.set(requestData);
    }

    /**
     * 设置传递参数
     *
     * @param key   关键字
     * @param value 参数值
     */
    public static void put(String key, Object value) {
        Map<String, Object> dataMap = getAll();
        if (null != dataMap && !dataMap.isEmpty()) {
            dataMap.put(key, value);
        } else {
            // 创建设置值
            put(new HashMap<String, Object>(16) {{
                put(key, value);
            }});
        }
    }

    /**
     * 动态分配节点处理人或角色
     *
     * @param dataMap 处理人或角色信息
     */
    public static void dynamicAssignee(Map<String, Object> dataMap) {
        put(FlowConstants.processDynamicAssignee, dataMap);
    }

    /**
     * 指定选择条件节点 KEY
     *
     * @param conditionNodeKey 条件节点 KEY
     */
    public static void specifyConditionNodeKey(String conditionNodeKey) {
        put(FlowConstants.processSpecifyConditionNodeKey, conditionNodeKey);
    }

    /**
     * 获取传递参数
     *
     * @param key 传递参数
     * @return 传递参数 MAP 对象
     */
    @SuppressWarnings({"all"})
    public static <T> T get(String key) {
        Map<String, Object> dataMap = getAll();
        if (null != dataMap) {
            return (T) dataMap.get(key);
        }
        return null;
    }

    /**
     * 获取传递参数
     *
     * @return 传递参数 MAP 对象
     */
    public static Map<String, Object> getAll() {
        return flowData.get();
    }

    /**
     * 移除传递参数
     */
    public static void remove() {
        flowData.remove();
    }

    /**
     * 移除指定key传递参数
     */
    public static void removeByKey(String key) {
        Map<String, Object> dataMap = getAll();
        if (null != dataMap) {
            dataMap.remove(key);
        }
    }
}
