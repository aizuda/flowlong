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
        ProcessModel bpmModel = FlowLongContext.JSON_HANDLER.fromJson(content, ProcessModel.class);
        Assert.isNull(bpmModel, "bpmn json parser error");
        return bpmModel;
    }
}
