package test.mysql;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * 包容分支测试
 */
public class TestInclusiveNode extends MysqlTest {

    @Test
    public void test() {
        processId = this.deployByResource("test/inclusiveProcess.json", testCreator);
        // 启动指定流程定义ID启动流程实例
        Map<String, Object> args = new HashMap<>();
        args.put("age", 8);
        flowLongEngine.startInstanceById(processId, testCreator, args).ifPresent(instance -> {

        });
    }

}
