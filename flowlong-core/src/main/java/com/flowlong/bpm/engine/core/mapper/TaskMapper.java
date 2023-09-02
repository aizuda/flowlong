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
package com.flowlong.bpm.engine.core.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.flowlong.bpm.engine.assist.Assert;
import com.flowlong.bpm.engine.entity.Task;

import java.util.Objects;

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
public interface TaskMapper extends BaseMapper<Task> {

    /**
     * 获取任务并检查ID的合法性
     *
     * @param id 任务ID
     * @return {@link Task}
     */
    default Task getCheckById(Long id) {
        Task task = selectById(id);
        Assert.notNull(task, "指定的任务[id=" + id + "]不存在");
        return task;
    }
}
