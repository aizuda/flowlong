/*
 * Copyright 2023-2025 Licensed under the AGPL License
 */
package com.flowlong.bpm.engine.assist;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Map;

/**
 * 日期帮助类
 *
 * <p>
 * 尊重知识产权，不允许非法使用，后果自负
 * </p>
 *
 * @author hubin
 * @since 1.0
 */
public class DateUtils {
    private static final String DATE_FORMAT_DEFAULT = "yyyy-MM-dd HH:mm:ss";

    /**
     * 当前时间 Date 类型
     *
     * @return {@link Date}
     */
    public static Date getCurrentDate() {
        return new Date();
    }

    /**
     * 日期判断
     */
    public static boolean after(Date arg0, Date arg1) {
        return null != arg0 && null != arg1 && arg0.after(arg1);
    }

    /**
     * 日期 Date 转为 LocalDateTime
     *
     * @param date {@link Date}
     * @return {@link LocalDateTime}
     */
    public static LocalDateTime dateToLocalDateTime(Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    private static String localDateTimeFormat(LocalDateTime localDateTime) {
        return localDateTime.format(DateTimeFormatter.ofPattern(DATE_FORMAT_DEFAULT));
    }

    /**
     * 解析日期时间对象
     *
     * @param date 日期类型对象
     * @return 解析日期
     */
    public static String parseTime(Object date) {
        if (date == null) {
            return null;
        }
        if (date instanceof Date) {
            return localDateTimeFormat(dateToLocalDateTime((Date) date));
        }
        if (date instanceof String) {
            return String.valueOf(date);
        }
        return "";
    }

    /**
     * 对时限数据进行处理
     * 1、运行时设置的date型数据直接返回
     * 2、模型设置的需要特殊转换成date类型
     * 3、运行时设置的转换为date型
     *
     * @param args      运行时参数
     * @param parameter 模型参数
     * @return Date类型
     */
    public static Date processTime(Map<String, Object> args, String parameter) {
        if (ObjectUtils.isEmpty(parameter)) {
            return null;
        }
        Object data = args.get(parameter);
        if (data == null) {
            data = parameter;
        }
        if (data instanceof Date) {
            return (Date) data;
        }
        if (data instanceof Long) {
            return new Date((Long) data);
        }
        return null;
    }
}
