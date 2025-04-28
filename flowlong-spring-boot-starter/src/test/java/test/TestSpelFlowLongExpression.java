package test;

import com.aizuda.bpm.engine.model.NodeExpression;
import com.aizuda.bpm.spring.adaptive.SpelFlowLongExpression;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class TestSpelFlowLongExpression {

    @Test
    public void test() {
        NodeExpression nodeExpression = new NodeExpression();
        nodeExpression.setLabel("日期");
        nodeExpression.setField("day");
        nodeExpression.setOperator(">");
        nodeExpression.setValue("7");
        Assertions.assertFalse(this.eval(nodeExpression, null));
        Assertions.assertTrue(this.eval(nodeExpression, new HashMap<String, Object>() {{
            put("day", 8);
        }}));
    }

    @Test
    public void testEqual() {
        NodeExpression nodeExpression = new NodeExpression();
        nodeExpression.setLabel("姓名");
        nodeExpression.setField("name");
        nodeExpression.setOperator("==");
        nodeExpression.setValue("张三");
        Assertions.assertFalse(this.eval(nodeExpression, null));
        Assertions.assertTrue(this.eval(nodeExpression, new HashMap<String, Object>() {{
            put("name", "张三");
        }}));
    }

    private boolean eval(NodeExpression nodeExpression, Map<String, Object> args) {
        SpelFlowLongExpression expression = new SpelFlowLongExpression();
        if (null == args) {
            args = Collections.emptyMap();
        }
        return expression.eval(Collections.singletonList(Collections.singletonList(nodeExpression)), args);
    }
}
