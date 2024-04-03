package test.mysql;

import com.aizuda.bpm.engine.entity.FlwHisInstance;
import com.aizuda.bpm.engine.entity.FlwInstance;
import com.aizuda.bpm.engine.entity.FlwProcess;
import com.aizuda.bpm.mybatisplus.mapper.FlwProcessMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.Map;

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
     * <a href="https://gitee.com/aizuda/flowlong/issues/I8MVO7">驳回发起人测试</a>
     */
    @Test
    public void testRejectTask() {
        Long processId = this.deployByResource("test/subProcess.json", testCreator);

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

    /**
     * <a href="https://gitee.com/aizuda/flowlong/issues/I8VGPC">驳回发起人测试</a>
     */
    @Test
    public void testConditionEnd() {
        Long processId = this.deployByResource("test/conditionEnd.json", testCreator);

        // 启动发起
        flowLongEngine.startInstanceById(processId, test3Creator).ifPresent(instance -> {

            // 人事审批
            Map<String, Object> args = new HashMap<>();
            args.put("day", 1);
            this.executeActiveTasks(instance.getId(), test2Creator, args);
        });
    }

    /**
     * 测试：记录不同的结束节点
     */
    @Test
    public void testEnd() {
        Long processId = this.deployByResource("test/conditionEnd.json", testCreator);

        // 启动发起
        flowLongEngine.startInstanceById(processId, test3Creator).ifPresent(instance -> {

            // 人事审批
            Map<String, Object> args = new HashMap<>();
            args.put("day", 8);
            this.executeActiveTasks(instance.getId(), test2Creator, args);

            this.executeActiveTasks(instance.getId(), testCreator, args);

            FlwHisInstance histInstance = flowLongEngine.queryService().getHistInstance(instance.getId());
            Assertions.assertEquals("领导审批结束", histInstance.getCurrentNode());
        });
    }

    /**
     * <a href="https://gitee.com/aizuda/flowlong/issues/I8WBFL">终止任务测试</a>
     */
    @Test
    public void testRevoke() {
        Long processId = this.deployByResource("test/conditionEnd.json", testCreator);

        // 启动发起
        flowLongEngine.startInstanceById(processId, test3Creator).ifPresent(instance -> {

            // 流程实例强制终止
            flowLongEngine.runtimeService().revoke(instance.getId(), test3Creator);
        });
    }

    /**
     * <a href="https://gitee.com/aizuda/flowlong/issues/I8YVHC">FlwInstance 获取 variable 变量值</a>
     */
    @Test
    public void testFlwInstanceVariable() {
        Long processId = this.deployByResource("test/purchase.json", testCreator);

        // 启动发起
        flowLongEngine.startInstanceById(processId, test3Creator, new HashMap<String, Object>(){{
            put("hi", 123);
            put("go", "abc");
        }}).ifPresent(instance -> {
            FlwInstance flwInstance = flowLongEngine.queryService().getInstance(instance.getId());
            Assertions.assertEquals(flwInstance.variableToMap().get("hi"), 123);

            /*
             * 领导审批，debug 查看 {@link NodeModel#execute 方法 arg 参数是否传递}
             */
            this.executeActiveTasks(instance.getId(), testCreator, new HashMap<String, Object>(){{
                put("hi", 678);// 会覆盖实例参数 123
                put("day", 8);
            }});
        });
    }
}
