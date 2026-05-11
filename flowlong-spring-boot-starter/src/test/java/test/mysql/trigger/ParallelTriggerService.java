package test.mysql.trigger;

import com.aizuda.bpm.engine.TaskTrigger;
import com.aizuda.bpm.engine.core.Execution;
import com.aizuda.bpm.engine.model.NodeModel;

import java.util.function.Function;

// 并行分支中的触发器
public class ParallelTriggerService implements TaskTrigger {

    @Override
    public boolean execute(NodeModel nodeModel, Execution execution, Function<Execution, Boolean> finish) {
        System.out.println("执行了========================ParallelTriggerService===========================");
        return finish.apply(execution);
    }
}
