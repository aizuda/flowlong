/*
 * Copyright 2023-2025 Licensed under the Dual Licensing
 * website: https://aizuda.com
 */
package test.mysql;

import com.aizuda.bpm.engine.QueryService;
import com.aizuda.bpm.engine.core.FlowCreator;
import com.aizuda.bpm.engine.entity.FlwTask;
import com.aizuda.bpm.engine.entity.FlwTaskActor;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import test.TestFlowLong;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Mysql 测试基类
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(locations = {"classpath:spring-test-mysql.xml"})
public class MysqlTest extends TestFlowLong {

    protected FlowCreator testCreator = FlowCreator.of(testUser1, "测试001");
    protected FlowCreator test2Creator = FlowCreator.of(testUser2, "测试002");
    protected FlowCreator test3Creator = FlowCreator.of(testUser3, "测试003");
    protected FlowCreator test4Creator = FlowCreator.of(testUser4, "测试004");
    protected FlowCreator test5Creator = FlowCreator.of(testUser5, "测试005");
    protected FlowCreator test6Creator = FlowCreator.of(testUser6, "测试006");

    /**
     * 执行当前活跃用户
     *
     * @param instanceId  流程实例ID
     * @param testCreator 任务创建者
     */
    public void executeActiveTasks(Long instanceId, FlowCreator testCreator) {
        this.executeActiveTasks(instanceId, testCreator, null);
    }

    public void executeActiveTasks(Long instanceId, FlowCreator testCreator, Map<String, Object> args) {
        this.executeActiveTasks(instanceId, t -> this.flowLongEngine.executeTask(t.getId(), testCreator, args));
    }

    public void executeActiveTasks(Long instanceId, Consumer<FlwTask> taskConsumer) {
        this.flowLongEngine.queryService().getActiveTasksByInstanceId(instanceId)
                .ifPresent(tasks -> tasks.forEach(taskConsumer));
    }

    public void executeTask(Long instanceId, FlowCreator flowCreator) {
        executeTask(instanceId, flowCreator, flwTask -> this.flowLongEngine.executeTask(flwTask.getId(), flowCreator));
    }

    public void executeTask(Long instanceId, FlowCreator flowCreator, Map<String, Object> args) {
        executeTask(instanceId, flowCreator, flwTask -> this.flowLongEngine.executeTask(flwTask.getId(), flowCreator, args));
    }

    public void executeTask(Long instanceId, FlowCreator flowCreator, Consumer<FlwTask> flwTaskConsumer) {
        QueryService queryService = this.flowLongEngine.queryService();
        List<FlwTask> flwTaskList = queryService.getTasksByInstanceId(instanceId);
        for (FlwTask flwTask : flwTaskList) {
            List<FlwTaskActor> taskActors = queryService.getTaskActorsByTaskId(flwTask.getId());
            if (null != taskActors && taskActors.stream()
                    // 找到当前对应审批的任务执行
                    .anyMatch(t -> Objects.equals(t.getActorId(), flowCreator.getCreateId()))) {
                flwTaskConsumer.accept(flwTask);
            }
        }
    }

    public void executeTaskByKey(Long instanceId, FlowCreator flowCreator, String nodeKey) {
        QueryService queryService = this.flowLongEngine.queryService();
        List<FlwTask> flwTaskList = queryService.getTasksByInstanceId(instanceId);
        flwTaskList.stream().filter(flwTask -> flwTask.getTaskKey().equals(nodeKey)).findFirst()
                .ifPresent(flwTask -> this.flowLongEngine.executeTask(flwTask.getId(), flowCreator));
    }
}
