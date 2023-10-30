/*
 *  Copyright 2023-2025 jobob@qq.com
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */
package com.flowlong.bpm.engine.scheduling;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 任务执行锁接口
 *
 * <p>
 * 尊重知识产权，不允许非法使用，后果自负
 * </p>
 *
 * @author hubin
 * @since 1.0
 */
public class LocalLock implements JobLock {

    /**
     * 防重入锁
     */
    private static Lock LOCAL_LOCK;

    public static Lock getLocalLock() {
        if (null == LOCAL_LOCK) {
            synchronized (LocalLock.class) {
                LOCAL_LOCK = new ReentrantLock();
            }
        }
        return LOCAL_LOCK;
    }

    @Override
    public void lock() {
        getLocalLock().lock();
    }

    @Override
    public void unlock() {
        getLocalLock().unlock();
    }
}
