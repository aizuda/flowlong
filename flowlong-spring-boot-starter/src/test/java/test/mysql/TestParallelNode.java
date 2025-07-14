package test.mysql;

import com.aizuda.bpm.engine.assist.Assert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * 并行分支测试
 */
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


    @Test
    public void testCcTaskParallel() {
        processId = this.deployByResource("test/ccTaskParallel.json", testCreator);
        flowLongEngine.startInstanceById(processId, testCreator).flatMap(instance ->
                flowLongEngine.queryService().getActiveTasksByInstanceId(instance.getId()))
                .ifPresent(t -> Assertions.assertEquals(1, t.size()));
    }


    @Test
    public void testParallelCC() {
        processId = this.deployByResource("test/parallelCC.json", testCreator);
        flowLongEngine.startInstanceById(processId, testCreator).ifPresent(instance -> {
            this.executeTaskByKey(instance.getId(), testCreator, "k004");
            this.executeTaskByKey(instance.getId(), test2Creator, "k006");
        });
    }


    @Test
    public void testParallelJumpTask() {
        processId = this.deployByResource("test/parallelJumpTask.json", testCreator);
        flowLongEngine.startInstanceById(processId, testCreator).ifPresent(instance -> {

            // 分支2审核
            this.executeTask(instance.getId(), test2Creator);

            // 领导审核跳转到分支2审核
            this.executeTask(instance.getId(), test3Creator, flwTask -> {

                flowLongEngine.executeJumpTask(flwTask.getId(), "flk1736078362210", test3Creator);

                flowLongEngine.queryService().getActiveTasksByInstanceId(instance.getId())
                        .ifPresent(t -> Assertions.assertEquals(2, t.size()));
            });

            // 分支2审核跳转到发起人
            this.executeTask(instance.getId(), test2Creator, flwTask -> {

                flowLongEngine.executeJumpTask(flwTask.getId(), "flk1735871288160", test2Creator);

                flowLongEngine.queryService().getActiveTasksByInstanceId(instance.getId())
                        .ifPresent(t -> Assertions.assertEquals(1, t.size()));
            });

        });
    }

    @Test
    public void testParallelJumpReject() {
        processId = this.deployByResource("test/parallelJumpTask.json", testCreator);
        flowLongEngine.startInstanceById(processId, testCreator).ifPresent(instance -> {
            //分支1 审核
            this.executeTaskByKey(instance.getId(), testCreator, "flk1736078360143");
            //分支2 审核
            this.executeTaskByKey(instance.getId(), test2Creator, "flk1736078362210");
            //分支2 领导审核
            this.executeTaskByKey(instance.getId(), test3Creator, "flk1736078364197");

            //汇总后驳回任务到发起人
            this.executeTask(instance.getId(), test3Creator, flwTask ->
                    flowLongEngine.executeRejectTask(flwTask, "flk1735871288160", test3Creator));

        });
    }

    @Test
    public void testParallelJumpNodeKey() {
        processId = this.deployByResource("test/parallelJumpTask.json", testCreator);
        flowLongEngine.startInstanceById(processId, testCreator).ifPresent(instance -> {
            //分支1 审核
            this.executeTaskByKey(instance.getId(), testCreator, "flk1736078360143");
            //分支2 审核
            this.executeTaskByKey(instance.getId(), test2Creator, "flk1736078362210");

            //分支1 审核A 驳回任务到 分支1 审核 不影响分支 2 的任务
            this.executeTask(instance.getId(), test2Creator, flwTask ->
                    flowLongEngine.executeRejectTask(flwTask, "flk1736078360143", test2Creator));

            // 判断当前还存在2个任务
            flowLongEngine.queryService().getActiveTasksByInstanceId(instance.getId())
                    .ifPresent(t -> Assertions.assertEquals(2, t.size()));
        });
    }
}
