package test.mysql.config;

import com.aizuda.bpm.engine.core.Execution;
import com.aizuda.bpm.engine.core.FlowCreator;
import com.aizuda.bpm.engine.core.FlowLongContext;
import com.aizuda.bpm.engine.entity.FlwTask;
import com.aizuda.bpm.engine.handler.FlowAiHandler;
import com.aizuda.bpm.engine.model.NodeModel;
import test.mysql.TestAiHandle;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class TestAiHandler implements FlowAiHandler {
    // 创建一个 AI 智能体处理用户
    public static final FlowCreator aiUser = new FlowCreator("1", "AI智能体");

    @Override
    public boolean handle(FlowLongContext flowLongContext, Execution execution, NodeModel nodeModel) {
        this.print(nodeModel, "handle");
        // 这里忽略AI分析部分逻辑，真实业务自行添加 ...
        for (FlwTask ft : execution.getFlwTasks()) {
            // 模拟 AI 智能体自动完成审批
            flowLongContext.getFlowLongEngine().autoCompleteTask(ft.getId(), aiUser);
        }
        return true;
    }

    @Override
    public Map<String, Object> getArgs(FlowLongContext flowLongContext, Execution execution, NodeModel nodeModel, Map<String, Object> args) {
        this.print(nodeModel, "getArgs");
        Map<String, Object> aiArgs = new HashMap<>();
        // 模拟 AI 识别文本返回 day 参数一天为 7 天
        if (Objects.equals(args.get("content"), TestAiHandle.CONTENT)) {
            aiArgs.put("day", 7);
        }
        return aiArgs;
    }

    private void print(NodeModel nodeModel, String method) {
        System.out.println(method + "，根据 callAi 识别处理具体逻辑：" + nodeModel.getCallAi());
    }
}
