/*
 * Copyright 2023-2025 Licensed under the Dual Licensing
 * website: https://aizuda.com
 */
package com.aizuda.bpm.solon.autoconfigure;

import com.aizuda.bpm.engine.scheduling.RemindParam;
import lombok.Getter;
import lombok.Setter;
import org.noear.solon.annotation.BindProps;
import org.noear.solon.annotation.Configuration;
import org.noear.solon.annotation.Inject;

/**
 * 配置属性
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
@BindProps(prefix = "flowlong")
@Configuration
public class FlowLongProperties {
    /**
     * 是否打印 banner
     */
    private boolean banner = true;

    /**
     * 提醒时间
     */
    private RemindParam remind;

    /**
     * 事件监听配置
     */
    private EventingParam eventing;
}