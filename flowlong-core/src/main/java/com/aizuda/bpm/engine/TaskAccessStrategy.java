/*
 * Copyright 2023-2025 Licensed under the apache-2.0 License
 * website: https://aizuda.com
 */
package com.aizuda.bpm.engine;

import com.aizuda.bpm.engine.assist.Assert;
import com.aizuda.bpm.engine.core.FlowCreator;
import com.aizuda.bpm.engine.entity.FlwTaskActor;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * 任务访问策略类
 * <p>
 * 用于判断给定的创建人员是否允许执行某个任务
 * </p>
 *
 * <p>
 * <a href="https://aizuda.com">官网</a>尊重知识产权，不允许非法使用，后果自负
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
     * @param taskActors 参与者列表
     * @return 被允许参与者 {@link FlwTaskActor}
     */
    FlwTaskActor isAllowed(String userId, List<FlwTaskActor> taskActors);

    /**
     * 获取指定合法参与者对象
     * <p>
     * 被使用在：分配任务，解决委派任务 场景
     * </p>
     *
     * @param taskId      当前任务ID
     * @param taskActors  通过任务ID查询到的任务参与者列表
     * @param flowCreator 任务参与者
     * @return {@link FlwTaskActor}
     */
    default FlwTaskActor getAllowedFlwTaskActor(Long taskId, FlowCreator flowCreator, List<FlwTaskActor> taskActors) {
        Optional<FlwTaskActor> taskActorOpt = taskActors.stream().filter(t -> Objects.equals(t.getActorId(), flowCreator.getCreateId())).findFirst();
        Assert.isTrue(!taskActorOpt.isPresent(), "Not authorized to perform this task");
        return taskActorOpt.get();
    }
}
