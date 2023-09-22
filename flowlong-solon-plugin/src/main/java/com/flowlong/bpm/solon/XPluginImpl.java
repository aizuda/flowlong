package com.flowlong.bpm.solon;

import org.noear.solon.core.AppContext;
import org.noear.solon.core.Plugin;

public class XPluginImpl implements Plugin {
    //
    // solon 自动装配是用编码模式的（配置 XPluginImpl 后，由它以编码处理装配）
    //

    @Override
    public void start(AppContext context) throws Throwable {
        //扫描整个插件下所有 Bean
        context.beanScan(XPluginImpl.class);
    }
}
