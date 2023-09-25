package com.flowlong.bpm.solon.example.config;

import com.zaxxer.hikari.HikariDataSource;
import org.noear.solon.annotation.Bean;
import org.noear.solon.annotation.Configuration;
import org.noear.solon.annotation.Inject;

import javax.sql.DataSource;


@Configuration
public class DsConfig {
    @Bean(name = "flowlong")
    public DataSource ds(@Inject("${flowlong.datasource}") HikariDataSource ds) {
        return ds;
    }
}
