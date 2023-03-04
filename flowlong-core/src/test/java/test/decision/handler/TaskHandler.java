package test.decision.handler;

import com.flowlong.bpm.engine.core.Execution;
import com.flowlong.bpm.engine.core.FlowLongContext;
import com.flowlong.bpm.engine.handler.DecisionHandler;

/**
 * TaskHandler
 *
 * @author yeluod
 * @since 1.0
 **/
public class TaskHandler implements DecisionHandler {

    @Override
    public String decide(FlowLongContext context, Execution execution) {
        return (String) execution.getArgs().get("content");
    }
}
