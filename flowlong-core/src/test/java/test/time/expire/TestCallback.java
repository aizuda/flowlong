package test.time.expire;

import java.util.List;

import com.flowlong.bpm.engine.entity.Task;
import com.flowlong.bpm.engine.scheduling.JobCallback;
import lombok.extern.slf4j.Slf4j;
/**
 * @author wangzi
 * @Todo
 */

@Slf4j
public class TestCallback implements JobCallback {
	public void callback(String taskId, List<Task> newTasks) {
		log.info("callback taskId=" + taskId);
		log.info("newTasks=" + newTasks);
	}
}
