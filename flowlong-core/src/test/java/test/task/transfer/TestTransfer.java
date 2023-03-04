package test.task.transfer;

import com.flowlong.bpm.engine.entity.Instance;
import com.flowlong.bpm.engine.entity.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import test.mysql.MysqlTest;

import java.util.List;

/**
 * @Description: TestTransfer
 * @Author: Binfeng.Yan
 * @Date: 2023/3/4 16:26
 * @Version: 1.0
 */
public class TestTransfer extends MysqlTest {

	@BeforeEach
	public void before() {
		processId = this.deployByResource("test/task/transfer/process.long");
	}

	/**
	 * Description: 转派任务
	 * @return void
	 * @author Binfeng.Yan
	 * @date 2023/3/4 17:10
	 */
	@Test
	public void test() {
		Instance instance = flowLongEngine.startInstanceByName("transfer", 1);
		System.out.println("instance=" + instance);
		List<Task> tasks = flowLongEngine.queryService().getActiveTasksByInstanceId(instance.getId());
		for(Task task : tasks) {
			flowLongEngine.taskService().createNewTask(task.getId(), 0, "test");
			flowLongEngine.taskService().complete(task.getId());
		}
	}


}
