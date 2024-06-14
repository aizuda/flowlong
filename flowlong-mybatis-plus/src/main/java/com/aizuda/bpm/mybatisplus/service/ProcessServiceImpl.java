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
import java.util.Objects;
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
     * @param processId   流程定义ID
     * @param jsonString  流程定义json字符串
     * @param flowCreator 流程任务部署者
     * @param repeat      是否重复部署 true 存在版本+1新增一条记录 false 存在流程直接返回
     * @param processSave 保存流程定义消费者函数
     * @return 流程ID
     */
    @Override
    public Long deploy(Long processId, String jsonString, FlowCreator flowCreator, boolean repeat, Consumer<FlwProcess> processSave) {
        Assert.isNull(jsonString);
        try {
            ProcessModel processModel = FlowLongContext.parseProcessModel(jsonString, null, false);
            FlwProcess dbProcess = null;
            if (null == processId) {
                /*
                 * 查询流程信息获取最后版本号
                 */
                List<FlwProcess> processList = processMapper.selectList(Wrappers.<FlwProcess>lambdaQuery()
                        .eq(FlwProcess::getProcessKey, processModel.getKey())
                        .eq(StringUtils.isNotBlank(flowCreator.getTenantId()), FlwProcess::getTenantId, flowCreator.getTenantId())
                        .orderByDesc(FlwProcess::getProcessVersion));
                if (ObjectUtils.isNotEmpty(processList)) {
                    dbProcess = processList.get(0);
                }
            } else {
                dbProcess = processMapper.selectById(processId);
            }

            int processVersion = 1;
            if (null != dbProcess) {

                // 不允许重复部署，直接返回当前流程定义ID
                if (!repeat) {
                    return dbProcess.getId();
                }

                /*
                 * 设置为历史流程
                 */
                int rows;
                FlwProcess his = new FlwProcess();
                his.setFlowState(FlowState.history);
                if (Objects.equals(processModel.getKey(), dbProcess.getProcessKey())) {
                    // 流程定义key未发生改变直接修改为历史即可
                    his.setId(dbProcess.getId());
                    rows = processMapper.updateById(his);
                } else {
                    // 流程定义KEY被修改历史KEY修改为最新KEY并重置为历史状态
                    his.setProcessKey(processModel.getKey());
                    rows = processMapper.update(his, Wrappers.<FlwProcess>lambdaQuery().eq(FlwProcess::getProcessKey, dbProcess.getProcessKey()));
                }
                Assert.isZero(rows, "Set as historical process failed");
                processVersion = dbProcess.nextProcessVersion();
            }

            /*
             * 添加一条新的流程记录
             */
            FlwProcess process = FlwProcess.of(flowCreator, processModel, processVersion, jsonString);
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
