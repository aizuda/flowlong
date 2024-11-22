/*
 * Copyright 2023-2025 Licensed under the apache-2.0 License
 * website: https://aizuda.com
 */
package com.aizuda.bpm.engine.scheduling;

import lombok.Getter;
import lombok.Setter;

/**
 * 提醒参数
 *
 * <p>
 * <a href="https://aizuda.com">官网</a>尊重知识产权，不允许非法使用，后果自负
 * </p>
 *
 * @author hubin
 * @since 1.0
 */
@Getter
@Setter
public class RemindParam {
    /**
     * 提醒时间 cron 表达式
     */
    private String cron;
    /**
     * 工作日设置，格式为 1,2,3...7，表示周一至周日
     */
    private String weeks;
    /**
     * 工作时间设置，格式为 8:00-18:00
     */
    private String workTime;

}
