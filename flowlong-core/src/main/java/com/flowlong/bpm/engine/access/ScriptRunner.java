/*
 *  Copyright 2023-2025 www.flowlong.com
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package com.flowlong.bpm.engine.access;

import com.flowlong.bpm.engine.assist.Assert;
import com.flowlong.bpm.engine.assist.StreamUtils;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * SQL脚本运行类
 *
 * @author hubin
 * @since 1.0
 */
@Slf4j
public class ScriptRunner {
    private static final String DEFAULT_DELIMITER = ";";
    //数据库连接对象
    private Connection connection;
    //是否自动提交
    private boolean autoCommit;
    //默认的分隔符;
    private String delimiter = DEFAULT_DELIMITER;

    public ScriptRunner(Connection connection, boolean autoCommit) {
        this.connection = connection;
        this.autoCommit = autoCommit;
    }

    public void runScript(String resource) throws IOException, SQLException {
        Assert.notNull(resource);
        InputStream input = StreamUtils.getResourceAsStream(resource);
        Reader reader = new InputStreamReader(input, "UTF-8");
        runScript(reader);
    }

    /**
     * 根据reader读取sql脚本，并运行
     *
     * @param reader 脚本资源
     */
    public void runScript(Reader reader) throws IOException, SQLException {
        Assert.notNull(connection);
        try {
            boolean originalAutoCommit = connection.getAutoCommit();
            try {
                if (originalAutoCommit != this.autoCommit) {
                    connection.setAutoCommit(this.autoCommit);
                }
                runScript(connection, reader);
            } finally {
                connection.setAutoCommit(originalAutoCommit);
            }
        } catch (IOException e) {
            throw e;
        } catch (SQLException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Error running script.  Cause: " + e, e);
        }
    }

    /**
     * 根据给定的sql脚本资源、数据库连接对象，执行sql脚本
     *
     * @param conn   数据库连接对象
     * @param reader sql脚本资源
     * @throws IOException  io异常
     * @throws SQLException sql异常
     */
    private void runScript(Connection conn, Reader reader)
            throws IOException, SQLException {
        StringBuffer command = null;
        try {
            LineNumberReader lineReader = new LineNumberReader(reader);
            String line = null;
            while ((line = lineReader.readLine()) != null) {
                if (command == null) {
                    command = new StringBuffer();
                }
                String trimmedLine = line.trim();
                if (trimmedLine.startsWith("--")) {
                    log.info(trimmedLine);
                } else if (trimmedLine.length() < 1 || trimmedLine.startsWith("//")) {
                    //Do nothing
                } else if (trimmedLine.length() < 1 || trimmedLine.startsWith("--")) {
                    //Do nothing
                } else if (trimmedLine.equals(getDelimiter()) || trimmedLine.endsWith(getDelimiter())) {
                    command.append(line.substring(0, line.lastIndexOf(getDelimiter())));
                    command.append(" ");
                    Statement statement = conn.createStatement();

                    log.info(command.toString());
                    try {
                        statement.execute(command.toString());
                    } catch (SQLException e) {
                        e.fillInStackTrace();
                        log.error("Error executing: " + command);
                    }

                    if (autoCommit && !conn.getAutoCommit()) {
                        conn.commit();
                    }
                    command = null;
                    try {
                        statement.close();
                    } catch (Exception e) {
                        //ignore
                    }
                    Thread.yield();
                } else {
                    command.append(line);
                    command.append(" ");
                }
            }
            if (!autoCommit) {
                conn.commit();
            }
        } catch (SQLException e) {
            e.fillInStackTrace();
            throw e;
        } catch (IOException e) {
            e.fillInStackTrace();
            throw e;
        }
    }

    /**
     * 获取分隔符
     *
     * @return delimiter
     */
    private String getDelimiter() {
        return delimiter;
    }

    public void setDelimiter(String delimiter) {
        this.delimiter = delimiter;
    }
}
