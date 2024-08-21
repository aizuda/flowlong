package test.mysql;

import com.aizuda.bpm.engine.entity.FlwHisInstance;
import com.aizuda.bpm.engine.entity.FlwTask;
import org.junit.jupiter.api.Assertions;
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
        Map<String, Object> args = new HashMap<>();

        // 走默认分支，有 1 个执行任务
        args.put("age", 1);
        flowLongEngine.startInstanceById(processId, testCreator, args).flatMap(i -> flowLongEngine.queryService()
                .getActiveTasksByInstanceId(i.getId())).ifPresent(t -> {
            Assertions.assertEquals(1, t.size());

            // 李小广 审批
            FlwTask flwTask = t.get(0);
            flowLongEngine.executeTask(flwTask.getId(), test3Creator);

            // 流程结束
            FlwHisInstance fhi = flowLongEngine.queryService().getHistInstance(flwTask.getInstanceId());
            Assertions.assertEquals(1, fhi.getInstanceState());
        });
    }

    @Test
    public void test2() {
        processId = this.deployByResource("test/inclusiveProcess.json", testCreator);
        Map<String, Object> args = new HashMap<>();

        // 走 age < 8 分支，有 2 个执行任务
        args.put("age", 8);
        flowLongEngine.startInstanceById(processId, testCreator, args).flatMap(i -> flowLongEngine.queryService()
                .getActiveTasksByInstanceId(i.getId())).ifPresent(t -> {
            Assertions.assertEquals(2, t.size());

            // 王小飞 审批
            FlwTask flwTask = t.get(0);
            flowLongEngine.executeTask(flwTask.getId(), testCreator);

            // 陈小超 审批
            FlwTask flwTask2 = t.get(1);
            flowLongEngine.executeTask(flwTask2.getId(), test2Creator);

            // 李小广 审批
            flowLongEngine.queryService().getActiveTasksByInstanceId(flwTask.getInstanceId()).ifPresent(t2 -> {
                Assertions.assertEquals(1, t2.size());
                flowLongEngine.executeTask(t2.get(0).getId(), test3Creator);
            });

            // 流程结束
            FlwHisInstance fhi = flowLongEngine.queryService().getHistInstance(flwTask.getInstanceId());
            Assertions.assertEquals(1, fhi.getInstanceState());
        });
    }

    @Test
    public void test3() {
        processId = this.deployByResource("test/inclusiveProcess.json", testCreator);
        Map<String, Object> args = new HashMap<>();

        // 走 age < 6 分支，有 1 个执行任务
        args.put("age", 6);
        flowLongEngine.startInstanceById(processId, testCreator, args).flatMap(i -> flowLongEngine.queryService()
                .getActiveTasksByInstanceId(i.getId())).ifPresent(t -> {
            Assertions.assertEquals(1, t.size());

            // 陈小超 审批
            FlwTask flwTask = t.get(0);
            flowLongEngine.executeTask(flwTask.getId(), test2Creator);

            // 李小广 审批
            flowLongEngine.queryService().getActiveTasksByInstanceId(flwTask.getInstanceId()).ifPresent(t2 -> {
                Assertions.assertEquals(1, t2.size());
                flowLongEngine.executeTask(t2.get(0).getId(), test3Creator);
            });

            // 流程结束
            FlwHisInstance fhi = flowLongEngine.queryService().getHistInstance(flwTask.getInstanceId());
            Assertions.assertEquals(1, fhi.getInstanceState());
        });
    }

}
