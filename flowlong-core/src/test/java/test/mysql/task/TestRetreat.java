package test.mysql.task;

import com.flowlong.bpm.engine.entity.Instance;
import com.flowlong.bpm.engine.entity.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import test.mysql.MysqlTest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
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
		Map<String, Object> args = new HashMap<String, Object>();
		args.put("number", 2);
		Instance instance = flowLongEngine.startInstanceById(processId, "creator");
		List<Task> tasks = flowLongEngine.queryService().getActiveTasksByInstanceId(instance.getId());
		Task firstTask = flowLongEngine.executeTask(tasks.get(0).getId(), "creator", args).get(0);
		Task secondTask = flowLongEngine.executeTask(firstTask.getId(), "creator", args).get(0);
		flowLongEngine.retreatTask(secondTask.getId(), "creator", args, "task1");
	}

}
