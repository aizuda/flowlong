package test.mysql;

import com.aizuda.bpm.engine.entity.FlwTask;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
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
        // 路由跳转
        testRouteBranch(8);

        // 默认路由
        testRouteBranch(1);
    }

    public void testRouteBranch(int day) {
        Map<String, Object> args = new HashMap<>();
        args.put("day", day);
        args.put("age", 7);
        flowLongEngine.startInstanceById(processId, testCreator).ifPresent(instance -> {

            // 主管审批，路由分支自动驳回至主管
            this.executeTask(instance.getId(), testCreator, args);

            // 主管审批，进入财务总监审批
            this.executeTask(instance.getId(), testCreator, args);

            List<FlwTask> flwTaskList = flowLongEngine.queryService().getTasksByInstanceId(instance.getId());
            Assertions.assertEquals(flwTaskList.get(0).getTaskKey(), day == 8 ? "k005" : "k008");
            flowLongEngine.runtimeService().cascadeRemoveByInstanceId(instance.getId());
        });
    }
}
