/*
 * Copyright 2023-2025 Licensed under the Dual Licensing
 * website: https://aizuda.com
 */
package com.aizuda.bpm.engine.scheduling;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 任务执行锁接口
 *
 * <p>
 * <a href="https://aizuda.com">官网</a>尊重知识产权，不允许非法使用，后果自负
 * </p>
 *
 * @author hubin
 * @since 1.0
 */
public class LocalLock implements JobLock {
    /**
     * 防重入锁
     */
    private static final Lock LOCAL_LOCK = new ReentrantLock();

    @Override
    public boolean tryLock() {
        return LOCAL_LOCK.tryLock();
    }

    @Override
    public void unlock() {
        LOCAL_LOCK.unlock();
    }
}