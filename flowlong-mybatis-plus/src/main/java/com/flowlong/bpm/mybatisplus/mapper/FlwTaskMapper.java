/*
 * Copyright 2023-2025 Licensed under the AGPL License
 */
package com.flowlong.bpm.mybatisplus.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.flowlong.bpm.engine.assist.Assert;
import com.flowlong.bpm.engine.entity.FlwTask;

import java.util.List;

/**
 * 任务 Mapper
 *
 * <p>
 * 尊重知识产权，不允许非法使用，后果自负
 * </p>
 *
 * @author hubin
 * @since 1.0
 */
public interface FlwTaskMapper extends BaseMapper<FlwTask> {

    /**
     * 获取任务并检查ID的合法性
     *
     * @param id 任务ID
     * @return {@link FlwTask}
     */
    default FlwTask getCheckById(Long id) {
        FlwTask flwTask = selectById(id);
        Assert.isNull(flwTask, "The specified task [id=" + id + "] does not exist");
        return flwTask;
    }

    /**
     * 根据流程实例ID获取任务列表
     *
     * @param instanceId 流程实例ID
     */
    default List<FlwTask> selectListByInstanceId(Long instanceId) {
        return this.selectList(Wrappers.<FlwTask>lambdaQuery().eq(FlwTask::getInstanceId, instanceId));
    }

}
