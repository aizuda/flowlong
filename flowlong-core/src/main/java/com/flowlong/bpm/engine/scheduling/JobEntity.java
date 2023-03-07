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
package com.flowlong.bpm.engine.scheduling;

import com.flowlong.bpm.engine.entity.Task;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

/**
 * job实体，用于传递给具体的调度框架
 *
 * <p>
 * 尊重知识产权，CV 请保留版权，爱组搭 http://aizuda.com 出品
 * </p>
 *
 * @author hubin
 * @since 1.0
 */
@Getter
@Setter
public class JobEntity implements Serializable {
    /**
     * job主键
     */
    private String key;
    /**
     * job组
     */
    private String group;
    /**
     * 任务对应的业务id串
     */
    private String id;
    /**
     * 节点模型名称
     */
    private String modelName;
    /**
     * job类型
     */
    private int jobType;
    /**
     * 任务对象
     */
    private Task task;
    /**
     * 启动时间
     */
    private Date startTime;
    /**
     * 间隔时间(分钟)
     */
    private int period;
    /**
     * 执行参数
     */
    private Map<String, Object> args;

    public JobEntity(String id, Task task, Date startTime) {
        this(id, task, startTime, 0);
    }

    public JobEntity(String id, Task task, Date startTime, int period) {
        this.id = id;
        this.task = task;
        this.startTime = startTime;
        this.period = period;
    }

    public JobEntity(String id, Task task, Date startTime, Map<String, Object> args) {
        this(id, task, startTime, args, 0);
    }

    public JobEntity(String id, Task task, Date startTime, Map<String, Object> args, int period) {
        this.id = id;
        this.task = task;
        this.startTime = startTime;
        this.period = period;
        this.args = args;
    }

    /**
     * 参与类型
     */
    public enum JobType {
        EXECUTER, REMINDER;
    }
}
