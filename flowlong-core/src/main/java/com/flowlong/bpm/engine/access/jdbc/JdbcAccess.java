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
package com.flowlong.bpm.engine.access.jdbc;

import com.flowlong.bpm.engine.DBAccess;
import com.flowlong.bpm.engine.FlowLongException;
import com.flowlong.bpm.engine.access.AbstractDBAccess;
import com.flowlong.bpm.engine.access.transaction.DataSourceTransactionInterceptor;
import com.flowlong.bpm.engine.entity.Process;
import com.flowlong.bpm.engine.assist.ClassUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.ScalarHandler;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * JDBC方式的数据库访问
 * 在无事务控制的情况下，使用cglib的拦截器+ThreadLocale控制
 *
 * @author hubin
 * @see DataSourceTransactionInterceptor
 * @since 1.0
 */
@Slf4j
public class JdbcAccess extends AbstractDBAccess implements DBAccess {
    /**
     * jdbc的数据源
     */
    protected DataSource dataSource;
    /**
     * dbutils的QueryRunner对象
     */
    private QueryRunner runner = new QueryRunner(true);

    public JdbcAccess() {

    }

    public JdbcAccess(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * setter
     *
     * @param dataSource
     */
    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void initialize(Object accessObject) {
        if (accessObject == null) return;
        if (accessObject instanceof DataSource) {
            this.dataSource = (DataSource) accessObject;
        }
    }

    /**
     * 返回数据库连接对象
     *
     * @return
     * @throws java.sql.SQLException
     */
    protected Connection getConnection() throws SQLException {
        return JdbcHelper.getConnection(dataSource);
    }

    /**
     * 使用原生JDBC操作BLOB字段
     */
    public void saveProcess(Process process) {
        super.saveProcess(process);
        if (process.getBytes() != null) {
            Connection conn = null;
            PreparedStatement pstmt = null;
            try {
                conn = getConnection();
                pstmt = conn.prepareStatement(PROCESS_UPDATE_BLOB);
                pstmt.setBytes(1, process.getBytes());
                pstmt.setString(2, process.getId());
                pstmt.execute();
            } catch (Exception e) {
                throw new FlowLongException(e.getMessage(), e.getCause());
            } finally {
                try {
                    JdbcHelper.close(pstmt);
                } catch (SQLException e) {
                    throw new FlowLongException(e.getMessage(), e.getCause());
                }
            }
        }
    }

    /**
     * 使用原生JDBC操作BLOB字段
     */
    public void updateProcess(Process process) {
        super.updateProcess(process);
        if (process.getBytes() != null) {
            Connection conn = null;
            PreparedStatement pstmt = null;
            try {
                conn = getConnection();
                pstmt = conn.prepareStatement(PROCESS_UPDATE_BLOB);
                pstmt.setBytes(1, process.getBytes());
                pstmt.setString(2, process.getId());
                pstmt.execute();
            } catch (Exception e) {
                throw new FlowLongException(e.getMessage(), e.getCause());
            } finally {
                try {
                    JdbcHelper.close(pstmt);
                } catch (SQLException e) {
                    throw new FlowLongException(e.getMessage(), e.getCause());
                }
            }
        }
    }

    /**
     * 查询指定列
     *
     * @param column 结果集的列索引号
     * @param sql    sql语句
     * @param params 查询参数
     * @return 指定列的结果对象
     */
    public Object query(int column, String sql, Object... params) {
        Object result;
        try {
            if (log.isDebugEnabled()) {
                log.debug("查询单列数据=\n" + sql);
            }
            result = runner.query(getConnection(), sql, new ScalarHandler(column), params);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e.getMessage(), e);
        }
        return result;
    }

    public Integer getLatestProcessVersion(String name) {
        String where = " where name = ?";
        Object result = query(1, QUERY_VERSION + where, name);
        return new Long(ClassUtils.castLong(result)).intValue();
    }

    public boolean isORM() {
        return false;
    }

    public void saveOrUpdate(Map<String, Object> map) {
        String sql = (String) map.get(KEY_SQL);
        Object[] args = (Object[]) map.get(KEY_ARGS);
        try {
            if (log.isDebugEnabled()) {
                log.debug("增删改数据(需手动提交事务)=\n" + sql);
            }
            runner.update(getConnection(), sql, args);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public <T> T queryObject(Class<T> clazz, String sql, Object... args) {
        List<T> result = null;
        try {
            if (log.isDebugEnabled()) {
                log.debug("查询单条记录=\n" + sql);
            }
            result = runner.query(getConnection(), sql, new BeanPropertyHandler<T>(clazz), args);
            return JdbcHelper.requiredSingleResult(result);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            return null;
        }
    }

    public <T> List<T> queryList(Class<T> clazz, String sql, Object... args) {
        try {
            if (log.isDebugEnabled()) {
                log.debug("查询多条记录=\n" + sql);
            }
            return runner.query(getConnection(), sql, new BeanPropertyHandler<T>(clazz), args);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    public Object queryCount(String sql, Object... args) {
        return query(1, sql, args);
    }
}
