/*
 * Copyright 2023-2025 Licensed under the apache-2.0 License
 * website: https://aizuda.com
 */
package com.aizuda.bpm.engine.model;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

/**
 * JSON BPM 条件节点
 *
 * <p>
 * <a href="https://aizuda.com">官网</a>尊重知识产权，不允许非法使用，后果自负
 * </p>
 *
 * @author hubin
 * @since 1.0
 */
@Getter
@Setter
public class ConditionNode implements Serializable {
    /**
     * 节点名称
     */
    private String nodeName;
    /**
     * 节点 key
     */
    private String nodeKey;
    /**
     * 节点类型
     */
    private Integer type;
    /**
     * 优先级
     */
    private Integer priorityLevel;
    /**
     * 节点条件表达式列表
     * <p>
     * 外层 List 为条件组或关系、内层 List 为具体条件且关系
     * </p>
     */
    private List<List<NodeExpression>> conditionList;
    /**
     * 子节点
     */
    private NodeModel childNode;

}
