package test.mysql;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

public class TestAiHandle extends MysqlTest {
    public static final String CONTENT = "申请请假一周请领导批准";

    @BeforeEach
    public void before() {
        processId = this.deployByResource("test/aiHandle.json", testCreator);
    }

    @Test
    public void test() {
        Map<String, Object> args = new HashMap<>();
        args.put("content", CONTENT);
        flowLongEngine.startInstanceById(processId, testCreator, args).ifPresent(instance -> {

        });
    }
}
