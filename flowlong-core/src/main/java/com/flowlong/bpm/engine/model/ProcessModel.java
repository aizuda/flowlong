/*
 * 爱组搭 http://aizuda.com 低代码组件化开发平台
 * ------------------------------------------
 * 受知识产权保护，请勿删除版权申明
 */
package com.flowlong.bpm.engine.model;

import com.flowlong.bpm.engine.assist.Assert;
import com.flowlong.bpm.engine.core.FlowLongContext;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 爱组搭 http://aizuda.com
 * ----------------------------------------
 * JSON BPM 模型
 *
 * @author 青苗
 * @since 2023-03-17
 */
@Getter
@Setter
public class ProcessModel {
    /**
     * 节点名称
     */
    private String name;
    /**
     * 实例地址
     */
    private String instanceUrl;
    /**
     * 节点信息
     */
    private NodeModel nodeConfig;

    /**
     * 获取process定义的指定节点名称的节点模型
     *
     * @param nodeName 节点名称
     * @return {@link NodeModel}
     */
    public NodeModel getNode(String nodeName) {
        return null == nodeConfig ? null : nodeConfig.getNode(nodeName);
    }

    /**
     * 流程文件字节码解析为流程模型
     *
     * @param content 流程定义内容
     */
    public static ProcessModel parse(String content) {
        ProcessModel processModel = FlowLongContext.JSON_HANDLER.fromJson(content, ProcessModel.class);
        Assert.isNull(processModel, "process model json parser error");
        processModel.buildParentNode(processModel.getNodeConfig());
        return processModel;
    }

    /**
     * 构建父节点
     *
     * @param rootNode 根节点
     */
    protected void buildParentNode(NodeModel rootNode) {
        List<ConditionNode> conditionNodes = rootNode.getConditionNodes();
        if (null != conditionNodes) {
            for (ConditionNode conditionNode : conditionNodes) {
                NodeModel conditionChildNode = conditionNode.getChildNode();
                if (null != conditionChildNode) {
                    conditionChildNode.setParentNode(rootNode);
                    this.buildParentNode(conditionChildNode);
                }
            }
        }
        NodeModel childNode = rootNode.getChildNode();
        if (null != childNode) {
            childNode.setParentNode(rootNode);
            this.buildParentNode(childNode);
        }
    }
}
