/*
 * Copyright 2023-2025 Licensed under the AGPL License
 */
package com.aizuda.bpm.engine.assist;

import com.aizuda.bpm.engine.exception.FlowLongException;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * 断言帮助类
 *
 * <p>
 * 尊重知识产权，不允许非法使用，后果自负
 * </p>
 *
 * @author hubin
 * @since 1.0
 */
public abstract class Assert {
    /**
     * 断言表达式为true
     *
     * @param expression 判断条件
     * @param message    异常打印信息
     */
    public static void isTrue(boolean expression, String message) {
        illegal(expression, message);
    }

    public static void isFalse(boolean expression, Supplier<String> supplier) {
        illegal(!expression, supplier);
    }

    public static void isFalse(boolean expression, String message) {
        illegal(!expression, message);
    }

    public static void isZero(int result, String message) {
        illegal(Objects.equals(0, result), message);
    }

    /**
     * 断言表达式为true
     *
     * @param expression 判断条件
     */
    public static void isTrue(boolean expression) {
        isTrue(expression, "[Assertion failed] - this expression must be false");
    }


    /**
     * 断言给定的object对象为空
     *
     * @param object  待检测对象
     * @param message 异常打印信息
     */
    public static void isNull(Object object, String message) {
        illegal(null == object, message);
    }

    /**
     * 断言给定的object对象为空
     *
     * @param object 待检测对象
     */
    public static void isNull(Object object) {
        isNull(object, "[Assertion failed] - the object argument must not be null");
    }

    /**
     * 断言给定的object对象为非空
     *
     * @param object  待检测对象
     * @param message 异常打印信息
     */
    public static void notNull(Object object, String message) {
        illegal(null != object, message);
    }

    /**
     * 断言给定的object对象为非空
     *
     * @param object 待检测对象
     */
    public static void notNull(Object object) {
        notNull(object, "[Assertion failed] - the object argument must be null");
    }

    /**
     * 断言给定的object对象为空
     *
     * @param object  待检测对象
     */
    public static void isEmpty(Object object) {
        isEmpty(object, "[Assertion failed] - this argument must not be null or empty");
    }

    /**
     * 断言给定的object对象为空
     *
     * @param object  待检测对象
     * @param message 异常打印信息
     */
    public static void isEmpty(Object object, String message) {
        illegal(ObjectUtils.isEmpty(object), message);
    }

    /**
     * 断言给定的字符串为非空
     *
     * @param str 待检测字符串
     */
    public static void notEmpty(String str) {
        notEmpty(str, "[Assertion failed] - this argument must be null or empty");
    }

    /**
     * 断言给定的字符串为非空
     *
     * @param str     待检测字符串
     * @param message 提升内容
     */
    public static void notEmpty(String str, String message) {
        illegal(ObjectUtils.isNotEmpty(str), message);
    }

    /**
     * 非法参数断言
     *
     * @param illegal 判断条件
     * @param message 提示内容
     */
    public static void illegal(boolean illegal, String message) {
        if (illegal) {
            throw throwable(message);
        }
    }

    /**
     * 非法参数断言
     *
     * @param illegal  判断条件
     * @param supplier 提示内容提供者
     */
    public static void illegal(boolean illegal, Supplier<String> supplier) {
        if (illegal) {
            throw throwable(supplier.get());
        }
    }

    /**
     * 非法参数断言
     *
     * @param message 提示内容
     */
    public static void illegal(String message) {
        throw throwable(message);
    }

    /**
     * 创建 FlowLongException 异常信息
     *
     * @param message 提示内容
     * @return {@link FlowLongException}
     */
    public static FlowLongException throwable(String message) {
        return new FlowLongException(message);
    }

    /**
     * 创建 FlowLongException 异常信息
     *
     * @param message 提示内容
     * @param cause   {@link Throwable}
     * @return {@link FlowLongException}
     */
    public static FlowLongException throwable(String message, Throwable cause) {
        return new FlowLongException(message, cause);
    }

    /**
     * 创建 FlowLongException 异常信息
     *
     * @param cause {@link Throwable}
     * @return {@link FlowLongException}
     */
    public static FlowLongException throwable(Throwable cause) {
        return new FlowLongException(cause);
    }
}
