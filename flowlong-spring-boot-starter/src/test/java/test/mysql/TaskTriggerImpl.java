package test.mysql;

import com.aizuda.bpm.engine.TaskTrigger;
import com.aizuda.bpm.engine.core.Execution;
import com.aizuda.bpm.engine.core.FlowCreator;
import com.aizuda.bpm.engine.core.enums.TaskType;
import com.aizuda.bpm.engine.model.NodeModel;

import java.util.function.Function;

public class TaskTriggerImpl implements TaskTrigger {

    @Override
    public boolean execute(NodeModel nodeModel, Execution execution, Function<Execution, Boolean> finish) {

//        // 触发器任务为暂存状态。
//        boolean ok = finish.apply(execution.saveAsDraft());
//        if (ok) {
//            FlwTask flwTask = execution.getFlwTask();
//            if (null != flwTask) {
//                Long flwTaskId = flwTask.getId();
//                // 触发器任务ID保存到业务中，待业务执行完成调用 flowlongEngine.executeFinishTrigger 执行完成触发器任务，继续执行流程进入下一个节点。
//                System.out.println("触发器任务ID = " + flwTaskId);
//            }
//        }
//        return ok;

        if (execution.argsEquals("jump2k001", "1")) {

            // 测试触发器内部跳转逻辑，先执行完成触发器
            boolean ok = finish.apply(execution.finishJump());
            if (ok) {
                // 跳转回发起人节点
                ok = SpringHelper.getFlowLongEngine().executeJumpTask(execution.getFlwTask().getId(), "k001",
                        FlowCreator.of("test001", "测试001"), null, TaskType.triggerJump).isPresent();
            }
            return ok;
        }

        System.out.println("执行了触发器 args = " + nodeModel.getExtendConfig().get("args"));
        return finish.apply(execution);
    }
}
