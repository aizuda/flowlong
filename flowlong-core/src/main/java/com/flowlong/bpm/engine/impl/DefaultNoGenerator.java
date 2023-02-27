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
package com.flowlong.bpm.engine.impl;

import com.flowlong.bpm.engine.NoGenerator;
import com.flowlong.bpm.engine.model.ProcessModel;
import org.joda.time.DateTime;

import java.util.Random;

/**
 * 默认的流程实例编号生成器
 * 编号生成规则为:yyyyMMdd-HH:mm:ss-SSS-random
 *
 * @author hubin
 * @since 1.0
 */
public class DefaultNoGenerator implements NoGenerator {

    @Override
    public String generate(ProcessModel model) {
        String time = new DateTime().toString("yyyyMMdd-HH:mm:ss-SSS");
        return time + "-" + RandomHolder.RANDOM.nextInt(1000);
    }

    private static class RandomHolder {
        private static final Random RANDOM = new Random();
    }
}
