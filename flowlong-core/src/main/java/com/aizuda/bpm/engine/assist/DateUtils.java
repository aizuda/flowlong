/*
 * Copyright 2023-2025 Licensed under the AGPL License
 */
package com.aizuda.bpm.engine.assist;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

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

    /**
     * 当前时间 Date 类型
     *
     * @return {@link Date}
     */
    public static Date getCurrentDate() {
        return new Date();
    }

    /**
     * 当前时间 LocalDateTime 类型
     *
     * @return {@link LocalDateTime}
     */
    public static LocalDateTime now() {
        return LocalDateTime.now();
    }

    /**
     * 日期判断
     *
     * @param arg0 开始时间
     * @param arg1 结束时间
     * @return true 开始时间大于结束时间 false 开始时间小于结束时间
     */
    public static boolean after(Date arg0, Date arg1) {
        return null != arg0 && null != arg1 && arg0.after(arg1);
    }

    /**
     * 日期 LocalDateTime 转为 Date
     *
     * @param localDateTime {@link LocalDateTime}
     * @return {@link Date}
     */
    public static Date toDate(LocalDateTime localDateTime) {
        if (null == localDateTime) {
            return null;
        }
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    /**
     * 计算时间差
     *
     * @param startDate 开始时间
     * @param endDate   接受时间
     * @return 时间差
     */
    public static Long calculateDateDifference(Date startDate, Date endDate) {
        if (null == startDate || null == endDate) {
            return null;
        }
        return endDate.getTime() - startDate.getTime();
    }

    /**
     * 解析定时器任务时间
     *
     * @param time 自定义触发时间
     * @return {@link Date}
     */
    public static Date parseTimerTaskTime(String time) {
        LocalDateTime expireTime = null;
        String[] timeArr = time.split(":");
        int l = timeArr.length;
        if (l == 2) {
            long vary = Long.parseLong(timeArr[0]);
            String unit = timeArr[1];
            if ("d".equals(unit)) {
                expireTime = DateUtils.now().plusDays(vary);
            } else if ("h".equals(unit)) {
                expireTime = DateUtils.now().plusHours(vary);
            } else if ("m".equals(unit)) {
                expireTime = DateUtils.now().plusMinutes(vary);
            }
        } else if (l == 3) {
            long hours = Long.parseLong(timeArr[0]);
            long minutes = Long.parseLong(timeArr[1]);
            long seconds = Long.parseLong(timeArr[2]);
            expireTime = DateUtils.now().plusHours(hours).plusMinutes(minutes).plusSeconds(seconds);
        }
        return DateUtils.toDate(expireTime);
    }
}
