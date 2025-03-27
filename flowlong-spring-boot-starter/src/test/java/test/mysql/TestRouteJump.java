package test.mysql;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

public class TestRouteJump extends MysqlTest {

    @BeforeEach
    public void before() {
        processId = this.deployByResource("test/routeJump.json", testCreator);
    }

    @Test
    public void test() {
        flowLongEngine.startInstanceById(processId, testCreator).ifPresent(instance -> {

            Map<String, Object> args = new HashMap<>();
            args.put("age", 1);

            // 审核人2审批
            this.executeActiveTasks(instance.getId(), t ->
                    flowLongEngine.executeTask(t.getId(), testCreator, args)
            );

            this.executeActiveTasks(instance.getId(), flwTask ->

                    // 路由到发起人
                    Assertions.assertEquals("发起人", flwTask.getTaskName()));
        });
    }
}
