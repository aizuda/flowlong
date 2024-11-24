package test.mysql;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * 路由分支测试
 */
public class TestRouteBranch extends MysqlTest {

    @BeforeEach
    public void before() {
        processId = this.deployByResource("test/routeBranch.json", testCreator);
    }

    @Test
    public void test() {
        Map<String, Object> args = new HashMap<>();
        args.put("day", 8);
        args.put("age", 7);
        flowLongEngine.startInstanceById(processId, testCreator, args).ifPresent(instance -> {

            // 主管审批，路由分支自动驳回至主管
            this.executeTask(instance.getId(), testCreator, args);

            // 主管审批，进入财务总监审批
            args.put("age", 20);
            this.executeTask(instance.getId(), testCreator, args);


        });
    }
}
