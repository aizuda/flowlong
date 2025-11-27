/*
 * Copyright 2023-2025 Licensed under the Dual Licensing
 * website: https://aizuda.com
 */
package com.aizuda.bpm.spring.autoconfigure;

import com.aizuda.bpm.engine.dao.*;
import com.aizuda.bpm.mybatisplus.impl.*;
import com.aizuda.bpm.mybatisplus.mapper.*;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * FlowLong MybatisPlus 加载配置处理类
 *
 * <p>
 * <a href="https://aizuda.com">官网</a>尊重知识产权，不允许非法使用，后果自负
 * </p>
 *
 * @author hubin
 * @since 1.0
 */
@Configuration
@MapperScan("com.aizuda.bpm.mybatisplus.mapper")
public class MybatisPlusConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public FlwExtInstanceDao extInstanceDao(FlwExtInstanceMapper extInstanceMapper) {
        return new FlwExtInstanceDaoImpl(extInstanceMapper);
    }

    @Bean
    @ConditionalOnMissingBean
    public FlwHisInstanceDao hisInstanceDao(FlwHisInstanceMapper hisInstanceMapper) {
        return new FlwHisInstanceDaoImpl(hisInstanceMapper);
    }

    @Bean
    @ConditionalOnMissingBean
    public FlwHisTaskActorDao hisTaskActorDao(FlwHisTaskActorMapper hisInstanceMapper) {
        return new FlwHisTaskActorDaoImpl(hisInstanceMapper);
    }

    @Bean
    @ConditionalOnMissingBean
    public FlwHisTaskDao hisTaskDao(FlwHisTaskMapper hisTaskMapper) {
        return new FlwHisTaskDaoImpl(hisTaskMapper);
    }

    @Bean
    @ConditionalOnMissingBean
    public FlwInstanceDao instanceDao(FlwInstanceMapper instanceMapper) {
        return new FlwInstanceDaoImpl(instanceMapper);
    }

    @Bean
    @ConditionalOnMissingBean
    public FlwProcessDao processDao(FlwProcessMapper processMapper) {
        return new FlwProcessDaoImpl(processMapper);
    }

    @Bean
    @ConditionalOnMissingBean
    public FlwTaskActorDao taskActorDao(FlwTaskActorMapper taskActorMapper) {
        return new FlwTaskActorDaoImpl(taskActorMapper);
    }

    @Bean
    @ConditionalOnMissingBean
    public FlwTaskDao taskDao(FlwTaskMapper taskMapper) {
        return new FlwTaskDaoImpl(taskMapper);
    }

}
