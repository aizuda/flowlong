package test.mysql;

import com.aizuda.bpm.engine.TaskTrigger;
import com.aizuda.bpm.engine.core.Execution;
import com.aizuda.bpm.engine.model.NodeModel;

import java.util.function.Function;

public class TaskTriggerImpl implements TaskTrigger {

    @Override
    public boolean execute(NodeModel nodeModel, Execution execution, Function<Execution, Boolean> finish) {

//        // 设置为 true 流程不会继续执行进入下一节点，触发器任务为暂存状态。
//        execution.setSaveAsDraft(true);
//        boolean ok = finish.apply(execution);
//        if (ok) {
//            FlwTask flwTask = execution.getFlwTask();
//            if (null != flwTask) {
//                Long flwTaskId = flwTask.getId();
//                // 触发器任务ID保存到业务中，待业务执行完成调用 flowlongEngine.executeFinishTrigger 执行完成触发器任务，继续执行流程进入下一个节点。
//                System.out.println("触发器任务ID = " + flwTaskId);
//            }
//        }
//        return ok;

        System.out.println("执行了触发器 args = " + nodeModel.getExtendConfig().get("args"));
        return finish.apply(execution);
    }
}
