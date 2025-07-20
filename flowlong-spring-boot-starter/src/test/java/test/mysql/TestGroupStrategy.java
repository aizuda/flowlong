/*
 * Copyright 2023-2025 Licensed under the Dual Licensing
 * website: https://aizuda.com
 */
package test.mysql;

import com.aizuda.bpm.engine.core.Execution;
import com.aizuda.bpm.engine.core.FlowCreator;
import com.aizuda.bpm.engine.core.enums.NodeSetType;
import com.aizuda.bpm.engine.entity.FlwHisInstance;
import com.aizuda.bpm.engine.entity.FlwTaskActor;
import com.aizuda.bpm.engine.impl.GeneralTaskActorProvider;
import com.aizuda.bpm.engine.model.NodeAssignee;
import com.aizuda.bpm.engine.model.NodeModel;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 测试分组审批策略
 *
 * @author 青苗
 */
@Slf4j
public class TestGroupStrategy extends MysqlTest {

    @Test
    public void testAllJoin() {
        Long processId = this.deployByResource("test/testGroupStrategyAllJoin.json", testCreator);
        this.flowLongEngine.getContext().setTaskActorProvider(new TestGroupStrategyTaskActorProvider());
        flowLongEngine.startInstanceById(processId, testCreator).ifPresent(instance -> {

            // 部门领导、测试人员 【或签审核】
            this.executeTask(instance.getId(), test5Creator);

            // 考勤管理员、财务总监 【顺序签审核】
            this.executeTaskByKey(instance.getId(), test4Creator, "flk1752830262547");
            this.executeTaskByKey(instance.getId(), test3Creator, "flk1752830262547");
            this.executeTaskByKey(instance.getId(), test2Creator, "flk1752830262547");

            // 技术总监、CEO 【会签审核】
            this.executeTask(instance.getId(), test4Creator);
            this.executeTask(instance.getId(), test3Creator);
            this.executeTask(instance.getId(), test2Creator);
            this.executeTask(instance.getId(), testCreator);

            // 流程执行结束
            FlwHisInstance fhi = flowLongEngine.queryService().getHistInstance(instance.getId());
            Assertions.assertEquals(1, fhi.getInstanceState());
        });
    }

    @Test
    public void testMix() {
        Long processId = this.deployByResource("test/testGroupStrategyMix.json", testCreator);
        this.flowLongEngine.getContext().setTaskActorProvider(new TestGroupStrategyTaskActorProvider());
        flowLongEngine.startInstanceById(processId, testCreator).ifPresent(instance -> {

            // 部门领导、测试人员 【或签审核】 执行任务认领
            this.executeActiveTasks(instance.getId(), flwTask -> flowLongEngine.taskService()
                    .claimRole(flwTask.getId(), testCreator));
            // 执行认领任务
            this.executeTask(instance.getId(), testCreator);

            // 考勤管理员、财务总监 【顺序签审核】
            this.executeTaskByKey(instance.getId(), test4Creator, "flk1752830262547");
            this.executeTaskByKey(instance.getId(), test3Creator, "flk1752830262547");
            this.executeTaskByKey(instance.getId(), test2Creator, "flk1752830262547");

            // 技术总监、CEO 【会签审核】

            // 技术总监任务认领
            this.executeTask(instance.getId(), FlowCreator.of("role002", "技术总监"), flwTask -> flowLongEngine.taskService()
                    .claimRole(flwTask.getId(), test2Creator));
            // 执行技术总监认领任务
            this.executeTask(instance.getId(), test2Creator);

            // CEO任务认领
            this.executeTask(instance.getId(), FlowCreator.of("role001", "CEO"), flwTask -> flowLongEngine.taskService()
                    .claimRole(flwTask.getId(), test3Creator));
            // 执行CEO认领任务
            this.executeTask(instance.getId(), test3Creator);

            // 流程执行结束
            FlwHisInstance fhi = flowLongEngine.queryService().getHistInstance(instance.getId());
            Assertions.assertEquals(1, fhi.getInstanceState());
        });
    }

    class TestGroupStrategyTaskActorProvider extends GeneralTaskActorProvider {

        @Override
        public boolean isAllowed(NodeModel nodeModel, FlowCreator flowCreator) {
            // 执行判断合法性
            return true;
        }

        @Override
        public List<FlwTaskActor> getTaskActors(NodeModel nodeModel, Execution execution) {
            if (NodeSetType.role.eq(nodeModel.getSetType())) {
                if (nodeModel.allJoinGroupStrategy()) {
                    // 全部人员参与审批分组策略，下面模拟动态提供具体流程参与者信息
                    List<FlwTaskActor> ftaList = new ArrayList<>();
                    for (NodeAssignee nodeAssignee : nodeModel.getNodeAssigneeList()) {
                        String id = nodeAssignee.getId();
                        if (Objects.equals("role001", id)) {
                            // CEO
                            ftaList.add(FlwTaskActor.ofFlowCreator(testCreator));
                            ftaList.add(FlwTaskActor.ofFlowCreator(test2Creator));
                        } else if (Objects.equals("role002", id)) {
                            // 技术总监
                            ftaList.add(FlwTaskActor.ofFlowCreator(test3Creator));
                            ftaList.add(FlwTaskActor.ofFlowCreator(test4Creator));
                        } else if (Objects.equals("role003", id)) {
                            // 财务总监
                            ftaList.add(FlwTaskActor.ofFlowCreator(test3Creator));
                            ftaList.add(FlwTaskActor.ofFlowCreator(test2Creator));
                        } else if (Objects.equals("role004", id)) {
                            // 考勤管理员
                            ftaList.add(FlwTaskActor.ofFlowCreator(test4Creator));
                            ftaList.add(FlwTaskActor.ofFlowCreator(test3Creator));
                        } else if (Objects.equals("role005", id)) {
                            // 部门领导
                            ftaList.add(FlwTaskActor.ofFlowCreator(testCreator));
                            ftaList.add(FlwTaskActor.ofFlowCreator(test3Creator));
                        } else if (Objects.equals("role006", id)) {
                            // 测试人员
                            ftaList.add(FlwTaskActor.ofFlowCreator(test5Creator));
                            ftaList.add(FlwTaskActor.ofFlowCreator(test6Creator));
                        }
                    }

                    // 数据查询列表根据 id 排序、去重审批用户，避免【顺序签】错误
                    List<FlwTaskActor> flwTaskActors = new ArrayList<>();
                    for (FlwTaskActor fta : ftaList) {
                        if (flwTaskActors.stream().noneMatch(t -> Objects.equals(t.getActorId(), fta.getActorId()))) {
                            flwTaskActors.add(fta);
                        }
                    }
                    return flwTaskActors;
                }
            }
            return super.getTaskActors(nodeModel, execution);
        }
    }
}
