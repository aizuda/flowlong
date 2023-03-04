package test.task.field;

import com.flowlong.bpm.engine.ProcessService;
import com.flowlong.bpm.engine.model.NodeModel;
import com.flowlong.bpm.engine.model.ProcessModel;
import com.flowlong.bpm.engine.model.TaskModel;
import org.junit.jupiter.api.Test;
import test.mysql.MysqlTest;

/**
 * 测试获取任务field
 *
 * @author lipanre
 * @date 2023/3/4 12:04
 */
public class FieldTest extends MysqlTest {

    /**
     * 测试获取流程Field
     */
    @Test
    public void testField() {
        Long processId = this.deployByResource("test/task/field/process.long");
        ProcessModel processModel = flowLongEngine.processService().getProcessById(processId).getProcessModel();
        // 获取名称为task1的task
        TaskModel taskModel = (TaskModel) processModel.getNode("task1");

        // 获取task下面的field
        System.out.println("taskModel.getFields() : " + taskModel.getFields());
    }

}
