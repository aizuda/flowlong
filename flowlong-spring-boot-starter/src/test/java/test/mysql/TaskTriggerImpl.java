package test.mysql;

import com.aizuda.bpm.engine.TaskTrigger;
import com.aizuda.bpm.engine.core.Execution;
import com.aizuda.bpm.engine.model.NodeModel;

import java.util.function.Function;

public class TaskTriggerImpl implements TaskTrigger {

    @Override
    public boolean execute(NodeModel nodeModel, Execution execution, Function<Execution, Boolean> finish) {

//        // 设置为 true 不会继续往下执行，触发器任务保存流程堵塞
//        execution.setSaveAsDraft(true);
//        boolean ok = finish.apply(execution);
//        if (ok) {
//            FlwTask flwTask = execution.getFlwTasks().get(0);
//            if (null != flwTask) {
//                Long flwTaskId = flwTask.getId();
//                // 触发器任务ID保存到业务中，待业务执行完成调用 flowlongEngine.executeFinishTrigger 结束触发器，继续执行流程
//                System.out.println("触发器任务ID = " + flwTaskId);
//            }
//        }
//        return ok;

        System.out.println("执行了触发器 args = " + nodeModel.getExtendConfig().get("args"));
        return finish.apply(execution);
    }
}
