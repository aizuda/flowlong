package test.mysql;

import com.aizuda.bpm.engine.FlowDataTransfer;
import com.aizuda.bpm.engine.FlowLongEngine;
import com.aizuda.bpm.engine.core.enums.TaskType;
import com.aizuda.bpm.engine.entity.FlwTask;
import com.aizuda.bpm.engine.entity.FlwTaskActor;
import com.aizuda.bpm.engine.model.DynamicAssignee;
import com.aizuda.bpm.engine.model.NodeAssignee;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestJump extends MysqlTest {

    @Autowired
    protected FlowLongEngine flowLongEngine;

    @Test
    public void testJump() {
        Long processId = this.deployByResource("test/testJumpTaskActor.json", testCreator);
        flowLongEngine.startInstanceById(processId, testCreator).ifPresent(instance -> {
            executeTask(instance.getId(), testCreator, flwTask -> {
                flowLongEngine.executeJumpTask(flwTask.getId(), "flk1733380972879", testCreator);
            });

            // 跳转后，应该把当前的任务迁移到历史表，重新生成跳转节点的任务
            List<FlwTask> flwTaskList = flowLongEngine.queryService().getTasksByInstanceId(instance.getId());
            Assertions.assertEquals(1, flwTaskList.size());
            FlwTask flwTask = flwTaskList.get(0);
            List<FlwTaskActor> taskActors = flowLongEngine.queryService().getTaskActorsByTaskId(flwTask.getId());
            Assertions.assertEquals(1, taskActors.size());
            Assertions.assertEquals("陈小辉", taskActors.get(0).getActorName());

            // 设置指定跳转节点处理人
            String jumpNodeId = "flk1733396309547";
            Map<String, DynamicAssignee> assigneeMap = new HashMap<>();
            assigneeMap.put(jumpNodeId, DynamicAssignee.builder().type(1).assigneeList(Collections.singletonList(
                    NodeAssignee.builder().id("test003").name("张三").build()
            )));
            FlowDataTransfer.dynamicAssignee(Collections.unmodifiableMap(assigneeMap));
            flowLongEngine.executeJumpTask(flwTask.getId(), jumpNodeId, testCreator, null, TaskType.jump).ifPresent(tasks -> {
                List<FlwTaskActor> taskActors2 = flowLongEngine.queryService().getTaskActorsByTaskId(tasks.get(0).getId());
                Assertions.assertEquals(1, taskActors2.size());
                Assertions.assertEquals("张三", taskActors2.get(0).getActorName());
            });
        });


    }
}
