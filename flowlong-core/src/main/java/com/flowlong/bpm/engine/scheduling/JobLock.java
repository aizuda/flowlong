/*
 * Copyright 2023-2025 Licensed under the AGPL License
 */
package com.flowlong.bpm.engine.scheduling;

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
public interface JobLock {

    /**
     * 进入锁
     */
    void lock();

    /**
     * 解除锁
     */
    void unlock();
}
