/*
 * 爱组搭 http://aizuda.com 低代码组件化开发平台
 * ------------------------------------------
 * 受知识产权保护，请勿删除版权申明
 */
package com.flowlong.bpm.engine.model;

import lombok.Getter;
import lombok.Setter;

/**
 * 爱组搭 http://aizuda.com
 * ----------------------------------------
 * JSON BPM 节点表达式条件
 *
 * @author 青苗
 * @since 2023-03-17
 */
@Getter
@Setter
public class NodeExpression {
    /**
     * 名称
     */
    private String label;
    /**
     * 属性
     */
    private String field;
    /**
     * 操作
     */
    private String operator;
    /**
     * 内容
     */
    private String value;

}
