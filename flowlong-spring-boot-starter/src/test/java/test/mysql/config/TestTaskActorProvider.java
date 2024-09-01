package test.mysql.config;

import com.aizuda.bpm.engine.core.Execution;
import com.aizuda.bpm.engine.core.FlowCreator;
import com.aizuda.bpm.engine.core.enums.NodeSetType;
import com.aizuda.bpm.engine.entity.FlwTaskActor;
import com.aizuda.bpm.engine.impl.GeneralTaskActorProvider;
import com.aizuda.bpm.engine.model.NodeModel;

import java.util.Arrays;
import java.util.List;

/**
 * 测试任务参与者提供处理类
 */
public class TestTaskActorProvider extends GeneralTaskActorProvider {

    @Override
    public List<FlwTaskActor> getTaskActors(NodeModel nodeModel, Execution execution) {
        if (NodeSetType.role.eq(nodeModel.getSetType())) {

            // 测试用例 TestAutoSkip 测试角色自动分配处理人员
            if ("100100".equals(nodeModel.getNodeAssigneeList().get(0).getId())) {
                return Arrays.asList(
                        FlwTaskActor.ofFlowCreator(FlowCreator.of("test001", "测试001")),
                        FlwTaskActor.ofFlowCreator(FlowCreator.of("test002", "测试002"))
                );
            }
        }
        return super.getTaskActors(nodeModel, execution);
    }
}
