/*
 * Copyright 2023-2025 Licensed under the apache-2.0 License
 * website: https://aizuda.com
 */
package test.mysql;

import org.junit.jupiter.api.Test;

/**
 * 测试流程
 */
public class TestProcess extends MysqlTest {

    @Test
    public void test() {
        // 多次执行观察数据库版本自增，归档历史流程
        flowLongEngine.processService().deployByResource("test/simpleProcess.json",
                testCreator, true, flwProcess -> flwProcess.setProcessIcon("自定义图标"));
    }
}
