package test.decision.expression;

import com.flowlong.bpm.engine.entity.Instance;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import test.TestFlowLong;

import java.util.HashMap;
import java.util.Map;

/**
 * 测试决策分支流程1：决策节点decision使用expr属性决定后置路线
 *
 * @author yeluod
 * @since 1.0
 **/
@ExtendWith(SpringExtension.class)
@ContextConfiguration(locations = {"classpath:spring-test-mysql.xml"})
class TestDecision1 extends TestFlowLong {

    private Long processId;

    @BeforeEach
    void deployByResource() {
        this.processId = super.deployByResource("test/decision/expression/process.long");
    }

    @Test
    void taskTest() {
        Map<String, Object> args = new HashMap<>(16);
        args.put("task1.operator", new String[]{"1","2"});
        // args.put("task2.operator", new String[]{"1","2"});
        // args.put("task3.operator", new String[]{"1","2"});
        args.put("content", "toTask1");
        Instance instance = super.flowLongEngine.startInstanceById(processId, "2", args);
        System.out.println("instance = " + instance);
        Assertions.assertNotNull(instance);
    }



}
