package com.aizuda.bpm.solon.autoconfigure;

import com.aizuda.bpm.engine.dao.FlwExtInstanceDao;
import com.aizuda.bpm.engine.dao.FlwHisInstanceDao;
import com.aizuda.bpm.engine.dao.FlwHisTaskActorDao;
import com.aizuda.bpm.engine.dao.FlwHisTaskDao;
import com.aizuda.bpm.engine.dao.FlwInstanceDao;
import com.aizuda.bpm.engine.dao.FlwProcessDao;
import com.aizuda.bpm.engine.dao.FlwTaskActorDao;
import com.aizuda.bpm.engine.dao.FlwTaskDao;
import com.aizuda.bpm.mybatisplus.impl.FlwExtInstanceDaoImpl;
import com.aizuda.bpm.mybatisplus.impl.FlwHisInstanceDaoImpl;
import com.aizuda.bpm.mybatisplus.impl.FlwHisTaskActorDaoImpl;
import com.aizuda.bpm.mybatisplus.impl.FlwHisTaskDaoImpl;
import com.aizuda.bpm.mybatisplus.impl.FlwInstanceDaoImpl;
import com.aizuda.bpm.mybatisplus.impl.FlwProcessDaoImpl;
import com.aizuda.bpm.mybatisplus.impl.FlwTaskActorDaoImpl;
import com.aizuda.bpm.mybatisplus.impl.FlwTaskDaoImpl;
import com.aizuda.bpm.mybatisplus.mapper.FlwExtInstanceMapper;
import com.aizuda.bpm.mybatisplus.mapper.FlwHisInstanceMapper;
import com.aizuda.bpm.mybatisplus.mapper.FlwHisTaskActorMapper;
import com.aizuda.bpm.mybatisplus.mapper.FlwHisTaskMapper;
import com.aizuda.bpm.mybatisplus.mapper.FlwInstanceMapper;
import com.aizuda.bpm.mybatisplus.mapper.FlwProcessMapper;
import com.aizuda.bpm.mybatisplus.mapper.FlwTaskActorMapper;
import com.aizuda.bpm.mybatisplus.mapper.FlwTaskMapper;
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
