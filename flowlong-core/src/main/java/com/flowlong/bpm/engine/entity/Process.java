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
import com.flowlong.bpm.engine.handler.impl.CreateTaskHandler;
import com.flowlong.bpm.engine.handler.impl.EndProcessHandler;
import com.flowlong.bpm.engine.model.NodeModel;
import com.flowlong.bpm.engine.model.ProcessModel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Objects;
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
public class Process extends FlowEntity {
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
    protected String content;

    public void setFlowState(FlowState flowState) {
        this.state = flowState.getValue();
    }

    /**
     * 模型解析
     */
    public ProcessModel getProcessModel() {
        return null == this.content ? null : ProcessModel.parse(this.content);
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
            NodeModel executeNode = nodeModel.getChildNode();
            if (null == executeNode) {
                // 如果当前节点完成，并且该节点为条件节点，找到主干执行节点继续执行
                executeNode = this.findNextNode(nodeModel);
            }

            /**
             * 执行节点任务
             */
            if (null != executeNode) {
                // 执行流程节点
                executeNode.execute(flowLongContext, execution);

                /**
                 * 不存在子节点，不存在其它分支节点，当前执行节点为最后节点
                 * 执行结束流程处理器
                 */
                if (null == executeNode.getChildNode() && null == executeNode.getConditionNodes()) {
                    NodeModel nextNode = this.findNextNode(executeNode);
                    if (null == nextNode || Objects.equals(executeNode.getNodeName(), nextNode.getNodeName())) {
                        new EndProcessHandler().handle(flowLongContext, execution);
                    }
                }
            } else {
                /**
                 * 无执行节点流程结束
                 */
                new EndProcessHandler().handle(flowLongContext, execution);
            }
        });
    }

    private NodeModel findNextNode(NodeModel nodeModel) {
        NodeModel parentNode = nodeModel.getParentNode();
        if (null == parentNode || Objects.equals(0, parentNode.getType())) {
            // 递归至发起节点，流程结束
            return null;
        }

        if (parentNode.isConditionNode()) {
            // 条件执行节点，返回子节点
            return parentNode.getChildNode();
        }

        // 往上继续找下一个执行节点
        return this.findNextNode(parentNode);
    }

    /**
     * 执行开始模型
     *
     * @param flowLongContext 流程引擎上下文
     * @param execution       流程执行对象
     */
    public void executeStartModel(FlowLongContext flowLongContext, Execution execution) {
        this.processModelParser(processModel -> {
            NodeModel nodeModel = processModel.getNodeConfig();
            Assert.notNull(nodeModel, "流程定义[name=" + this.name + ", version=" + this.version + "]没有开始节点");
            // 创建首个审批任务
            new CreateTaskHandler(nodeModel).handle(flowLongContext, execution);
        });
    }

    /**
     * 流程模型解析
     *
     * @param consumer 解析模型消费者
     */
    private void processModelParser(Consumer<ProcessModel> consumer) {
        if (null != this.content) {
            consumer.accept(ProcessModel.parse(this.content));
        }
    }
}
