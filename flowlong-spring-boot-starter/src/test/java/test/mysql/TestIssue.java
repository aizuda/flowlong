package test.mysql;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.flowlong.bpm.engine.entity.FlwProcess;
import com.flowlong.bpm.mybatisplus.mapper.FlwProcessMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * issue 问题测试
 */
public class TestIssue extends MysqlTest {

    @Autowired
    protected FlwProcessMapper flwProcessMapper;

    /**
     * 测试流程分页查询
     */
    @Test
    public void testFlwProcessPage() {
        LambdaQueryWrapper<FlwProcess> lqw = Wrappers.<FlwProcess>lambdaQuery()
                .like(FlwProcess::getProcessName, "审批")
                .orderByDesc(FlwProcess::getSort);
        Page<FlwProcess> page = flwProcessMapper.selectPage(Page.of(1, 10), lqw);
        Assertions.assertTrue(page.getPages() >= 0);
    }

    /**
     * 驳回发起人测试
     * <p>
     * https://gitee.com/aizuda/flowlong/issues/I8MVO7
     * </p>
     */
    @Test
    public void testRejectTask() {
        Long processId = this.deployByResource("test/process.json", testCreator);

        // 启动指定流程定义ID启动流程实例
        flowLongEngine.startInstanceById(processId, test3Creator).ifPresent(instance -> {

            // 测试拒绝任务至 test3
            this.executeActiveTasks(instance.getId(), flwTask -> {

                // test 拒绝
                flowLongEngine.taskService().rejectTask(flwTask, testCreator);
            });

            // 当前审批驳回到发起人，发起人审批
            this.executeActiveTasks(instance.getId(), test3Creator);

            // 流转到人事审批
        });
    }
}
