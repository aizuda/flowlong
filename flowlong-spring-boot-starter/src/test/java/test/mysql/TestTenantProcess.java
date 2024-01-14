package test.mysql;

import com.flowlong.bpm.engine.core.FlowCreator;
import org.junit.jupiter.api.Test;

/**
 * 测试租户逻辑
 */
public class TestTenantProcess extends TestSubProcess {

    @Test
    public void testTenant() {
        this.testProcess(false, 8);
    }

    @Override
    public FlowCreator getFlowCreator() {
        return FlowCreator.of("1000", testUser1, "测试001");
    }
}
