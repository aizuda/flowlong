/*
 * 爱组搭 http://aizuda.com 低代码组件化开发平台
 * ------------------------------------------
 * 受知识产权保护，请勿删除版权申明
 */
package com.flowlong.bpm.engine.model;

import com.flowlong.bpm.engine.assist.ObjectUtils;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 爱组搭 http://aizuda.com
 * ----------------------------------------
 * JSON BPM 条件节点
 *
 * @author 青苗
 * @since 2023-03-17
 */
@Getter
@Setter
public class ConditionNode {
    /**
     * 节点名称
     */
    private String nodeName;
    /**
     * 节点类型
     */
    private Integer type;
    /**
     * 优先级
     */
    private Integer priorityLevel;
    /**
     * 条件关系 0，且 1，或
     */
    private Integer conditionMode;
    /**
     * 节点条件表达式列表
     */
    private List<NodeExpression> conditionList;
    /**
     * 子节点
     */
    private NodeModel childNode;

    /**
     * 字符串 SpEL 表达式条件
     */
    public String getExpr() {
        if (ObjectUtils.isNotEmpty(this.conditionList)) {
            return conditionList.stream().map(t -> "#" + t.getField() + t.getOperator() + t.getValue())
                    .collect(Collectors.joining(0 == conditionMode ? " && " : " || "));
        }
        return null;
    }

}
