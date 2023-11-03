/*
 * 爱组搭 http://aizuda.com 低代码组件化开发平台
 * ------------------------------------------
 * 受知识产权保护，请勿删除版权申明
 */
package com.flowlong.bpm.engine.model;

import com.flowlong.bpm.engine.entity.FlwTaskActor;
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
    /**
     * 权重（ 用于票签，多个参与者合计权重 100% ）
     */
    private Integer weight;

    public static NodeAssignee of(FlwTaskActor flwTaskActor) {
        NodeAssignee nodeAssignee = new NodeAssignee();
        nodeAssignee.setId(flwTaskActor.getActorId());
        nodeAssignee.setName(flwTaskActor.getActorName());
        return nodeAssignee;
    }
}
