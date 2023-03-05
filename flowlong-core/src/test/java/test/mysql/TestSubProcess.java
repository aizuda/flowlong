package test.mysql;

import com.flowlong.bpm.engine.entity.Instance;
import com.flowlong.bpm.engine.entity.Task;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 测试子流程
 *
 * @author ximu
 */
public class TestSubProcess extends MysqlTest {

    @Test
    public void testParallel() {
        // 创建子流程和并行子流程
        Long childProcessId = this.deployByResource("test/subprocess/child.long");
        System.out.println("子流程ID = " + childProcessId);
        Long parallelChildProcessId = this.deployByResource("test/subprocess/parallel-subprocess.long");
        System.out.println("并行子流程ID = " + parallelChildProcessId);

        // 设置工作流任务节点 assignee 属性
        Map<String, Object> args = new HashMap<>(2);
        args.put("task1.assignee", "1");
        // 创建流程实例
        Instance instance = flowLongEngine.startInstanceById(parallelChildProcessId, "createUserName", args);
        Long id = instance.getId();
        System.out.println("流程实例ID = " + id);
        List<Task> tasks = flowLongEngine.queryService().getTasksByInstanceId(id);
        for (Task task : tasks) {
            System.out.println("************************begin:::::" + task);
            flowLongEngine.executeTask(task.getId(), "1", args);
            System.out.println("************************end:::::" + task);
        }
    }

    @Test
    public void testSerial() {
        // 创建子流程和并行子流程
        Long childProcessId = this.deployByResource("test/subprocess/child.long");
        System.out.println("子流程ID = " + childProcessId);
        Long serialChildProcessId = this.deployByResource("test/subprocess/serial-subprocess.long");
        System.out.println("串行行子流程ID = " + serialChildProcessId);

        // 设置工作流任务节点 assignee 属性
        Map<String, Object> args = new HashMap<>(2);
        args.put("task1.assignee", "1");
        // 创建流程实例
        Instance instance = flowLongEngine.startInstanceById(serialChildProcessId, "createUserName", args);
        Long id = instance.getId();
        System.out.println("流程实例ID = " + id);
        List<Task> tasks = flowLongEngine.queryService().getTasksByInstanceId(id);
        for (Task task : tasks) {
            System.out.println("************************begin:::::" + task);
            flowLongEngine.executeTask(task.getId(), "1", args);
            System.out.println("************************end:::::" + task);
        }
    }
}
