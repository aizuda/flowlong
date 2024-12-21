/*
 * Copyright 2023-2025 Licensed under the Dual Licensing
 * website: https://aizuda.com
 */
package com.aizuda.bpm.solon;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import org.noear.solon.core.AppContext;
import org.noear.solon.core.Plugin;


/**
 * solon 自动装配是用编码模式的（配置 XPluginImpl 后，由它以编码处理装配）
 *
 * <p>
 * <a href="https://aizuda.com">官网</a>尊重知识产权，不允许非法使用，后果自负
 * </p>
 *
 * @author noear
 * @since 1.0
 */
public class XPluginImpl implements Plugin {

    @Override
    public void start(AppContext context) throws Throwable {
        // 扫描整个插件下所有 Bean
        context.beanScan(XPluginImpl.class);

        context.onEvent(MybatisConfiguration.class, e -> {
            if ("flowlong".equals(e.getEnvironment().getId())) {
                e.addMappers("com.aizuda.bpm.mybatisplus.mapper");
            }
        });
    }
}
