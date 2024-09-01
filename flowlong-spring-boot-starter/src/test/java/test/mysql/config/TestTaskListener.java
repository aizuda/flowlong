package test.mysql.config;

import com.aizuda.bpm.engine.FlowLongEngine;
import com.aizuda.bpm.engine.core.FlowCreator;
import com.aizuda.bpm.engine.core.enums.EventType;
import com.aizuda.bpm.engine.core.enums.NodeApproveSelf;
import com.aizuda.bpm.engine.core.enums.NodeSetType;
import com.aizuda.bpm.engine.entity.FlwTask;
import com.aizuda.bpm.engine.listener.TaskListener;
import com.aizuda.bpm.engine.model.NodeAssignee;
import com.aizuda.bpm.engine.model.NodeModel;
import lombok.AllArgsConstructor;

import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * 测试任务监听处理器
 */
@AllArgsConstructor
public class TestTaskListener implements TaskListener {
    private FlowLongEngine flowLongEngine;

    @Override
    public boolean notify(EventType eventType, Supplier<FlwTask> supplier, NodeModel nodeModel,
                          FlowCreator flowCreator) {
        if (EventType.create.eq(eventType)) {
            // 创建任务时候，判断是否自动审批通过
            Integer approveSelf = nodeModel.getApproveSelf();
            if (NodeApproveSelf.AutoSkip.eq(approveSelf)) {
                FlwTask flwTask = supplier.get();
                if (NodeSetType.specifyMembers.eq(nodeModel.getSetType())) {
                    // 普通成员情况
                    List<NodeAssignee> nodeAssigneeList = nodeModel.getNodeAssigneeList();
                    if (nodeAssigneeList.stream().anyMatch(t -> Objects.equals(t.getId(), flowCreator.getCreateId()))) {
                        return flowLongEngine.autoJumpTask(flwTask.getId(), flowCreator);
                    }
                } else if (NodeSetType.role.eq(nodeModel.getSetType())) {
                    // 角色情况
                    flowLongEngine.queryService().getActiveTaskActorsByTaskId(flwTask.getId()).flatMap(flwTaskActorList -> flwTaskActorList.stream()
                            .filter(t -> Objects.equals(t.getActorId(), flowCreator.getCreateId()))
                            .findFirst()).ifPresent(taskActor -> flowLongEngine.autoJumpTask(taskActor.getTaskId(), flowCreator));
                    return true;
                }
            }
        }
        return true;
    }
}
