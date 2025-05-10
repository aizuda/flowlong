/*
 * Copyright 2023-2025 Licensed under the Dual Licensing
 * website: https://aizuda.com
 */
package com.aizuda.bpm.engine.dao;

import com.aizuda.bpm.engine.assist.Assert;
import com.aizuda.bpm.engine.entity.FlwHisTask;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * 历史任务数据访问层接口
 *
 * <p>
 * <a href="https://aizuda.com">官网</a>尊重知识产权，不允许非法使用，后果自负，不允许非法使用，后果自负
 * </p>
 *
 * @author hubin
 * @since 1.0
 */
public interface FlwHisTaskDao {

    boolean insert(FlwHisTask hisTask);

    boolean deleteById(Long id);

    boolean deleteByInstanceIds(List<Long> instanceIds);

    boolean updateById(FlwHisTask hisTask);

    FlwHisTask selectById(Long id);

    default FlwHisTask selectCheckById(Long id) {
        FlwHisTask flwHisTask = selectById(id);
        Assert.isNull(flwHisTask, "The specified hisTask [id=" + id + "] does not exist");
        return flwHisTask;
    }

    FlwHisTask selectStartTaskByInstanceId(Long instanceId);

    List<FlwHisTask> selectListByInstanceIdAndTaskName(Long instanceId, String taskName);

    Optional<List<FlwHisTask>> selectListByInstanceId(Long instanceId);

    Optional<List<FlwHisTask>> selectListByInstanceIdAndTaskState(Long instanceId, Integer taskState);

    List<FlwHisTask> selectListByCallProcessIdAndCallInstanceId(Long callProcessId, Long callInstanceId);

    List<FlwHisTask> selectListByParentTaskId(Long parentTaskId);

    Collection<FlwHisTask> selectListByInstanceIdAndTaskNameAndParentTaskId(Long instanceId, String taskName, Long parentTaskId);
}
