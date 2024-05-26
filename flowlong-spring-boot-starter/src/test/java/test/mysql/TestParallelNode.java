package test.mysql;

import com.aizuda.bpm.engine.assist.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class TestParallelNode extends MysqlTest {

    @Test
    public void test() {
        processId = this.deployByResource("test/parallelProcess.json", testCreator);
        // 启动指定流程定义ID启动流程实例
        flowLongEngine.startInstanceById(processId, testCreator).ifPresent(instance -> {

            this.executeTaskByKey(instance.getId(), test2Creator, "k003");

            flowLongEngine.queryService().getActiveTasksByInstanceId(instance.getId())
                    .ifPresent(flwTasks -> Assert.isFalse(flwTasks.size() == 1, "task size should be one"));

            this.executeActiveTasks(instance.getId(), test3Creator);

            flowLongEngine.queryService().getActiveTasksByInstanceId(instance.getId())
                    .ifPresent(flwTasks -> Assert.isFalse(flwTasks.isEmpty(), "task size should be zero"));
        });
    }


    @Test
    public void testMoreConditionParallel() {
        processId = this.deployByResource("test/moreConditionParallel.json", testCreator);
        // 启动指定流程定义ID启动流程实例
        flowLongEngine.startInstanceById(processId, testCreator).ifPresent(instance -> {

            flowLongEngine.queryService().getActiveTasksByInstanceId(instance.getId())
                    .ifPresent(flwTasks -> Assert.isFalse(flwTasks.size() == 3, "task size should be one"));

            this.executeTaskByKey(instance.getId(), test3Creator, "k005");

            flowLongEngine.queryService().getActiveTasksByInstanceId(instance.getId())
                    .ifPresent(flwTasks -> Assert.isFalse(flwTasks.size() == 2, "task size should be one"));

            this.executeTaskByKey(instance.getId(), test2Creator, "k003");
            this.executeTaskByKey(instance.getId(), test3Creator, "k004");

            flowLongEngine.queryService().getActiveTasksByInstanceId(instance.getId())
                    .ifPresent(flwTasks -> Assert.isFalse(flwTasks.isEmpty(), "task size should be zero"));
        });
    }
}
