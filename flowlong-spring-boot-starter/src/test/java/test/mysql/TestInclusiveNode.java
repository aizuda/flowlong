package test.mysql;

import org.junit.jupiter.api.Test;

/**
 * 包容分支测试
 */
public class TestInclusiveNode extends MysqlTest {

    @Test
    public void test() {
        processId = this.deployByResource("test/inclusiveProcess.json", testCreator);
        // 启动指定流程定义ID启动流程实例
        flowLongEngine.startInstanceById(processId, testCreator).ifPresent(instance -> {

        });
    }

}
