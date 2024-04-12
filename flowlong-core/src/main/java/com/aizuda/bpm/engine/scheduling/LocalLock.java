/*
 * Copyright 2023-2025 Licensed under the AGPL License
 */
package com.aizuda.bpm.engine.scheduling;

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
    public boolean tryLock() {
        return getLocalLock().tryLock();
    }

    @Override
    public void unlock() {
        getLocalLock().unlock();
    }
}
