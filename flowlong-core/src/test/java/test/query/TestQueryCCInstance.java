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
public class TestQueryCCInstance extends TestLongBase {
    @Test
    public void test() {
        Page<HisInstance> page = new Page<HisInstance>();
        System.out.println(engine.queryService().getCCWorks(page, new QueryFilter().setState(1)));
    }
}
