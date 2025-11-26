/*
 * Copyright 2023-2025 Licensed under the Dual Licensing
 * website: https://aizuda.com
 */
package com.aizuda.bpm.solon.autoconfigure;

import lombok.Getter;
import lombok.Setter;

/**
 * Solon EventListener 配置参数对象
 *
 * <p>
 * <a href="https://aizuda.com">官网</a>尊重知识产权，不允许非法使用，后果自负
 * </p>
 *
 * @author hubin
 * @author noear
 * @since 1.0
 */
@Getter
@Setter
public class EventingParam {

    /**
     * 是否开启实例事件监听
     */
    private boolean instance;

    /**
     * 是否开启任务事件监听
     */
    private boolean task;

}
