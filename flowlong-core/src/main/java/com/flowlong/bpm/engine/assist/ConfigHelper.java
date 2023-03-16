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

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.math.NumberUtils;
import org.springframework.util.ResourceUtils;

import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Enumeration;
import java.util.Properties;

/**
 * 配置属性帮助类
 *
 * <p>
 * 尊重知识产权，CV 请保留版权，爱组搭 http://aizuda.com 出品
 * </p>
 *
 * @author hubin
 * @since 1.0
 */
@Slf4j
public class ConfigHelper {

    /**
     * 常用配置属性文件名称.
     */
    private final static String PROPERTIES_FILENAME = "long.properties";
    /**
     * 配置属性对象静态化
     */
    private static Properties properties;

    public static Properties getProperties() {
        if (properties == null) {
            synchronized (ConfigHelper.class) {
                if (properties == null) {
                    loadProperties(PROPERTIES_FILENAME);
                }
            }
        }
        return properties;
    }

    /**
     * 根据key获取配置的字符串value值
     *
     * @param key
     * @return
     */
    public static String getProperty(String key) {
        if (key == null) {
            return null;
        }
        return getProperties().getProperty(key);
    }

    /**
     * 根据key获取配置的数字value值
     *
     * @param key
     * @return
     */
    public static int getNumerProperty(String key) {
        String value = getProperties().getProperty(key);
        if (NumberUtils.isNumber(value)) {
            return Integer.parseInt(value);
        } else {
            return 0;
        }
    }

    public static void loadProperties(Properties props) {
        properties = props;
    }

    /**
     * 根据指定的文件名称，从类路径中加载属性文件，构造Properties对象
     *
     * @param resourceName 属性文件名称
     */
    public static void loadProperties(String resourceName) {
        properties = new Properties();
        ClassLoader classLoaderToUse = ClassUtils.getDefaultClassLoader();
        try {
            Enumeration<URL> urls = (classLoaderToUse != null ? classLoaderToUse.getResources(resourceName) :
                    ClassLoader.getSystemResources(resourceName));
            while (urls.hasMoreElements()) {
                URL url = urls.nextElement();
                URLConnection con = url.openConnection();
                ResourceUtils.useCachesIfNecessary(con);
                try (InputStream is = con.getInputStream()) {
                    if (resourceName.endsWith(".xml")) {
                        properties.loadFromXML(is);
                    } else {
                        properties.load(is);
                    }
                    log.info("Properties read " + properties);
                }
            }
        } catch (Exception e) {
            log.error("Error reading from " + resourceName, e);
        }
    }

}
