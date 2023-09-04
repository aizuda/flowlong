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
package com.flowlong.bpm.engine;

import com.flowlong.bpm.engine.assist.StreamUtils;
import com.flowlong.bpm.engine.core.FlowCreator;
import com.flowlong.bpm.engine.entity.Process;

import java.io.InputStream;

/**
 * 流程定义业务类
 *
 * <p>
 * 尊重知识产权，CV 请保留版权，爱组搭 http://aizuda.com 出品，不允许非法使用，后果自负
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
    Process getProcessById(Long id);

    /**
     * 根据流程名称或版本号查找流程定义对象
     *
     * @param name    流程定义名称
     * @param version 版本号
     * @return {@link Process}
     */
    Process getProcessByVersion(String name, Integer version);

    default Process getProcessByName(String name) {
        return getProcessByVersion(name, null);
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
        return this.deploy(StreamUtils.getResourceAsStream(resourceName), flowCreator, repeat);
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
        return StreamUtils.readBytes(input, t -> this.deploy(t, flowCreator, repeat));
    }

    /**
     * 根据 流程定义jsonString 部署流程定义
     *
     * @param jsonString  流程定义json字符串
     * @param flowCreator 流程任务部署者
     * @param repeat      是否重复部署 true 存在版本+1新增一条记录 false 存在流程直接返回
     * @return 流程定义ID
     */
    Long deploy(String jsonString, FlowCreator flowCreator, boolean repeat);

    /**
     * 根据InputStream输入流，部署流程定义
     *
     * @param id    流程定义id
     * @param input 流程定义输入流
     */
    default boolean redeploy(Long id, InputStream input) {
        return StreamUtils.readBytes(input, t -> this.redeploy(id, t));
    }

    /**
     * 根据 流程定义jsonString 重新部署流程定义
     *
     * @param id         流程定义id
     * @param jsonString 流程定义json字符串
     */
    boolean redeploy(Long id, String jsonString);

    /**
     * 卸载指定的定义流程，更新为未启用状态
     *
     * @param id 流程定义ID
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
