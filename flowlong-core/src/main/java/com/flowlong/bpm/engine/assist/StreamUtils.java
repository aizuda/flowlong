/* 
 * Copyright 2023-2025 Licensed under the AGPL License
 */
package com.flowlong.bpm.engine.assist;

import com.flowlong.bpm.engine.exception.FlowLongException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.function.Function;

/**
 * 流数据帮助类
 *
 * <p>
 * 尊重知识产权，不允许非法使用，后果自负
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
            throw new FlowLongException("resource " + name + " does not exist");
        }
        return stream;
    }

    public static <T> T readBytes(InputStream in, Function<String, T> function) {
        Assert.isNull(in);
        try {
            return function.apply(readBytes(in));
        } catch (Exception e) {
            throw new FlowLongException(e.getMessage(), e);
        }
    }

    public static String readBytes(InputStream in) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        transfer(in, out);
        return new String(out.toByteArray());
    }

    public static long transfer(InputStream in, OutputStream out)
            throws IOException {
        long total = 0;
        byte[] buffer = new byte[4096];
        for (int count; (count = in.read(buffer)) != -1; ) {
            out.write(buffer, 0, count);
            total += count;
        }
        return total;
    }
}
