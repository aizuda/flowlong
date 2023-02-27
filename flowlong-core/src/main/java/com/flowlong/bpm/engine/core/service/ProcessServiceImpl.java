/* Copyright 2023-2025 www.flowlong.com
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
import com.flowlong.bpm.engine.cache.FlowLongCache;
import com.flowlong.bpm.engine.entity.HisInstance;
import com.flowlong.bpm.engine.core.mapper.ProcessMapper;
import com.flowlong.bpm.engine.exception.FlowLongException;
import com.flowlong.bpm.engine.ProcessService;
import com.flowlong.bpm.engine.assist.Assert;
import com.flowlong.bpm.engine.assist.DateUtils;
import com.flowlong.bpm.engine.assist.StreamUtils;
import com.flowlong.bpm.engine.assist.StringUtils;
import com.flowlong.bpm.engine.cache.CacheManager;
import com.flowlong.bpm.engine.cache.CacheManagerAware;
import com.flowlong.bpm.engine.core.FlowLongContext;
import com.flowlong.bpm.engine.core.mapper.HisInstanceMapper;
import com.flowlong.bpm.engine.entity.Process;
import com.flowlong.bpm.engine.model.ProcessModel;
import com.flowlong.bpm.engine.parser.ModelParser;
import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;
import java.util.List;

/**
 * 流程定义业务类
 *
 * @author hubin
 * @since 1.0
 */
@Slf4j
public class ProcessServiceImpl extends AbstractService implements ProcessService, CacheManagerAware {
    private String DEFAULT_SEPARATOR = ".";
    /**
     * 流程定义对象cache名称
     */
    private String CACHE_ENTITY = "long.process.entity";
    /**
     * 流程id、name的cache名称
     */
    private String CACHE_NAME = "long.process.name";
    /**
     * cache manager
     */
    private CacheManager cacheManager;
    /**
     * 实体cache(key=name,value=entity对象)
     */
    private FlowLongCache<String, Process> entityCache;
    /**
     * 名称cache(key=id,value=name对象)
     */
    private FlowLongCache<String, String> nameCache;
    private FlowLongContext flowLongContext;
    private ProcessMapper processMapper;
    private HisInstanceMapper hisInstanceMapper;

    public ProcessServiceImpl(FlowLongContext flowLongContext, ProcessMapper processMapper, HisInstanceMapper hisInstanceMapper) {
        this.flowLongContext = flowLongContext;
        this.processMapper = processMapper;
        this.hisInstanceMapper = hisInstanceMapper;
    }

    @Override
    public void check(Process process, String idOrName) {
        Assert.notNull(process, "指定的流程定义[id/name=" + idOrName + "]不存在");
        if (process.getState() != null && process.getState() == 0) {
            throw new IllegalArgumentException("指定的流程定义[id/name=" + idOrName +
                    ",version=" + process.getVersion() + "]为非活动状态");
        }
    }

    /**
     * 保存process实体对象
     */
    @Override
    public void saveProcess(Process process) {
        processMapper.insert(process);
    }

    /**
     * 更新process的类别
     */
    @Override
    public void updateType(String id, String type) {
        Process process = new Process();
        process.setId(id);
        process.setType(type);
        processMapper.updateById(process);
        this.cache(process);
    }

