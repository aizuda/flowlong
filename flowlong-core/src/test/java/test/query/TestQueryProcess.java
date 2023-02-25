package test.query;

import com.flowlong.bpm.engine.access.Page;
import com.flowlong.bpm.engine.access.QueryFilter;
import com.flowlong.bpm.engine.entity.Process;
import test.TestLongBase;
import org.junit.Test;

/**
 * 流程定义查询测试
 *
 * @author hubin
 * @since 1.0
 */
public class TestQueryProcess extends TestLongBase {
    @Test
    public void test() {
        System.out.println(engine.processService().getProcess(null));
        System.out.println(engine.processService().getProcess(new Page<Process>(),
                new QueryFilter().setName("subprocess1")));
        System.out.println(engine.processService().getProcessByVersion("subprocess1", 0));
        System.out.println(engine.processService().getProcessByName("subprocess1"));
    }
}
