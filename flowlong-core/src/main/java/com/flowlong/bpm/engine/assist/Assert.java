/* Copyright 2023-2025 jobob@qq.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.flowlong.bpm.engine.assist;

import java.util.Objects;

/**
 * 断言帮助类
 *
 * <p>
 * 尊重知识产权，CV 请保留版权，爱组搭 http://aizuda.com 出品
 * </p>
 *
 * @author hubin
 * @since 1.0
 */
public abstract class Assert {
    /**
     * 断言表达式为true
     *
     * @param expression
     * @param message    异常打印信息
     */
    public static void isTrue(boolean expression, String message) {
        illegalArgument(expression, message);
    }

    public static void isFalse(boolean expression, String message) {
        illegalArgument(!expression, message);
    }

    public static void isZero(int result, String message) {
        illegalArgument(Objects.equals(0, result), message);
    }

    /**
     * 断言表达式为true
     *
     * @param expression
     */
    public static void isTrue(boolean expression) {
        isTrue(expression, "[Assertion failed] - this expression must be true");
    }

    /**
     * 断言给定的object对象为空
     *
     * @param object
     * @param message 异常打印信息
     */
    public static void isNull(Object object, String message) {
        illegalArgument(null != object, message);
    }

    /**
     * 断言给定的object对象为空
     *
     * @param object
     */
    public static void isNull(Object object) {
        isNull(object, "[Assertion failed] - the object argument must be null");
    }

    /**
     * 断言给定的object对象为非空
     *
     * @param object
     * @param message 异常打印信息
     */
    public static void notNull(Object object, String message) {
        illegalArgument(null == object, message);
    }

    /**
     * 断言给定的object对象为非空
     *
     * @param object
     */
    public static void notNull(Object object) {
        notNull(object, "[Assertion failed] - this argument is required; it must not be null");
    }

    /**
     * 断言给定的字符串为非空
     *
     * @param str
     */
    public static void notEmpty(String str) {
        notEmpty(str, "[Assertion failed] - this argument is required; it must not be null or empty");
    }

    /**
     * 断言给定的字符串为非空
     *
     * @param str
     * @param message
     */
    public static void notEmpty(String str, String message) {
        illegalArgument(str == null || str.length() == 0, message);
    }

    /**
     * 非法参数断言
     *
     * @param illegal 判断条件
     * @param message 提示内容
     */
    public static void illegalArgument(boolean illegal, String message) {
        if (illegal) {
            throw new IllegalArgumentException(message);
        }
    }
}
