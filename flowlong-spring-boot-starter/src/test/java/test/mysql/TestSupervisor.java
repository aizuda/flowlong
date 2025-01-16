package test.mysql;

import com.aizuda.bpm.engine.TaskActorProvider;
import com.aizuda.bpm.engine.assist.ObjectUtils;
import com.aizuda.bpm.engine.core.Execution;
import com.aizuda.bpm.engine.core.FlowCreator;
import com.aizuda.bpm.engine.core.enums.NodeSetType;
import com.aizuda.bpm.engine.entity.FlwTaskActor;
import com.aizuda.bpm.engine.model.NodeModel;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 连续主管审批顺签
 *
 * @author 青苗
 */
@Slf4j
public class TestSupervisor extends MysqlTest {

    protected FlowCreator user1 = FlowCreator.of("1", "一级部门");
    protected FlowCreator user2 = FlowCreator.of("2", "二级部门");
    protected FlowCreator user3 = FlowCreator.of("3", "三级部门");
    protected FlowCreator user4 = FlowCreator.of("4", "四级部门");

    @BeforeEach
    public void before() {
        processId = this.deployByResource("test/supervisor.json", testCreator);
        /*
         * 自定义参与者提供测试
         */
        this.flowLongEngine.getContext().setTaskActorProvider(
                new TaskActorProvider() {

                    @Override
                    public Integer getActorType(NodeModel nodeModel) {

                        // 1，角色
                        if (NodeSetType.role.eq(nodeModel.getSetType())) {
                            return 1;
                        }

                        // 2，部门
                        if (NodeSetType.department.eq(nodeModel.getSetType())) {
                            return 2;
                        }

                        return 0;
                    }

                    @Override
                    public List<FlwTaskActor> getTaskActors(NodeModel nodeModel, Execution execution) {
                        if (nodeModel.getType() == 0) {
                            // 发起人审批，经过 isAllowed 验证合法，直接返回当前执行人
                            return Collections.singletonList(FlwTaskActor.ofFlowCreator(execution.getFlowCreator()));
                        }
                        if (nodeModel.getType() == 1 && nodeModel.getSetType() == 7) {
                            /*
                             * 审核人类型 1，指定成员 7，连续多级主管
                             */
                            List<FlwTaskActor> flwTaskActors = new ArrayList<>();
                            flwTaskActors.add(FlwTaskActor.ofFlowCreator(user4));
                            flwTaskActors.add(FlwTaskActor.ofFlowCreator(user3));
                            flwTaskActors.add(FlwTaskActor.ofFlowCreator(user2));
                            flwTaskActors.add(FlwTaskActor.ofFlowCreator(user1));
                            return flwTaskActors;
                        }
                        if (ObjectUtils.isNotEmpty(nodeModel.getNodeAssigneeList())) {
                            if (NodeSetType.role.eq(nodeModel.getSetType())) {
                                return nodeModel.getNodeAssigneeList().stream().map(t -> FlwTaskActor.ofRole(t.getTenantId(), t.getId(), t.getName()))
                                        .collect(Collectors.toList());
                            }

                            return nodeModel.getNodeAssigneeList().stream().map(FlwTaskActor::ofNodeAssignee).collect(Collectors.toList());
                        }
                        return Collections.emptyList();
                    }

                    @Override
                    public boolean isAllowed(NodeModel nodeModel, FlowCreator flowCreator) {
                        // 执行判断合法性
                        return true;
                    }
                }
        );
    }

    @Test
    public void test() {
        // 四级部门发起审批
        Map<String, Object> args = new HashMap<>();
        args.put("day", 4);
        // 启动指定流程定义ID启动流程实例
        flowLongEngine.startInstanceById(processId, user4, args).ifPresent(instance -> {

            /// 四级部门审核
            this.executeActiveTasks(instance.getId(), user4);

            // 三级部门审核
            this.executeActiveTasks(instance.getId(), user3);

            // 二级部门审核
            this.executeActiveTasks(instance.getId(), user2);

            // 一级部门审核
            this.executeActiveTasks(instance.getId(), user1);

        });
    }
}
