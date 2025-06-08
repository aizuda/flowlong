package test.mysql;

import com.aizuda.bpm.engine.FlowDataTransfer;
import com.aizuda.bpm.engine.entity.FlwHisInstance;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

public class TestConditionNode extends MysqlTest {

    @BeforeEach
    public void before() {
        processId = this.deployByResource("test/conditionEnd.json", testCreator);
    }

    @Test
    public void testKey() {
        // 启动发起
        flowLongEngine.startInstanceById(processId, test3Creator).ifPresent(instance -> {

            // 指定选择短期条件节点
            FlowDataTransfer.specifyConditionNodeKey("k007");

            // 人事审批
            this.executeActiveTasks(instance.getId(), test2Creator);

            FlwHisInstance hisInstance = flowLongEngine.queryService().getHistInstance(instance.getId());
            Assertions.assertEquals("人事审批", hisInstance.getCurrentNodeName());
        });
    }

    @Test
    public void testArgs() {
        // 启动发起
        flowLongEngine.startInstanceById(processId, test3Creator).ifPresent(instance -> {

            // 条件参数选择条件节点
            Map<String, Object> args = new HashMap<>();
            args.put("day", 8);

            // 移除testKey测试方法中指定选择短期条件节点参数
            FlowDataTransfer.remove();

            // 人事审批
            this.executeActiveTasks(instance.getId(), test2Creator, args);

            // 领导审批
            this.executeActiveTasks(instance.getId(), testCreator, args);

            FlwHisInstance hisInstance = flowLongEngine.queryService().getHistInstance(instance.getId());
            Assertions.assertEquals("领导审批结束", hisInstance.getCurrentNodeName());
        });
    }
}
