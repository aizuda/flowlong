package test.mysql.task;

import com.flowlong.bpm.engine.entity.Instance;
import com.flowlong.bpm.engine.entity.Task;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import test.mysql.MysqlTest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 测试驳回测试类
 * <p>
 * 尊重知识产权，CV 请保留版权，爱组搭 http://aizuda.com 出品
 * </p>
 *
 * @author Binfeng.Yan
 * @since 1.0
 */
public class TestRetreat extends MysqlTest {

    @BeforeEach
    public void before() {
        processId = this.deployByResource("test/task/retreat.long");
    }

    @Test
    public void test() {
        Instance instance = flowLongEngine.startInstanceById(processId, testUser1);
        Assertions.assertNotNull(instance);

        List<Task> taskList = flowLongEngine.queryService().getActiveTasksByInstanceId(instance.getId());
        Assertions.assertTrue(taskList.size() == 1);

        // 任务1
		Task task1 = taskList.get(0);
        Assertions.assertEquals(task1.getTaskName(), "task1");
        Map<String, Object> args = new HashMap<>();
        args.put("number", 2);

		// 任务2
        Task task2 = flowLongEngine.executeTask(task1.getId(), testUser1, args).get(0);
        Assertions.assertEquals(task2.getTaskName(), "task2");

		// 任务3
        Task task3 = flowLongEngine.executeTask(task2.getId(), testUser1, args).get(0);
        Assertions.assertEquals(task3.getTaskName(), "task3");

		// 回退任务1
        flowLongEngine.retreatTask(task3.getId(), testUser1, args, "task1");
		List<Task> task1List = flowLongEngine.queryService().getActiveTasksByInstanceId(instance.getId());
		Assertions.assertEquals(task1List.get(0).getTaskName(), "task1");
    }

}
