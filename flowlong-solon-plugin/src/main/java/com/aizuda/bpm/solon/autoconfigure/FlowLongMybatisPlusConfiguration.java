package com.aizuda.bpm.solon.autoconfigure;

import com.aizuda.bpm.engine.dao.*;
import com.aizuda.bpm.mybatisplus.impl.*;
import com.aizuda.bpm.mybatisplus.mapper.*;
import org.noear.solon.annotation.Bean;
import org.noear.solon.annotation.Condition;
import org.noear.solon.annotation.Configuration;

@Configuration
public class FlowLongMybatisPlusConfiguration {

    @Bean
    @Condition(onMissingBean = FlwExtInstanceDao.class)
    public FlwExtInstanceDao extInstanceDao(FlwExtInstanceMapper extInstanceMapper) {
        return new FlwExtInstanceDaoImpl(extInstanceMapper);
    }

    @Bean
    @Condition(onMissingBean = FlwHisInstanceDao.class)
    public FlwHisInstanceDao hisInstanceDao(FlwHisInstanceMapper hisInstanceMapper) {
        return new FlwHisInstanceDaoImpl(hisInstanceMapper);
    }

    @Bean
    @Condition(onMissingBean = FlwHisTaskActorDao.class)
    public FlwHisTaskActorDao hisTaskActorDao(FlwHisTaskActorMapper hisInstanceMapper) {
        return new FlwHisTaskActorDaoImpl(hisInstanceMapper);
    }

    @Bean
    @Condition(onMissingBean = FlwHisTaskDao.class)
    public FlwHisTaskDao hisTaskDao(FlwHisTaskMapper hisTaskMapper) {
        return new FlwHisTaskDaoImpl(hisTaskMapper);
    }

    @Bean
    @Condition(onMissingBean = FlwInstanceDao.class)
    public FlwInstanceDao instanceDao(FlwInstanceMapper instanceMapper) {
        return new FlwInstanceDaoImpl(instanceMapper);
    }

    @Bean
    @Condition(onMissingBean = FlwProcessDao.class)
    public FlwProcessDao processDao(FlwProcessMapper processMapper) {
        return new FlwProcessDaoImpl(processMapper);
    }

    @Bean
    @Condition(onMissingBean = FlwTaskActorDao.class)
    public FlwTaskActorDao taskActorDao(FlwTaskActorMapper taskActorMapper) {
        return new FlwTaskActorDaoImpl(taskActorMapper);
    }

    @Bean
    @Condition(onMissingBean = FlwTaskDao.class)
    public FlwTaskDao taskDao(FlwTaskMapper taskMapper) {
        return new FlwTaskDaoImpl(taskMapper);
    }

}
