package test.mysql.task.interceptor;

import com.flowlong.bpm.engine.QueryService;
import com.flowlong.bpm.engine.entity.Instance;
import com.flowlong.bpm.engine.entity.Task;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import test.mysql.MysqlTest;

import java.util.List;

/**
 * Test Interceptor
 *
 * @author august
 */
@Slf4j
public class TestLocalInterceptor extends MysqlTest {

    @BeforeEach
    public void before() {
        processId = this.deployByResource("test/cc.long");
    }

    @Test
    public void test() {
        Instance instance = flowLongEngine.startInstanceById(processId, testUser1);
        QueryService queryService = flowLongEngine.queryService();
        List<Task> tasks = queryService.getActiveTasksByInstanceId(instance.getId());
        for (Task task : tasks) {
            flowLongEngine.executeTask(task.getId(), testUser1);
        }
    }
}
