/*
 * Copyright 2023-2025 Licensed under the AGPL License
 */
package com.aizuda.bpm.engine.assist;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;

/**
 * Java 对象判断处理帮助类
 *
 * <p>
 * 尊重知识产权，不允许非法使用，后果自负
 * </p>
 *
 * @author hubin
 * @since 1.0
 */
public class ObjectUtils {

    /**
     * 判断字符串是否为空
     *
     * @param obj 待判断对象
     * @return 是否为空标识
     */
    public static boolean isEmpty(Object obj) {
        if (null == obj) {
            return true;
        }
        if (obj instanceof String) {
            String str = (String) obj;
            return str.isEmpty();
        } else if (obj instanceof Collection) {
            return ((Collection<?>) obj).isEmpty();
        } else if (obj instanceof Map) {
            return ((Map<?, ?>) obj).isEmpty();
        }
        return false;
    }

    /**
     * 判断字符串是否为非空
     *
     * @param obj 待判断对象
     * @return 是否为非空标识
     */
    public static boolean isNotEmpty(Object obj) {
        return !isEmpty(obj);
    }

    /**
     * 判断 Map 是否未 Collections$SingletonMap 对象
     */
    public static boolean isSingletonMap(Map<?, ?> mapObj) {
        return Objects.equals(mapObj.getClass().getName(), "java.util.Collections$SingletonMap");
    }
}
