package test.mysql;

import com.aizuda.bpm.engine.TaskTrigger;
import com.aizuda.bpm.engine.core.Execution;
import com.aizuda.bpm.engine.model.NodeModel;

public class TaskTriggerImpl implements TaskTrigger {

    @Override
    public boolean execute(NodeModel nodeModel, Execution execution) {
        System.out.println("执行了触发器 args = " + nodeModel.getExtendConfig().get("args"));
        return true;
    }
}
