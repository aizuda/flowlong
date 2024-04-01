/* 
 * Copyright 2023-2025 Licensed under the AGPL License
 */
package com.aizuda.bpm.engine;

import com.aizuda.bpm.engine.entity.FlwTaskActor;

import java.util.List;

/**
 * 任务访问策略类
 * <p>
 * 用于判断给定的创建人员是否允许执行某个任务
 * </p>
 *
 * <p>
 * 尊重知识产权，不允许非法使用，后果自负
 * </p>
 *
 * @author hubin
 * @since 1.0
 */
public interface TaskAccessStrategy {

    /**
     * 根据创建人ID、参与者集合判断是否允许访问所属任务
     *
     * @param userId     用户ID
     * @param taskActors 参与者列表 传递至该接口的实现类中的参与者都是为非空
     * @return boolean 是否允许访问
     */
    boolean isAllowed(String userId, List<FlwTaskActor> taskActors);
}
