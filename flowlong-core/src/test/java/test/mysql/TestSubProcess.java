package test.mysql;

import com.flowlong.bpm.engine.core.FlowLongContext;
import com.flowlong.bpm.engine.entity.Instance;
import com.flowlong.bpm.engine.entity.Task;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
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

    /**
     * 子流程关联父流程
     */
    @Test
    public void testRelation() {
        // 创建父流程
        Long processId = this.deployByResource("test/task/simple.long");
        System.out.println("父流程id:" + processId);
        // 组装参数列表
        Map<String, Object> args = new HashMap<>();
        args.put("task1.assignee", Arrays.asList(testUser3));
        // 启动父流程实例
        Instance surrogate = flowLongEngine.startInstanceById(processId, testUser1, args);
        Assertions.assertNotNull(surrogate);

        // 创建子流程
        Long serialChildProcessId = this.deployByResource("test/subprocess/serial-subprocess.long");
        System.out.println("串行行子流程ID = " + serialChildProcessId);

        // 创建子流程实例 传入父流程实例ID做关联
        Instance instance = flowLongEngine.startInstanceByIdAndParentId(serialChildProcessId, testUser1, null, surrogate.getId(), surrogate.getParentNodeName());
        // print: {"parentId":${父流程id}}
        System.out.println("子流程实例：" + FlowLongContext.JSON_HANDLER.toJson(instance));
    }

}
