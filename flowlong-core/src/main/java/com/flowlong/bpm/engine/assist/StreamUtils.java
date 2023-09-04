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
 * 尊重知识产权，CV 请保留版权，爱组搭 http://aizuda.com 出品，不允许非法使用，后果自负
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
        Assert.notNull(in);
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
