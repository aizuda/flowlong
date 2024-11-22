/*
 * Copyright 2023-2025 Licensed under the apache-2.0 License
 * website: https://aizuda.com
 */
package com.aizuda.bpm.engine.scheduling;

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
public interface JobLock {

    /**
     * 进入锁（获取锁），立即返回，不会阻塞等待锁
     *
     * @return true 获取到锁 false 未获取到锁
     */
    boolean tryLock();

    /**
     * 解除锁
     */
    void unlock();
}
