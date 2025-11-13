package test.mysql.config;

import com.aizuda.bpm.engine.TaskCreateInterceptor;
import com.aizuda.bpm.engine.core.Execution;
import com.aizuda.bpm.engine.core.FlowLongContext;

public class TestAiTaskCreate implements TaskCreateInterceptor {

    @Override
    public void after(FlowLongContext flowLongContext, Execution execution) {

    }
}
