package test.task.actor;

import com.flowlong.bpm.engine.FlowLongEngine;
import com.flowlong.bpm.engine.ProcessService;
import com.flowlong.bpm.engine.core.FlowLongContext;
import com.flowlong.bpm.engine.entity.Instance;
import com.flowlong.bpm.engine.entity.Process;
import com.flowlong.bpm.engine.entity.Task;
import com.flowlong.bpm.engine.model.TaskModel;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import test.TestFlowLong;
import test.mysql.MysqlTest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author shen tao tao
 * @date 2023/3/4 11:40
 * @since 1.0
 */
public class TestActor extends MysqlTest {

    @Test
    void test() {
        Task task = createTask();
        flowLongEngine.taskService().addTaskActor(task.getId(), 0, "test1", "test2");
        flowLongEngine.taskService().removeTaskActor(task.getId(), "test2");
    }

    private Task createTask() {
        Map<String, Object> args = new HashMap<String, Object>();
        args.put("task1.operator", new String[]{"1"});
        Long processId = this.deployByResource("test/task/simple/process.long");
        Instance instance = flowLongEngine.startInstanceById(processId, "test0", args);
        System.out.println("instance=" + instance);
        TaskModel tm1 = new TaskModel();
        tm1.setName("task1");
        tm1.setDisplayName("任务1");
        List<Task> tasks = flowLongEngine.createFreeTask(instance.getId(), "test0", args, tm1);
        return tasks.get(0);
    }
}
