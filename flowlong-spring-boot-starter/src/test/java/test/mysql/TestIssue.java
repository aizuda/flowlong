package test.mysql;

import com.aizuda.bpm.engine.ProcessService;
import com.aizuda.bpm.engine.QueryService;
import com.aizuda.bpm.engine.TaskService;
import com.aizuda.bpm.engine.core.FlowCreator;
import com.aizuda.bpm.engine.entity.FlwHisInstance;
import com.aizuda.bpm.engine.entity.FlwInstance;
import com.aizuda.bpm.engine.entity.FlwProcess;
import com.aizuda.bpm.engine.entity.FlwTask;
import com.aizuda.bpm.engine.model.NodeAssignee;
import com.aizuda.bpm.engine.model.NodeModel;
import com.aizuda.bpm.mybatisplus.mapper.FlwProcessMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

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
     * 测试驳回至起始节点
     */
    @Test
    public void testRejectStartNode() {
        Long processId = this.deployByResource("test/conditionEnd.json", testCreator);

        // 启动发起
        flowLongEngine.startInstanceById(processId, test3Creator).ifPresent(instance -> executeActiveTasks(instance.getId(),
                flwTask -> flowLongEngine.taskService().rejectTask(flwTask, test2Creator,
                Collections.singletonMap("rejectReason", "不同意"))));
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
        flowLongEngine.startInstanceById(processId, test3Creator, new HashMap<String, Object>() {{
            put("hi", 123);
            put("go", "abc");
        }}).ifPresent(instance -> {
            FlwInstance flwInstance = flowLongEngine.queryService().getInstance(instance.getId());
            Assertions.assertEquals(flwInstance.variableToMap().get("hi"), 123);

            /*
             * 领导审批，debug 查看 {@link NodeModel#execute 方法 arg 参数是否传递}
             */
            this.executeActiveTasks(instance.getId(), testCreator, new HashMap<String, Object>() {{
                put("hi", 678);// 会覆盖实例参数 123
                put("day", 8);
            }});
        });
    }

    /**
     * <a href="https://gitee.com/aizuda/flowlong/issues/I9HTX1">测试加签节点存在多人时，当其中一人驳回异常问题</a>
     */
    @Test
    public void testI9HTX1() {
        final ProcessService processService = flowLongEngine.processService();
        final QueryService queryService = flowLongEngine.queryService();
        final TaskService taskService = flowLongEngine.taskService();

        // 部署流程
        Long processId = processService.deployByResource("test/issues_I9HTX1.json", testCreator, false);

        // 启动流程
        final Map<String, Object> args = new HashMap<>();
        args.put("yg", 1);
        args.put("money", 500);
        args.put("fl", 1);
        args.put("ftlb", 1);
        flowLongEngine.startInstanceById(processId, testCreator, args).ifPresent(flwInstance -> {

            // 部门经理
            FlowCreator shiYong = FlowCreator.of("370000197405268159", "石勇");
            this.executeTask(flwInstance.getId(), shiYong, flwTask -> flowLongEngine.executeTask(flwTask.getId(), shiYong));

            // 人资部薪资专员
            FlowCreator weiLei = FlowCreator.of("410000199512025445", "魏磊");
            this.executeTask(flwInstance.getId(), weiLei, flwTask -> flowLongEngine.executeTask(flwTask.getId(), weiLei));

            // 总裁办主任（加签）
            queryService.getActiveTasksByInstanceId(flwInstance.getId()).flatMap(flwTasks -> flwTasks.stream()
                    .filter(t -> Objects.equals("总裁办主任", t.getTaskName())).findFirst()).ifPresent(flwTask -> {
                NodeModel nodeModel = new NodeModel();
                nodeModel.setNodeName("人工选择，多人并审");
                nodeModel.setType(1);
                nodeModel.setSetType(1);
                nodeModel.setExamineMode(1);
                nodeModel.setNodeAssigneeList(Arrays.asList(NodeAssignee.ofFlowCreator(FlowCreator.of("45000019760731722X", "汤强")),
                        NodeAssignee.ofFlowCreator(FlowCreator.of("440000200407105727", "贾超"))));
                flowLongEngine.executeAppendNodeModel(flwTask.getId(), nodeModel, FlowCreator.of("640000200911201176", "邵涛"), true);
            });

            // 加签节点（审批）
            queryService.getActiveTasksByInstanceId(flwInstance.getId()).ifPresent(flwTasks -> {
                FlwTask flwTask = flwTasks.get(0);
                queryService.getTaskActorsByTaskId(flwTask.getId()).forEach(a -> {
                    if ("汤强".equals(a.getActorName())) {
                        flowLongEngine.executeTask(flwTask.getId(), FlowCreator.of(a.getActorId(), a.getActorName()));
                    } else {
                        taskService.rejectTask(flwTask, FlowCreator.of(a.getActorId(), a.getActorName()), Collections.singletonMap("rejectReason", "不同意"));
                    }
                });
            });
        });
    }
}
