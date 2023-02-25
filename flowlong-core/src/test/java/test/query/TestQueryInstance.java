package test.query;

import com.flowlong.bpm.engine.access.Page;
import com.flowlong.bpm.engine.access.QueryFilter;
import com.flowlong.bpm.engine.entity.Instance;
import test.TestLongBase;
import org.junit.Test;

/**
 * 流程实例查询测试
 *
 * @author hubin
 * @since 1.0
 */
public class TestQueryInstance extends TestLongBase {

    @Test
    public void test() {
        Page<Instance> page = new Page<Instance>();
        System.out.println(engine.queryService().getActiveInstances(
                new QueryFilter().setCreateTimeStart("2014-01-01").setProcessId("860e5edae536495a9f51937f435a1c01")));
        System.out.println(engine.queryService().getActiveInstances(page, new QueryFilter()));
        System.out.println(engine.queryService().getInstance("b2802224d75d4847ae5bfb0f7e621b8f"));
    }
}
