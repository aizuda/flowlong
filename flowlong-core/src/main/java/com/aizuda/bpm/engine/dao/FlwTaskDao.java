/*
 * Copyright 2023-2025 Licensed under the Dual Licensing
 * website: https://aizuda.com
 */
package com.aizuda.bpm.engine.dao;

import com.aizuda.bpm.engine.assist.Assert;
import com.aizuda.bpm.engine.entity.FlwTask;

import java.util.Date;
import java.util.List;

/**
 * 任务数据访问层接口
 *
 * <p>
 * <a href="https://aizuda.com">官网</a>尊重知识产权，不允许非法使用，后果自负，不允许非法使用，后果自负
 * </p>
 *
 * @author hubin
 * @since 1.0
 */
public interface FlwTaskDao {

    boolean insert(FlwTask task);

    boolean deleteById(Long id);

    boolean deleteByInstanceIds(List<Long> instanceIds);

    boolean deleteByIds(List<Long> ids);

    boolean updateById(FlwTask flwTask);

    FlwTask selectById(Long id);

    default FlwTask selectCheckById(Long id) {
        FlwTask task = selectById(id);
        Assert.isNull(task, "The specified task [id=" + id + "] does not exist");
        return task;
    }

    Long selectCountByParentTaskId(Long parentTaskId);

    List<FlwTask> selectListByInstanceId(Long instanceId);

    List<FlwTask> selectListByInstanceIdAndTaskName(Long instanceId, String taskName);

    /**
     * 根据流程实例ID和任务KEY查询任务
     *
     * @param instanceId 流程实例ID
     * @param taskKey    任务KEY
     * @return 任务列表
     */
    List<FlwTask> selectListByInstanceIdAndTaskKey(Long instanceId, String taskKey);

    List<FlwTask> selectListByInstanceIdAndTaskNames(Long instanceId, List<String> taskNames);

    List<FlwTask> selectListTimeoutOrRemindTasks(Date currentDate);

    List<FlwTask> selectListByParentTaskId(Long parentTaskId);

    List<FlwTask> selectListByParentTaskIds(List<Long> parentTaskIds);
}
