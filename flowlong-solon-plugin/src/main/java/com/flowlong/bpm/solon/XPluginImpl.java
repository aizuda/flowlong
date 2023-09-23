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
package com.flowlong.bpm.solon;

import com.flowlong.bpm.mybatisplus.service.ProcessServiceImpl;
import com.flowlong.bpm.mybatisplus.service.QueryServiceImpl;
import com.flowlong.bpm.mybatisplus.service.RuntimeServiceImpl;
import com.flowlong.bpm.mybatisplus.service.TaskServiceImpl;
import org.noear.solon.core.AppContext;
import org.noear.solon.core.Plugin;

import java.lang.reflect.Constructor;

/**
 * solon 自动装配是用编码模式的（配置 XPluginImpl 后，由它以编码处理装配）
 *
 * <p>
 * 尊重知识产权，CV 请保留版权，爱组搭 http://aizuda.com 出品，不允许非法使用，后果自负
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
        context.lifecycle(() -> {
            //顺序不能改
            buildServiceBean(context, QueryServiceImpl.class);
            buildServiceBean(context, TaskServiceImpl.class);
            buildServiceBean(context, RuntimeServiceImpl.class);
            buildServiceBean(context, ProcessServiceImpl.class);
        });
    }

    private void buildServiceBean(AppContext context, Class<?> clz) throws Exception {
        Constructor c = clz.getDeclaredConstructors()[0];
        Object[] args = new Object[c.getParameterCount()];

        for (int i = 0; i < args.length; i++) {
            args[i] = context.getBean(c.getParameterTypes()[i]);
        }

        Object obj = c.newInstance(args);
        context.wrapAndPut(clz, obj);

        for (Class<?> clz1 : clz.getInterfaces()) {
            context.wrapAndPut(clz1, obj);
        }
    }
}
