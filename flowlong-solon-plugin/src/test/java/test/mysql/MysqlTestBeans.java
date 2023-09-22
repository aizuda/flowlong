package test.mysql;

import org.noear.solon.annotation.Bean;
import org.noear.solon.annotation.Configuration;
import org.noear.solon.annotation.Inject;

import javax.sql.DataSource;

/**
 * @author noear 2023/9/22 created
 */
@Configuration
public class MysqlTestBeans {
    @Bean
    public DataSource ds(@Inject("${classpath:jdbc-mysql.properties}") DataSource ds){
        return ds;
    }
}
