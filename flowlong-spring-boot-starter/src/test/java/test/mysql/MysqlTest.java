/* 
 * Copyright 2023-2025 Licensed under the AGPL License
 */
package test.mysql;

import com.flowlong.bpm.engine.QueryService;
import com.flowlong.bpm.engine.core.FlowCreator;
import com.flowlong.bpm.engine.entity.FlwTask;
import com.flowlong.bpm.engine.entity.FlwTaskActor;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import test.TestFlowLong;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Mysql 测试基类
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(locations = {"classpath:spring-test-mysql.xml"})
public class MysqlTest extends TestFlowLong {

    protected FlowCreator testCreator = FlowCreator.of(testUser1, "测试001");
    protected FlowCreator test3Creator = FlowCreator.of(testUser3, "测试003");

    /**
     * 执行当前活跃用户
     *
     * @param instanceId  流程实例ID
     * @param testCreator 任务创建者
     */
    public void executeActiveTasks(Long instanceId, FlowCreator testCreator) {
        this.executeActiveTasks(instanceId, t -> this.flowLongEngine.executeTask(t.getId(), testCreator));
    }

    public void executeActiveTasks(Long instanceId, Consumer<FlwTask> taskConsumer) {
        this.flowLongEngine.queryService().getActiveTasksByInstanceId(instanceId)
                .ifPresent(tasks -> tasks.forEach(t -> taskConsumer.accept(t)));
    }

    public void executeTask(Long instanceId, FlowCreator flowCreator) {
        QueryService queryService = this.flowLongEngine.queryService();
        List<FlwTask> flwTaskList = queryService.getTasksByInstanceId(instanceId);
        for (FlwTask flwTask : flwTaskList) {
            List<FlwTaskActor> taskActors = queryService.getTaskActorsByTaskId(flwTask.getId());
            if (null != taskActors && taskActors.stream()
                    // 找到当前对应审批的任务执行
                    .anyMatch(t -> Objects.equals(t.getActorId(), flowCreator.getCreateId()))) {
                // 执行审批
                this.flowLongEngine.executeTask(flwTask.getId(), flowCreator);
            }
        }
    }
}
