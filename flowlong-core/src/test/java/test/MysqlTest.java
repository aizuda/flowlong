package test;

import com.flowlong.bpm.engine.FlowLongEngine;
import com.flowlong.bpm.engine.RuntimeService;
import com.flowlong.bpm.engine.entity.Instance;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.HashMap;
import java.util.Map;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(locations = {"classpath:spring-test-mysql.xml"})
public class MysqlTest {

    @Autowired
    private FlowLongEngine flowLongEngine;
    /**
     * 流程ID
     */
    protected Long processId;

    protected void deployByResource(String resourceName) {
        this.processId = flowLongEngine.processService().deployByResource(resourceName);
    }

    @BeforeEach
    public void before() {
        this.deployByResource("test/cc/process.long");
    }

    @Test
    public void test() {
        Map<String, Object> args = new HashMap<>();
        // 设置工作流任务节点 assignee 属性
        args.put("task1.operator", new String[]{"1"});
        Instance instance = flowLongEngine.startInstanceByName("simple", 1, "2", args);
        RuntimeService runtimeService = flowLongEngine.runtimeService();
        // 创建抄送实例，暂时先 debug 观察数据库表结构数据变化
        final String actorId = "1000";
        runtimeService.createCCInstance(instance.getId(), "test", actorId);
		runtimeService.updateCCStatus(instance.getId(), actorId);
		runtimeService.deleteCCInstance(instance.getId(), actorId);
    }

}
