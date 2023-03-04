package test.task.take;

import com.flowlong.bpm.engine.entity.Instance;
import com.flowlong.bpm.engine.entity.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import test.mysql.MysqlTest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Description: TestTake
 * @Author: Binfeng.Yan
 * @Date: 2023/3/4 15:24
 * @Version: 1.0
 */
public class TestTake extends MysqlTest {

	@BeforeEach
	public void before() {
		processId = this.deployByResource("test/task/simple/process.long");
	}
	
	/**
	 * Description: 测试任务提取（测试任务分配给预处理人）
	 * @return void
	 * @author Binfeng.Yan
	 * @date 2023/3/4 16:27
	 */
	@Test
	public void test() {
		Map<String, Object> args = new HashMap<String, Object>();
		args.put("task1.operator", new String[]{"1"});
		Instance instance = flowLongEngine.startInstanceById(processId, "2", args);
		System.out.println("instance=" + instance);

		List<Task> tasks = flowLongEngine.queryService().getActiveTasksByInstanceId(instance.getId());
		for(Task task : tasks) {
			//flowLongEngine.executeTask(task.getId(), "1");
			flowLongEngine.taskService().take(task.getId(), "1");
		}
	}

}
