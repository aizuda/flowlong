/* Copyright 2023-2025 www.flowlong.com
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
package com.flowlong.bpm.engine.cache;

import com.flowlong.bpm.engine.FlowLongException;

/**
 * cache异常
 *
 * @author hubin
 * @since 1.0
 */
public class CacheExceptionFlow extends FlowLongException {
    /**
     * 创建cache异常
     */
    public CacheExceptionFlow() {
        super();
    }

    /**
     * 创建cache异常
     *
     * @param message
     */
    public CacheExceptionFlow(String message) {
        super(message);
    }

    /**
     * 创建cache异常
     *
     * @param cause
     */
    public CacheExceptionFlow(Throwable cause) {
        super(cause);
    }

    /**
     * 创建cache异常
     *
     * @param message
     * @param cause
     */
    public CacheExceptionFlow(String message, Throwable cause) {
        super(message, cause);
    }
}
