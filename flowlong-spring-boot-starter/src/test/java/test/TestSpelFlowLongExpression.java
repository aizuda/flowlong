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
        // long
        assertNumericalValue(">", "1030151717973013506", new HashMap<String, Object>() {{
            put("day", 1930151717973013506L);
        }});
        assertNumericalValue("<", "20", new HashMap<String, Object>() {{
            put("day", 19L);
        }});

        // int
        assertNumericalValue(">", "7");
        assertNumericalValue(">=", "8");
        assertNumericalValue("<=", "8");
        assertNumericalValue("<", "9");
        assertNumericalValue("!=", "3");
        assertNumericalValue(">", "7");

        // String
        assertStringValue("==", "张三");
        assertStringValue("include", "飞龙工作流张三王五都说好用");
        assertStringValue("notinclude", "李");

    }

    public void assertNumericalValue(String operator, String value) {
        assertNumericalValue(operator, value, null);
    }

    public void assertNumericalValue(String operator, String value, Map<String, Object> args) {
        NodeExpression nodeExpression = new NodeExpression();
        nodeExpression.setLabel("日期");
        nodeExpression.setField("day");
        nodeExpression.setOperator(operator);
        nodeExpression.setValue(value);
        Assertions.assertFalse(this.eval(nodeExpression, null));
        Assertions.assertTrue(this.eval(nodeExpression, null != args ? args : new HashMap<String, Object>() {{
            put("day", 8);
        }}));
    }

    public void assertStringValue(String operator, String value) {
        NodeExpression nodeExpression = new NodeExpression();
        nodeExpression.setLabel("姓名");
        nodeExpression.setField("name");
        nodeExpression.setOperator(operator);
        nodeExpression.setValue("张三");
        Assertions.assertFalse(this.eval(nodeExpression, null));
        Assertions.assertTrue(this.eval(nodeExpression, new HashMap<String, Object>() {{
            put("name", value);
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
