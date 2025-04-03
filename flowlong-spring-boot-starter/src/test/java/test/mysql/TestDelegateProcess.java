package test.mysql;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class TestDelegateProcess extends MysqlTest {

    @BeforeEach
    public void before() {
        processId = this.deployByResource("test/delegateProcess.json", testCreator);
    }

    @Test
    public void testDelegateTask() {
        flowLongEngine.startInstanceById(processId, testCreator).ifPresent(instance -> {

            // test 委托任务给 test2
            this.executeTask(instance.getId(), testCreator, flwTask ->
                    flowLongEngine.taskService().delegateTask(flwTask.getId(), testCreator, test2Creator));

            // test2 委托任务给 test3
            this.executeTask(instance.getId(), test2Creator, flwTask ->
                    flowLongEngine.taskService().delegateTask(flwTask.getId(), test2Creator, test3Creator));

            // 被委派人 test3 解决问题，后归还任务给委派人
            this.executeTask(instance.getId(), test3Creator, flwTask -> flowLongEngine.taskService()
                    .resolveTask(flwTask.getId(), test3Creator));

            flowLongEngine.queryService().getActiveTaskActorsByInstanceId(instance.getId()).ifPresent(taskActors -> {
                Assertions.assertEquals(1, taskActors.size());
                Assertions.assertEquals(testCreator.getCreateBy(), taskActors.get(0).getActorName());
            });
        });
    }
}
