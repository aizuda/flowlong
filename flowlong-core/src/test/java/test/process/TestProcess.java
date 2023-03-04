package test.process;

import com.flowlong.bpm.engine.entity.Process;
import org.junit.jupiter.api.Test;
import test.TestFlowLong;
import test.mysql.MysqlTest;

import java.util.HashMap;
import java.util.Map;

/**
 * 类名称：TestProcess
 * <p>
 * 描述：
 * 创建人：xdg
 * 创建时间：2023-03-04 10:58
 */
public class TestProcess extends MysqlTest {
    @Test
    public void test() {
        Long processId = this.deployByResource("test/task/simple/process.long");

        Process process = flowLongEngine.processService().getProcessById(processId);

        System.out.println("output 1=" + process);

        process = flowLongEngine.processService().getProcessByVersion(process.getName(), process.getVersion());

        System.out.println("output 2="+process);

        Map<String, Object> args = new HashMap<String, Object>();

        args.put("task1.operator", "1");

        flowLongEngine.startInstanceById(processId, "1", args);
        flowLongEngine.processService().undeploy(processId);
    }
}
