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
package com.flowlong.bpm.engine.handler.impl;

import com.flowlong.bpm.engine.QueryService;
import com.flowlong.bpm.engine.core.Execution;
import com.flowlong.bpm.engine.core.FlowLongContext;
import com.flowlong.bpm.engine.entity.Instance;
import com.flowlong.bpm.engine.handler.FlowLongHandler;
import com.flowlong.bpm.engine.model.ProcessModel;

import java.util.List;

/**
 * 合并处理的抽象处理器
 * 需要子类提供查询无法合并的task集合的参数map
 *
 * <p>
 * 尊重知识产权，CV 请保留版权，爱组搭 http://aizuda.com 出品
 * </p>
 *
 * @author hubin
 * @since 1.0
 */
public abstract class AbstractMergeHandler implements FlowLongHandler {

    @Override
    public void handle(FlowLongContext flowLongContext, Execution execution) {
        /**
         * 查询当前流程实例的无法参与合并的node列表
         * 若所有中间node都完成，则设置为已合并状态，告诉model可继续执行join的输出变迁
         */
        QueryService queryService = execution.getEngine().queryService();
        Instance instance = execution.getInstance();
        ProcessModel model = execution.getProcess().getProcessModel();
        List<String> activeNodes = findActiveNodes();
        boolean isSubProcessMerged = false;
        boolean isTaskMerged = false;

//        if (model.containsNodeNames(SubProcessModel.class, activeNodes)) {
//            QueryFilter filter = new QueryFilter().setParentId(instance.getId())
//                    .setExcludedIds(new String[]{execution.getChildInstanceId()});
//            List<Instance> instances = queryService.getActiveInstances(filter);
//            //如果所有子流程都已完成，则表示可合并
//            if (instances == null || instances.isEmpty()) {
//                isSubProcessMerged = true;
//            }
//        } else {
//            isSubProcessMerged = true;
//        }
//        if (isSubProcessMerged && model.containsNodeNames(TaskModel.class, activeNodes)) {
//            QueryFilter filter = new QueryFilter().
//                    setInstanceId(instance.getId()).
//                    setExcludedIds(new String[]{execution.getTask().getId()}).
//                    setNames(activeNodes);
//            List<Task> tasks = queryService.getActiveTasks(filter);
//            if (tasks == null || tasks.isEmpty()) {
//                //如果所有task都已完成，则表示可合并
//                isTaskMerged = true;
//            }
//        }
        execution.setMerged(isSubProcessMerged && isTaskMerged);
    }

    /**
     * 子类需要提供如何查询未合并任务的参数map
     *
     * @return
     */
    protected abstract List<String> findActiveNodes();
}
