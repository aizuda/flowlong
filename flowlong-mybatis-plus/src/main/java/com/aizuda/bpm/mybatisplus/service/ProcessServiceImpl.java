/*
 * Copyright 2023-2025 Licensed under the AGPL License
 */
package com.aizuda.bpm.mybatisplus.service;

import com.aizuda.bpm.engine.ProcessService;
import com.aizuda.bpm.engine.RuntimeService;
import com.aizuda.bpm.engine.assist.Assert;
import com.aizuda.bpm.engine.assist.ObjectUtils;
import com.aizuda.bpm.engine.core.FlowCreator;
import com.aizuda.bpm.engine.core.FlowLongContext;
import com.aizuda.bpm.engine.core.enums.FlowState;
import com.aizuda.bpm.engine.entity.FlwProcess;
import com.aizuda.bpm.engine.model.ProcessModel;
import com.aizuda.bpm.mybatisplus.mapper.FlwProcessMapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.function.Consumer;

/**
 * 流程定义业务类
 *
 * <p>
 * 尊重知识产权，不允许非法使用，后果自负
 * </p>
 *
 * @author hubin
 * @since 1.0
 */
@Slf4j
public class ProcessServiceImpl implements ProcessService {
    private final FlwProcessMapper processMapper;
    private final RuntimeService runtimeService;

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
        FlwProcess process = processMapper.selectById(id);
        Assert.isTrue(ObjectUtils.isEmpty(process), "process id [" + id + "] does not exist");
        return process;
    }

    /**
     * 根据流程名称或版本号查找流程定义对象
     *
     * @param processKey 流程定义key
     * @param version    版本号
     * @return {@link Process}
     */
    @Override
    public FlwProcess getProcessByVersion(String processKey, Integer version) {
        Assert.isEmpty(processKey);
        List<FlwProcess> processList = processMapper.selectList(Wrappers.<FlwProcess>lambdaQuery().eq(FlwProcess::getProcessKey, processKey)
                .eq(null != version, FlwProcess::getProcessVersion, version)
                .orderByDesc(FlwProcess::getProcessVersion));
        Assert.isTrue(ObjectUtils.isEmpty(processList), "process key [" + processKey + "] does not exist");
        return processList.get(0);
    }

    /**
     * 根据流程定义json字符串，部署流程定义
     *
     * @param jsonString  流程定义json字符串
     * @param flowCreator 流程任务部署者
     * @param repeat      是否重复部署 true 存在版本+1新增一条记录 false 存在流程直接返回
     * @param processSave 保存流程定义消费者函数
     * @return 流程ID
     */
    @Override
    public Long deploy(String jsonString, FlowCreator flowCreator, boolean repeat, Consumer<FlwProcess> processSave) {
        Assert.isNull(jsonString);
        try {
            ProcessModel processModel = FlowLongContext.parseProcessModel(jsonString, null, false);
            /*
             * 查询流程信息获取最后版本号
             */
            List<FlwProcess> processList = processMapper.selectList(Wrappers.<FlwProcess>lambdaQuery()
                    .eq(FlwProcess::getProcessKey, processModel.getKey())
                    .eq(StringUtils.isNotBlank(flowCreator.getTenantId()), FlwProcess::getTenantId, flowCreator.getTenantId())
                    .orderByDesc(FlwProcess::getProcessVersion));
            if (ObjectUtils.isNotEmpty(processList)) {
                FlwProcess process = processList.get(0);
                Long processId = process.getId();
                if (!repeat) {
                    return processId;
                }
                // 更新当前版本 +1
                Assert.isFalse(this.redeploy(processId, jsonString, process.nextProcessVersion()), "Redeploy failed");

                // 保留历史版本
                process.setId(null);
                process.setFlowState(FlowState.history);
                if (null != processSave) {
                    processSave.accept(process);
                }
                processMapper.insert(process);
                return processId;
            }
            /*
             * 添加一条新的流程记录
             */
            FlwProcess process = FlwProcess.of(flowCreator, processModel, jsonString);
            if (null != processSave) {
                processSave.accept(process);
            }
            Assert.isZero(processMapper.insert(process), "Failed to save the deployment process");
            return process.getId();
        } catch (Exception e) {
            log.error(e.getMessage());
            throw Assert.throwable(e);
        }
    }

    /**
     * 根据 流程定义jsonString 重新部署流程定义
     *
     * @param id             流程定义id
     * @param jsonString     流程定义json字符串
     * @param processVersion 流程版本
     * @return true 成功 false 失败
     */
    public boolean redeploy(Long id, String jsonString, int processVersion) {
        FlwProcess process = processMapper.selectById(id);
        Assert.isNull(process);
        ProcessModel processModel = FlowLongContext.parseProcessModel(jsonString, process.modelCacheKey(), true);
        process.setProcessVersion(processVersion);
        process.setProcessKey(processModel.getKey());
        process.setProcessName(processModel.getName());
        process.setInstanceUrl(processModel.getInstanceUrl());
        return processMapper.updateById(process.formatModelContent(jsonString)) > 0;
    }

    /**
     * 卸载指定的定义流程，更新为未启用状态
     *
     * @param id 流程定义ID
     * @return true 成功 false 失败
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
