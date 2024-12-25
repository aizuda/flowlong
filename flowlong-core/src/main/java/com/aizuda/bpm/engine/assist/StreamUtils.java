/*
 * Copyright 2023-2025 Licensed under the Dual Licensing
 * website: https://aizuda.com
 */
package com.aizuda.bpm.engine.assist;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.function.Function;

/**
 * 流数据帮助类
 *
 * <p>
 * <a href="https://aizuda.com">官网</a>尊重知识产权，不允许非法使用，后果自负
 * </p>
 *
 * @author hubin
 * @since 1.0
 */
public class StreamUtils {

    public static InputStream getResourceAsStream(String name) {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        InputStream stream = classLoader.getResourceAsStream(name);
        if (null == stream) {
            stream = StreamUtils.class.getClassLoader().getResourceAsStream(name);
        }
        if (stream == null) {
            throw Assert.throwable("resource " + name + " does not exist");
        }
        return stream;
    }

    public static <T> T readBytes(InputStream in, Function<String, T> function) {
        Assert.isNull(in);
        try {
            return function.apply(readBytes(in));
        } catch (Exception e) {
            throw Assert.throwable(e.getMessage(), e);
        }
    }

    public static String readBytes(InputStream in) throws IOException {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[4096];
            for (int count; (count = in.read(buffer)) != -1; ) {
                out.write(buffer, 0, count);
            }
            return out.toString(StandardCharsets.UTF_8.toString());
        }
    }
}
