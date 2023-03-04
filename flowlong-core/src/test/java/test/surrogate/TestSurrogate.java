package test.surrogate;

import com.flowlong.bpm.engine.entity.Instance;
import org.junit.jupiter.api.Test;
import test.mysql.MysqlTest;

import java.util.HashMap;
import java.util.Map;

/**
 * 测试委托代理
 *
 * @author xlsea
 * @since 2023-03-04
 */
public class TestSurrogate extends MysqlTest {

    @Test
    public void test() {
        // 部署流程
        Long processId = this.deployByResource("test/surrogate/process.long");
        // 组装参数列表
        Map<String, Object> args = new HashMap<>();
        args.put("task1.operator", new String[]{"test"});
        // 启动流程实例
        Instance surrogate = flowLongEngine.startInstanceById(processId, "2", args);
        // 输出流程实例信息
        System.out.println("surrogate=" + surrogate);
        // List<Task> tasks = flowLongEngine.queryService().getTasksByInstanceId(surrogate.getId());
        // for (Task task : tasks) {
        //     // flowLongEngine.executeTask(task.getId(), "1", args);
        // }
    }

}
