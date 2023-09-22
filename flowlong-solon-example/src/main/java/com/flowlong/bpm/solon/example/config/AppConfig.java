package com.flowlong.bpm.solon.example.config;

import org.noear.solon.annotation.Bean;
import org.noear.solon.annotation.Configuration;
import org.noear.solon.annotation.Inject;
import org.noear.solon.scheduling.scheduled.manager.IJobManager;
import org.noear.solon.scheduling.simple.JobManager;

import javax.sql.DataSource;


@Configuration
public class AppConfig {
    @Bean(name = "flowlong", typed = true)
    public DataSource ds(@Inject("${flowlong.datasource}") DataSource ds) {
        return ds;
    }

    @Bean
    public IJobManager jobManager(){
        return JobManager.getInstance();
    }
}
