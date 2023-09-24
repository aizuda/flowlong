package com.flowlong.bpm.solon.example.config;

import com.zaxxer.hikari.HikariDataSource;
import org.noear.solon.annotation.Bean;
import org.noear.solon.annotation.Configuration;
import org.noear.solon.annotation.Inject;
import org.noear.solon.scheduling.scheduled.manager.IJobManager;
import org.noear.solon.scheduling.simple.JobManager;

import javax.sql.DataSource;


@Configuration
public class AppConfig {
    @Bean(name = "flowlong")
    public DataSource ds(@Inject("${flowlong.datasource}") HikariDataSource ds) {
        return ds;
    }

    @Bean
    public IJobManager jobManager(){
        return JobManager.getInstance();
    }
}
