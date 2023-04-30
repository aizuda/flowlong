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
 * JSON BPM 分配到任务的人
 *
 * @author 青苗
 * @since 2023-03-17
 */
@Getter
@Setter
public class NodeAssignee {
    /**
     * 主键ID
     */
    private String id;
    /**
     * 名称
     */
    private String name;

}
