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
package com.flowlong.bpm.engine.core.service;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.flowlong.bpm.engine.ProcessService;
import com.flowlong.bpm.engine.RuntimeService;
import com.flowlong.bpm.engine.assist.Assert;
import com.flowlong.bpm.engine.assist.StreamUtils;
import com.flowlong.bpm.engine.core.enums.FlowState;
import com.flowlong.bpm.engine.core.mapper.ProcessMapper;
import com.flowlong.bpm.engine.entity.Process;
import com.flowlong.bpm.engine.entity.Surrogate;
import com.flowlong.bpm.engine.exception.FlowLongException;
import com.flowlong.bpm.engine.model.ProcessModel;
import com.flowlong.bpm.engine.parser.ModelParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.Date;
import java.util.List;

/**
 * 流程定义业务类
 *
 * <p>
 * 尊重知识产权，CV 请保留版权，爱组搭 http://aizuda.com 出品
 * </p>
 *
 * @author hubin
 * @since 1.0
 */
@Slf4j
@Service
public class ProcessServiceImpl implements ProcessService {
    private String DEFAULT_SEPARATOR = ".";
    /**
     * 流程定义对象cache名称
     */
    private String CACHE_ENTITY = "long.process.entity";
    /**
     * 流程id、name的cache名称
     */
    private String CACHE_NAME = "long.process.name";
    private ProcessMapper processMapper;
    private RuntimeService runtimeService;

    public ProcessServiceImpl(RuntimeService runtimeService, ProcessMapper processMapper) {
        this.processMapper = processMapper;
        this.runtimeService = runtimeService;
    }

    @Override
    public void check(Process process, Long id) {
        Assert.notNull(process, "指定的流程定义[id=" + id + "]不存在");
        if (process.getState() != null && process.getState() == 0) {
            throw new IllegalArgumentException("指定的流程定义[id=" + id + ",version=" + process.getVersion() + "]为非活动状态");
        }
    }

    /**
     * 更新process的类别
     */
    @Override
    public void updateType(Long id, String type) {
        Process process = new Process();
        process.setId(id);
        process.setType(type);
        processMapper.updateById(process);
    }

    /**
     * 根据id获取process对象
     * 先通过cache获取，如果返回空，就从数据库读取并put
     */
    @Override
    public Process getProcessById(Long id) {
        return processMapper.selectById(id);
    }

    /**
     * 根据name获取process对象
     * 先通过cache获取，如果返回空，就从数据库读取并put
     */
    @Override
    public Process getProcessByName(String name) {
        return getProcessByVersion(name, null);
    }

    /**
     * 根据流程名称或版本号查找流程定义对象
     *
     * @param name    流程定义名称
     * @param version 版本号
     * @return {@link Process}
     */
    @Override
    public Process getProcessByVersion(String name, Integer version) {
        Assert.notEmpty(name);
        List<Process> processList = processMapper.selectList(Wrappers.<Process>lambdaQuery().eq(Process::getName, name).eq(null != version, Process::getVersion, version).orderByDesc(Process::getVersion));
        if (CollectionUtils.isEmpty(processList)) {
            throw new FlowLongException("process [" + name + "] does not exist");
        }
        return processList.get(0);
    }

    /**
     * 根据流程定义xml的输入流解析为字节数组，保存至数据库中，并且put到缓存中
     *
     * @param input 定义输入流
     */
    @Override
    public Long deploy(InputStream input, boolean repeat) {
        return deploy(input, null, repeat);
    }

    /**
     * 根据InputStream输入流，部署流程定义
     *
     * @param input    流程定义输入流
     * @param createBy 创建人
     * @param repeat   是否重复部署 true 存在版本+1新增一条记录 false 存在流程直接返回
     * @return
     */
    @Override
    public Long deploy(InputStream input, String createBy, boolean repeat) {
        Assert.notNull(input);
        try {
            final byte[] bytes = StreamUtils.readBytes(input);
            ProcessModel processModel = ModelParser.parse(bytes);
            /**
             * 查询流程信息获取最后版本号
             */
            List<Process> processList = processMapper.selectList(Wrappers.<Process>lambdaQuery().select(Process::getId, Process::getVersion).eq(Process::getName, processModel.getName()).orderByDesc(Process::getVersion));
            Integer version = 0;
            if (CollectionUtils.isNotEmpty(processList)) {
                Process process = processList.get(0);
                if (!repeat) {
                    return process.getId();
                }
                version = process.getVersion();
            }
            /**
             * 当前版本 +1 添加一条新的流程记录
             */
            Process process = new Process();
            process.setVersion(version + 1);
            process.setFlowState(FlowState.active);
            process.setName(processModel.getName());
            process.setDisplayName(processModel.getDisplayName());
            process.setInstanceUrl(processModel.getInstanceUrl());
            process.setContent(bytes);
            process.setCreateBy(createBy);
            process.setCreateTime(new Date());
            Assert.isZero(processMapper.insert(process), "Failed to save the deployment process");
            return process.getId();
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new FlowLongException(e);
        }
    }

    /**
     * 根据流程定义id、xml的输入流解析为字节数组，保存至数据库中，并且重新put到缓存中
     *
     * @param input 定义输入流
     */
    @Override
    public void redeploy(Long id, InputStream input) {
        Assert.notNull(input);
        Process process = processMapper.selectById(id);
        Assert.notNull(process);
        try {
            byte[] bytes = StreamUtils.readBytes(input);
            process.setContent(bytes);
            processMapper.updateById(process);
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage());
            throw new FlowLongException(e.getMessage(), e.getCause());
        }
    }

    /**
     * 卸载指定的定义流程，更新为未启用状态
     *
     * @param id 流程定义ID
     * @return
     */
    @Override
    public boolean undeploy(Long id) {
        Process process = new Process();
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
