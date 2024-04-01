package test;

import com.aizuda.bpm.engine.model.NodeExpression;
import com.aizuda.bpm.spring.adaptive.SpelExpression;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class TestSpelExpression {

    @Test
    public void test() {
        SpelExpression expression = new SpelExpression();
        Map<String, Object> args = new HashMap<>();
        args.put("day", 8);
        NodeExpression nodeExpression = new NodeExpression();
        nodeExpression.setLabel("日期");
        nodeExpression.setField("day");
        nodeExpression.setOperator(">");
        nodeExpression.setValue("7");
        Assertions.assertTrue(expression.eval(Collections.singletonList(Collections.singletonList(nodeExpression)), args));
    }
}
