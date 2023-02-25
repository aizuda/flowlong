package test.query;

import com.flowlong.bpm.engine.access.Page;
import com.flowlong.bpm.engine.access.QueryFilter;
import com.flowlong.bpm.engine.entity.HisInstance;
import test.TestLongBase;
import org.junit.Test;

/**
 * 流程实例查询测试
 *
 * @author hubin
 * @since 1.0
 */
public class TestQueryHistInstance extends TestLongBase {
    @Test
    public void test() {
        System.out.println(engine.queryService().getHistoryInstances(
                new QueryFilter().setCreateTimeStart("2014-01-01").setName("simple").setState(0).setProcessType("预算管理流程1")));
        System.out.println(engine.queryService().getHistoryInstances(new Page<HisInstance>(), new QueryFilter()));
    }
}
