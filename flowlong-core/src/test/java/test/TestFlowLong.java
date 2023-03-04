package test;

import com.flowlong.bpm.engine.FlowLongEngine;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class TestFlowLong {

    @Autowired
    protected FlowLongEngine flowLongEngine;

    protected Long deployByResource(String resourceName) {
        return flowLongEngine.processService().deployByResource(resourceName, false);
    }
}