    /**
     * 根据id获取process对象
     * 先通过cache获取，如果返回空，就从数据库读取并put
     */
    @Override
    public Process getProcessById(String id) {
        Assert.notEmpty(id);
        Process entity = null;
        String processName;
        FlowLongCache<String, String> nameCache = ensureAvailableNameCache();
        FlowLongCache<String, Process> entityCache = ensureAvailableEntityCache();
        if (nameCache != null && entityCache != null) {
            processName = nameCache.get(id);
            if (StringUtils.isNotEmpty(processName)) {
                entity = entityCache.get(processName);
            }
        }
        if (entity != null) {
            if (log.isDebugEnabled()) {
                log.debug("obtain process[id={}] from cache.", id);
            }
            return entity;
        }
        entity = processMapper.selectById(id);
        if (entity != null) {
            if (log.isDebugEnabled()) {
                log.debug("obtain process[id={}] from database.", id);
            }
            cache(entity);
        }
        return entity;
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
     * 根据name获取process对象
     * 先通过cache获取，如果返回空，就从数据库读取并put
     */
    @Override
    public Process getProcessByVersion(String name, Integer version) {
        Assert.notEmpty(name);
        Process process = null;
        if (version == null) {
            List<Process> processList = processMapper.selectList(Wrappers.<Process>lambdaQuery()
                    .eq(Process::getName, name).orderByDesc(Process::getVersion));
            if (CollectionUtils.isEmpty(processList)) {
                throw new FlowLongException("process [" + name + "] does not exist");
            }
            process = processList.get(0);
            cache(process);
        }
        if (null == process) {
            String processName = name + DEFAULT_SEPARATOR + 0;
            FlowLongCache<String, Process> entityCache = ensureAvailableEntityCache();
            if (entityCache != null) {
                process = entityCache.get(processName);
            }
        }
        return process;
    }

    /**
     * 根据流程定义xml的输入流解析为字节数组，保存至数据库中，并且put到缓存中
     *
     * @param input 定义输入流
     */
    @Override
    public String deploy(InputStream input) {
        return deploy(input, null);
    }

    /**
     * 根据流程定义xml的输入流解析为字节数组，保存至数据库中，并且put到缓存中
     *
     * @param input   定义输入流
     * @param creator 创建人
     */
    @Override
    public String deploy(InputStream input, String creator) {
        Assert.notNull(input);
        try {
            byte[] bytes = StreamUtils.readBytes(input);
            ProcessModel model = ModelParser.parse(bytes, flowLongContext);
            // 查询流程信息
            List<Process> processList = processMapper.selectList(Wrappers.<Process>lambdaQuery().select(Process::getVersion)
                    .eq(Process::getName, model.getName()).orderByDesc(Process::getVersion));
            if (CollectionUtils.isEmpty(processList)) {
                throw new FlowLongException("process [" + model.getName() + "] does not exist");
            }
            Integer version = processList.get(0).getVersion();
            Process entity = new Process();
            entity.setId(StringUtils.getPrimaryKey());
            if (version == null || version < 0) {
                entity.setVersion(0);
            } else {
                entity.setVersion(version + 1);
            }
            entity.setState(STATE_ACTIVE);
            entity.setModel(model);
            entity.setBytes(bytes);
            entity.setCreateTime(DateUtils.getTime());
            entity.setCreator(creator);
            saveProcess(entity);
            cache(entity);
            return entity.getId();
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
    public void redeploy(String id, InputStream input) {
        Assert.notNull(input);
        Process entity = processMapper.selectById(id);
        Assert.notNull(entity);
        try {
            byte[] bytes = StreamUtils.readBytes(input);
            ProcessModel model = ModelParser.parse(bytes, flowLongContext);
            String oldProcessName = entity.getName();
            entity.setModel(model);
            entity.setBytes(bytes);
            processMapper.updateById(entity);
            if (!oldProcessName.equalsIgnoreCase(entity.getName())) {
                FlowLongCache<String, Process> entityCache = ensureAvailableEntityCache();
                if (entityCache != null) {
                    entityCache.remove(oldProcessName + DEFAULT_SEPARATOR + entity.getVersion());
                }
            }
            cache(entity);
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage());
            throw new FlowLongException(e.getMessage(), e.getCause());
        }
    }

    /**
     * 根据processId卸载流程
     */
    @Override
    public void undeploy(String id) {
        Process process = new Process();
        process.setId(id);
        process.setState(STATE_FINISH);
        if (processMapper.updateById(process) > 0) {
            cache(process);
        }
    }

    /**
     * 级联删除指定流程定义的所有数据
     */
    @Override
    public void cascadeRemove(String id) {
        Process process = processMapper.selectById(id);
        List<HisInstance> hisInstances = hisInstanceMapper.selectList(Wrappers.<HisInstance>lambdaQuery().eq(HisInstance::getProcessId, id));
        for (HisInstance hisInstance : hisInstances) {
            flowLongContext.getRuntimeService().cascadeRemove(hisInstance.getId());
        }
        if (processMapper.deleteById(id) > 0) {
            clear(process);
        }
    }

    /**
     * 缓存实体
     *
     * @param entity 流程定义对象
     */
    private void cache(Process entity) {
        FlowLongCache<String, String> nameCache = ensureAvailableNameCache();
        FlowLongCache<String, Process> entityCache = ensureAvailableEntityCache();
        if (entity.getModel() == null && entity.getDBContent() != null) {
            entity.setModel(ModelParser.parse(entity.getDBContent(), flowLongContext));
        }
        String processName = entity.getName() + DEFAULT_SEPARATOR + entity.getVersion();
        if (nameCache != null && entityCache != null) {
            if (log.isDebugEnabled()) {
                log.debug("cache process id is[{}],name is[{}]", entity.getId(), processName);
            }
            entityCache.put(processName, entity);
            nameCache.put(entity.getId(), processName);
        } else {
            if (log.isDebugEnabled()) {
                log.debug("no cache implementation class");
            }
        }
    }

    /**
     * 清除实体
     *
     * @param entity 流程定义对象
     */
    private void clear(Process entity) {
        FlowLongCache<String, String> nameCache = ensureAvailableNameCache();
        FlowLongCache<String, Process> entityCache = ensureAvailableEntityCache();
        String processName = entity.getName() + DEFAULT_SEPARATOR + entity.getVersion();
        if (nameCache != null && entityCache != null) {
            nameCache.remove(entity.getId());
            entityCache.remove(processName);
        }
    }

    @Override
    public void setCacheManager(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    private FlowLongCache<String, Process> ensureAvailableEntityCache() {
        FlowLongCache<String, Process> entityCache = ensureEntityCache();
        if (entityCache == null && this.cacheManager != null) {
            entityCache = this.cacheManager.getCache(CACHE_ENTITY);
        }
        return entityCache;
    }

    private FlowLongCache<String, String> ensureAvailableNameCache() {
        FlowLongCache<String, String> nameCache = ensureNameCache();
        if (nameCache == null && this.cacheManager != null) {
            nameCache = this.cacheManager.getCache(CACHE_NAME);
        }
        return nameCache;
    }

    public FlowLongCache<String, Process> ensureEntityCache() {
        return entityCache;
    }

    public void setEntityCache(FlowLongCache<String, Process> entityCache) {
        this.entityCache = entityCache;
    }

    public FlowLongCache<String, String> ensureNameCache() {
        return nameCache;
    }

    public void setNameCache(FlowLongCache<String, String> nameCache) {
        this.nameCache = nameCache;
    }
}
