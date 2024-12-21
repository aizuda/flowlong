/*
 * Copyright 2023-2025 Licensed under the Dual Licensing
 * website: https://aizuda.com
 */
package com.aizuda.bpm.engine.impl;

import com.aizuda.bpm.engine.TaskAccessStrategy;
import com.aizuda.bpm.engine.assist.ObjectUtils;
import com.aizuda.bpm.engine.entity.FlwTaskActor;

import java.util.List;
import java.util.Objects;

/**
 * 基于用户或组（角色、部门等）的访问策略类
 * 该策略类适合组作为参与者的情况
 *
 * <p>
 * <a href="https://aizuda.com">官网</a>尊重知识产权，不允许非法使用，后果自负
 * </p>
 *
 * @author hubin
 * @since 1.0
 */
public class GeneralAccessStrategy implements TaskAccessStrategy {

    /**
     * 如果创建人ID所属的组只要有一项存在于参与者集合中，则表示可访问
     */
    @Override
    public FlwTaskActor isAllowed(String userId, List<FlwTaskActor> taskActors) {
        if (null == userId || ObjectUtils.isEmpty(taskActors)) {
            return null;
        }
        // 参与者 ID 默认非组，作为用户ID判断是否允许执行
        return taskActors.stream().filter(t -> Objects.equals(t.getActorId(), userId))
                .findFirst().orElse(null);
    }
}
