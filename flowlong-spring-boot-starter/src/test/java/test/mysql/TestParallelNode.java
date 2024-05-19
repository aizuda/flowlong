package test.mysql;

import com.aizuda.bpm.engine.TaskService;
import com.aizuda.bpm.engine.assist.Assert;
import com.aizuda.bpm.engine.entity.FlwTask;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

public class TestParallelNode extends MysqlTest{

    @BeforeEach
    public void before() {
        processId = this.deployByResource("test/parallelProcess.json", testCreator);
    }

    @Test
    public void test() {
        // 启动指定流程定义ID启动流程实例
        flowLongEngine.startInstanceById(processId, testCreator).ifPresent(instance -> {
            executeTaskByKey(instance.getId(),test2Creator,"k003");
            flowLongEngine.queryService().getActiveTasksByInstanceId(instance.getId()).ifPresent(flwTasks -> {
                Assert.isFalse(flwTasks.size() ==1,"task size should be one");
            });
            this.executeActiveTasks(instance.getId(), test3Creator);
            flowLongEngine.queryService().getActiveTasksByInstanceId(instance.getId()).ifPresent(flwTasks -> {
                Assert.isFalse(flwTasks.isEmpty(),"task size should be zero");
            });
        });
    }
}
