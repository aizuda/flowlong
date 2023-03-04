package test.task.right;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.flowlong.bpm.engine.FlowLongEngine;
import com.flowlong.bpm.engine.core.mapper.TaskMapper;
import com.flowlong.bpm.engine.entity.Instance;
import com.flowlong.bpm.engine.entity.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import test.mysql.MysqlTest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 类名称：TestNotAllow
 * <p>
 * 描述：测试无权限执行任务
 * 创建人：xdg
 * 创建时间：2023-03-04 13:03
 */
public class TestNotAllow extends MysqlTest {
    private Long processId;

    @BeforeEach
    public void before() {
        processId = this.deployByResource("test/task/right/process.long");
    }

    @Test
    public void test() {
        Map<String, Object> args = new HashMap<String, Object>();
        args.put("task1.operator", new String[]{"2"});

        Instance instance = flowLongEngine.startInstanceById(processId, "2", args);
        System.out.println(instance);

        List<Task> taskList = flowLongEngine.queryService().getActiveTasksByInstanceId(instance.getId());

        for (Task task : taskList) {
            flowLongEngine.executeTask(task.getId(), FlowLongEngine.ADMIN, args);
        }
    }
}
