/* Copyright 2023-2025 jobob@qq.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
 * 尊重知识产权，CV 请保留版权，爱组搭 http://aizuda.com 出品，不允许非法使用，后果自负
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
        Assert.notNull(flwTask, "指定的任务[id=" + id + "]不存在");
        return flwTask;
    }

    /**
     * 根据流程实例ID获取任务列表
     *
     * @param instanceId 流程实例ID
     * @return
     */
    default List<FlwTask> selectListByInstanceId(Long instanceId) {
        return this.selectList(Wrappers.<FlwTask>lambdaQuery().eq(FlwTask::getInstanceId, instanceId));
    }

}
