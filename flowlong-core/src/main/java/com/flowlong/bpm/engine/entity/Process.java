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
package com.flowlong.bpm.engine.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.flowlong.bpm.engine.assist.Assert;
import com.flowlong.bpm.engine.core.Execution;
import com.flowlong.bpm.engine.core.FlowLongContext;
import com.flowlong.bpm.engine.core.enums.FlowState;
import com.flowlong.bpm.engine.model.NodeModel;
import com.flowlong.bpm.engine.model.ProcessModel;
import com.flowlong.bpm.engine.model.StartModel;
import com.flowlong.bpm.engine.parser.ModelParser;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.function.Consumer;

/**
 * 流程定义实体类
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
@ToString
@TableName("flw_process")
public class Process extends BaseEntity {
    /**
     * 流程定义名称
     */
    protected String name;
    /**
     * 流程定义显示名称
     */
    protected String displayName;
    /**
     * 流程定义类型（预留字段）
     */
    protected String type;
    /**
     * 版本
     */
    protected Integer version;
    /**
     * 当前流程的实例url（一般为流程第一步的url）
     * 该字段可以直接打开流程申请的表单
     */
    protected String instanceUrl;
    /**
     * 是否可用的开关
     */
    protected Integer state;
    /**
     * 流程定义xml
     */
    protected byte[] content;

    public void setFlowState(FlowState flowState) {
        this.state = flowState.getValue();
    }

    /**
     * 模型解析
     */
    public ProcessModel getProcessModel() {
        return null == this.content ? null : ModelParser.parse(this.content);
    }

    /**
     * 执行节点模型
     *
     * @param flowLongContext 流程引擎上下文
     * @param execution       流程执行对象
     * @param nodeName        节点名称
     */
    public void executeNodeModel(FlowLongContext flowLongContext, Execution execution, String nodeName) {
        this.processModelParser(processModel -> {
            NodeModel nodeModel = processModel.getNode(nodeName);
            Assert.notNull(nodeModel, "流程模型中未发现，流程节点" + nodeName);
            nodeModel.execute(flowLongContext, execution);
        });
    }

    /**
     * 执行开始模型
     *
     * @param flowLongContext 流程引擎上下文
     * @param execution       流程执行对象
     */
    public void executeStartModel(FlowLongContext flowLongContext, Execution execution) {
        this.processModelParser(processModel -> {
            StartModel start = processModel.getStart();
            Assert.notNull(start, "流程定义[name=" + this.name + ", version=" + this.version + "]没有开始节点");
            start.execute(flowLongContext, execution);
        });
    }

    /**
     * 流程模型解析
     *
     * @param consumer 解析模型消费者
     */
    private void processModelParser(Consumer<ProcessModel> consumer) {
        if (null != this.content) {
            consumer.accept(ModelParser.parse(this.content));
        }
    }
}
