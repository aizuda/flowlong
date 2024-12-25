/*
 * Copyright 2023-2025 Licensed under the Dual Licensing
 * website: https://aizuda.com
 */
package com.aizuda.bpm.engine;

import com.aizuda.bpm.engine.assist.StreamUtils;
import com.aizuda.bpm.engine.core.FlowCreator;
import com.aizuda.bpm.engine.entity.FlwProcess;

import java.io.InputStream;
import java.util.function.Consumer;

/**
 * 流程定义业务类
 *
 * <p>
 * <a href="https://aizuda.com">官网</a>尊重知识产权，不允许非法使用，后果自负
 * </p>
 *
 * @author hubin
 * @since 1.0
 */
public interface ProcessService {

    /**
     * 更新流程定义的类别
     *
     * @param id   流程定义id
     * @param type 类别
     */
    void updateType(Long id, String type);

    /**
     * 根据主键ID获取流程定义对象
     *
     * @param id 流程定义id
     * @return Process 流程定义对象
     */
    FlwProcess getProcessById(Long id);

    /**
     * 根据流程定义KEY或版本号查找流程定义对象
     *
     * @param tenantId   租户ID
     * @param processKey 流程定义key
     * @param version    版本号
     * @return {@link FlwProcess}
     */
    FlwProcess getProcessByVersion(String tenantId, String processKey, Integer version);

    /**
     * 根据流程定义KEY查找流程定义对象
     *
     * @param tenantId   租户ID
     * @param processKey 流程定义key
     * @return {@link FlwProcess}
     */
    default FlwProcess getProcessByKey(String tenantId, String processKey) {
        return getProcessByVersion(tenantId, processKey, null);
    }

    /**
     * 根据本地 resource 资源名称部署流程
     *
     * @param resourceName 资源名称
     * @param flowCreator  流程任务部署者
     * @param repeat       是否重复部署 true 存在版本+1新增一条记录 false 存在流程直接返回
     * @return 流程定义ID
     */
    default Long deployByResource(String resourceName, FlowCreator flowCreator, boolean repeat) {
        return this.deployByResource(resourceName, flowCreator, repeat, null);
    }

    /**
     * 根据本地 resource 资源名称部署流程
     *
     * @param resourceName 资源名称
     * @param flowCreator  流程任务部署者
     * @param repeat       是否重复部署 true 存在版本+1新增一条记录 false 存在流程直接返回
     * @param processSave  保存流程定义消费者函数
     * @return 流程定义ID
     */
    default Long deployByResource(String resourceName, FlowCreator flowCreator, boolean repeat, Consumer<FlwProcess> processSave) {
        return this.deploy(StreamUtils.getResourceAsStream(resourceName), flowCreator, repeat, processSave);
    }

    /**
     * 根据InputStream输入流，部署流程定义
     *
     * @param input       流程定义输入流
     * @param flowCreator 流程任务部署者
     * @param repeat      是否重复部署 true 存在版本+1新增一条记录 false 存在流程直接返回
     * @return 流程定义ID
     */
    default Long deploy(InputStream input, FlowCreator flowCreator, boolean repeat) {
        return deploy(input, flowCreator, repeat, null);
    }

    /**
     * 根据InputStream输入流，部署流程定义
     *
     * @param input       流程定义输入流
     * @param flowCreator 流程任务部署者
     * @param repeat      是否重复部署 true 存在版本+1新增一条记录 false 存在流程直接返回
     * @param processSave 保存流程定义消费者函数
     * @return 流程定义ID
     */
    default Long deploy(InputStream input, FlowCreator flowCreator, boolean repeat, Consumer<FlwProcess> processSave) {
        return StreamUtils.readBytes(input, t -> this.deploy(null, t, flowCreator, repeat, processSave));
    }

    /**
     * 根据 流程定义jsonString 部署流程定义
     *
     * @param processId   流程定义ID
     * @param jsonString  流程定义json字符串
     * @param flowCreator 流程任务部署者
     * @param repeat      是否重复部署 true 存在版本+1新增一条记录 false 存在流程直接返回
     * @param processSave 保存流程定义消费者函数
     * @return 流程定义ID
     */
    Long deploy(Long processId, String jsonString, FlowCreator flowCreator, boolean repeat, Consumer<FlwProcess> processSave);

    /**
     * 卸载指定的定义流程，更新为未启用状态
     *
     * @param id 流程定义ID
     * @return true 成功 false 失败
     */
    boolean undeploy(Long id);

    /**
     * 谨慎使用！！！不可恢复，
     * 级联删除指定流程定义的所有数据
     *
     * @param id 流程定义ID
     */
    void cascadeRemove(Long id);
}
