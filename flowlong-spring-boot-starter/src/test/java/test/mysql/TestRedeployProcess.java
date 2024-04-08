package test.mysql;

import com.aizuda.bpm.engine.entity.FlwHisInstance;
import com.aizuda.bpm.engine.entity.FlwInstance;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

/**
 * 在流程实例执行过程中，重新部署流程定义，验证之前的流程实例是否受影响
 *
 * @author songyinyin
 * @since 2024/4/8 上午9:52
 */
public class TestRedeployProcess extends MysqlTest {

    @BeforeEach
    public void before() {
        processId = this.deployByResource("test/sortSign.json", testCreator);
    }

    @Test
    public void test() {
        // 启动指定流程定义ID启动流程实例
        Optional<FlwInstance> flwInstanceOpt = flowLongEngine.startInstanceById(processId, testCreator);
        if (!flwInstanceOpt.isPresent()) {
            Assertions.fail();
        }
        FlwInstance instance = flwInstanceOpt.get();

        // 会签审批人001【审批】，执行转办、任务交给 test2 处理
        this.executeTask(instance.getId(), testCreator, flwTask -> flowLongEngine.taskService()
                .transferTask(flwTask.getId(), testCreator, test2Creator));


        // 被转办人 test2 审批
        this.executeTask(instance.getId(), test2Creator);

        // 重新部署一个 V2 版本，只有 test1 审批
        Long processIdV2 = flowLongEngine.processService().deployByResource("test/sortSignV2.json", testCreator, true);
        Optional<FlwInstance> flwInstanceV2Opt = flowLongEngine.startInstanceById(processIdV2, testCreator);
        if (!flwInstanceV2Opt.isPresent()) {
            Assertions.fail();
        }
        FlwInstance instanceV2 = flwInstanceV2Opt.get();

        // test3 领导审批同意
        this.executeActiveTasks(instance.getId(), test3Creator);
        // 抄送人力资源，流程自动结束
        FlwHisInstance histInstance = flowLongEngine.queryService().getHistInstance(instance.getId());
        Assertions.assertEquals(1, histInstance.getInstanceState());

        // V2: test3 领导审批同意
        this.executeActiveTasks(instanceV2.getId(), testCreator);

        FlwHisInstance histInstanceV2 = flowLongEngine.queryService().getHistInstance(instanceV2.getId());
        Assertions.assertEquals(1, histInstanceV2.getInstanceState());

    }

}
