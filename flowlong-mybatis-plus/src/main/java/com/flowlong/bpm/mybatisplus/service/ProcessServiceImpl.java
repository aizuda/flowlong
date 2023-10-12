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
package com.flowlong.bpm.mybatisplus.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.flowlong.bpm.engine.ProcessService;
import com.flowlong.bpm.engine.RuntimeService;
import com.flowlong.bpm.engine.assist.Assert;
import com.flowlong.bpm.engine.assist.DateUtils;
import com.flowlong.bpm.engine.assist.ObjectUtils;
import com.flowlong.bpm.engine.core.FlowCreator;
import com.flowlong.bpm.engine.core.enums.FlowState;
import com.flowlong.bpm.engine.entity.FlwProcess;
import com.flowlong.bpm.engine.exception.FlowLongException;
import com.flowlong.bpm.engine.model.ProcessModel;
import com.flowlong.bpm.mybatisplus.mapper.FlwProcessMapper;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

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
@Slf4j
public class ProcessServiceImpl implements ProcessService {
    private FlwProcessMapper processMapper;
    private RuntimeService runtimeService;

    public ProcessServiceImpl(RuntimeService runtimeService, FlwProcessMapper processMapper) {
        this.processMapper = processMapper;
        this.runtimeService = runtimeService;
    }

    /**
     * 更新process的类别
     */
    @Override
    public void updateType(Long id, String processType) {
        FlwProcess process = new FlwProcess();
        process.setId(id);
        process.setProcessType(processType);
        processMapper.updateById(process);
    }

    /**
     * 根据id获取process对象
     * 先通过cache获取，如果返回空，就从数据库读取并put
     */
    @Override
    public FlwProcess getProcessById(Long id) {
        return processMapper.selectById(id);
    }

    /**
     * 根据流程名称或版本号查找流程定义对象
     *
     * @param name    流程定义名称
     * @param version 版本号
     * @return {@link Process}
     */
    @Override
    public FlwProcess getProcessByVersion(String name, Integer version) {
        Assert.notEmpty(name);
        List<FlwProcess> processList = processMapper.selectList(Wrappers.<FlwProcess>lambdaQuery().eq(FlwProcess::getProcessName, name)
                .eq(null != version, FlwProcess::getProcessVersion, version)
                .orderByDesc(FlwProcess::getProcessVersion));
        Assert.isTrue(ObjectUtils.isEmpty(processList), "process [" + name + "] does not exist");
        return processList.get(0);
    }

    /**
     * 根据流程定义json字符串，部署流程定义
     *
     * @param jsonString  流程定义json字符串
     * @param flowCreator 流程任务部署者
     * @param repeat      是否重复部署 true 存在版本+1新增一条记录 false 存在流程直接返回
     * @return
     */
    @Override
    public Long deploy(String jsonString, FlowCreator flowCreator, boolean repeat) {
        Assert.isNull(jsonString);
        try {
            ProcessModel processModel = ProcessModel.parse(jsonString, null);
            /**
             * 查询流程信息获取最后版本号
             */
            List<FlwProcess> processList = processMapper.selectList(Wrappers.<FlwProcess>lambdaQuery()
                    .select(FlwProcess::getId, FlwProcess::getProcessVersion)
                    .eq(FlwProcess::getProcessName, processModel.getName())
                    .orderByDesc(FlwProcess::getProcessVersion));
            Integer version = 0;
            if (ObjectUtils.isNotEmpty(processList)) {
                FlwProcess process = processList.get(0);
                if (!repeat) {
                    return process.getId();
                }
                version = process.getProcessVersion();
            }
            /**
             * 当前版本 +1 添加一条新的流程记录
             */
            FlwProcess process = new FlwProcess();
            process.setProcessVersion(version + 1);
            process.setFlowState(FlowState.active);
            process.setProcessName(processModel.getName());
            process.setDisplayName(processModel.getName());
            process.setInstanceUrl(processModel.getInstanceUrl());
            process.setUseScope(0);
            process.setModelContent(jsonString);
            process.setCreateId(flowCreator.getCreateId());
            process.setCreateBy(flowCreator.getCreateBy());
            process.setCreateTime(DateUtils.getCurrentDate());
            Assert.isZero(processMapper.insert(process), "Failed to save the deployment process");
            return process.getId();
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new FlowLongException(e);
        }
    }

    /**
     * 根据 流程定义jsonString 重新部署流程定义
     *
     * @param id         流程定义id
     * @param jsonString 流程定义json字符串
     * @return
     */
    @Override
    public boolean redeploy(Long id, String jsonString) {
        FlwProcess process = processMapper.selectById(id);
        Assert.isNull(process);
        ProcessModel processModel = ProcessModel.parse(jsonString, id);
        process.setProcessName(processModel.getName());
        process.setDisplayName(processModel.getName());
        process.setInstanceUrl(processModel.getInstanceUrl());
        process.setModelContent(jsonString);
        return processMapper.updateById(process) > 0;
    }

    /**
     * 卸载指定的定义流程，更新为未启用状态
     *
     * @param id 流程定义ID
     * @return
     */
    @Override
    public boolean undeploy(Long id) {
        FlwProcess process = new FlwProcess();
        process.setId(id);
        process.setFlowState(FlowState.inactive);
        return processMapper.updateById(process) > 0;
    }

    /**
     * 级联删除指定流程定义的所有数据
     */
    @Override
    public void cascadeRemove(Long id) {
        // 删除与流程相关的实例
        runtimeService.cascadeRemoveByProcessId(id);

        // 删除部署流程流程信息
        processMapper.deleteById(id);
    }
}
