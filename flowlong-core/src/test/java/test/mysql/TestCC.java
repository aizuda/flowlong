package test.mysql;

import com.flowlong.bpm.engine.RuntimeService;
import com.flowlong.bpm.engine.entity.Instance;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import test.TestFlowLong;

import java.util.HashMap;
import java.util.Map;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(locations = {"classpath:spring-test-mysql.xml"})
public class TestCC extends TestFlowLong {

    /**
     * 抄送测试
     */
    @Test
    public void testCc() {
        Long processId = this.deployByResource("test/cc/process.long");
        System.out.println("流程定义ID = " + processId);
        Map<String, Object> args = new HashMap<>();
        // 设置工作流任务节点 assignee 属性
        args.put("task1.assignee", "1");
        Instance instance = flowLongEngine.startInstanceByName("simple", 1, "testUser", args);
        RuntimeService runtimeService = flowLongEngine.runtimeService();
        // 创建抄送实例，暂时先 debug 观察数据库表结构数据变化
        final String actorId = "1000";
        runtimeService.createCCInstance(instance.getId(), "test", actorId);
        runtimeService.updateCCStatus(instance.getId(), actorId);
        runtimeService.deleteCCInstance(instance.getId(), actorId);
    }
}
