package test;

import com.aizuda.bpm.engine.model.NodeExpression;
import com.aizuda.bpm.spring.adaptive.SpelFlowLongExpression;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestSpelFlowLongExpression {

    @Test
    public void testNumericComparison() {
        assertCondition("day", ">", "1030151717973013506", mapOf("day", 1930151717973013506L), true);
        assertCondition("day", "<", "20", mapOf("day", 19L), true);
        assertCondition("day", ">", "7", mapOf("day", 8), true);
        assertCondition("day", ">=", "8", mapOf("day", 8), true);
        assertCondition("day", "<=", "8", mapOf("day", 8), true);
        assertCondition("day", "<", "9", mapOf("day", 8), true);
        assertCondition("day", "!=", "3", mapOf("day", 8), true);
    }

    @Test
    public void testScalarIncludeUsesEquality() {
        String candidate = "李伟";
        Assertions.assertTrue(eval(expression("name", "include", candidate), mapOf("name", candidate)));
        Assertions.assertFalse(eval(expression("name", "include", candidate), mapOf("name", candidate + "民")));
        Assertions.assertTrue(eval(expression("name", "notinclude", candidate), mapOf("name", candidate + "民")));
        Assertions.assertFalse(eval(expression("name", "notinclude", candidate), mapOf("name", candidate)));
    }

    @Test
    public void testNumericScalarInclude() {
        NodeExpression include = expression("userId", "include", "1001");
        NodeExpression notInclude = expression("userId", "notinclude", "1001");

        Assertions.assertTrue(eval(include, mapOf("userId", 1001L)));
        Assertions.assertFalse(eval(include, mapOf("userId", 1002L)));
        Assertions.assertTrue(eval(notInclude, mapOf("userId", 1002L)));
        Assertions.assertFalse(eval(notInclude, mapOf("userId", 1001L)));
        Assertions.assertTrue(eval(include, mapOf("userId", 1001)));
    }

    @Test
    public void testCollectionContains() {
        NodeExpression include = expression("tags", "include", "important");
        NodeExpression notInclude = expression("tags", "notinclude", "critical");
        Map<String, Object> args = mapOf("tags", Arrays.asList("normal", "important", "urgent"));

        Assertions.assertTrue(eval(include, args));
        Assertions.assertTrue(eval(notInclude, args));
        Assertions.assertFalse(eval(expression("tags", "include", "imp"), args));
    }

    @Test
    public void testDateComparison() {
        String condition = "2026-07-01 12:00:00";
        Date startTime = Date.from(LocalDateTime.of(2026, 7, 1, 12, 0)
                .atZone(ZoneId.systemDefault()).toInstant());

        Assertions.assertTrue(eval(expression("startTime", ">=", condition), mapOf("startTime", startTime)));
        Assertions.assertFalse(eval(expression("startTime", ">", condition), mapOf("startTime", startTime)));
        Assertions.assertTrue(eval(expression("startTime", "<", "2026-07-01 12:00:01"), mapOf("startTime", startTime)));
    }

    @Test
    public void testLocalDateTimeComparison() {
        LocalDateTime startTime = LocalDateTime.of(2026, 7, 1, 12, 0);
        Assertions.assertTrue(eval(expression("startTime", ">=", "2026-07-01 12:00:00"), mapOf("startTime", startTime)));
        Assertions.assertFalse(eval(expression("startTime", "<", "2026-07-01 11:59:59"), mapOf("startTime", startTime)));
    }

    @Test
    public void testMissingArgumentReturnsFalse() {
        Assertions.assertFalse(eval(expression("name", "==", "张三"), Collections.emptyMap()));
    }

    private void assertCondition(String field, String operator, String value, Map<String, Object> args, boolean expected) {
        Assertions.assertEquals(expected, eval(expression(field, operator, value), args));
    }

    private NodeExpression expression(String field, String operator, String value) {
        NodeExpression expression = new NodeExpression();
        expression.setField(field);
        expression.setOperator(operator);
        expression.setValue(value);
        return expression;
    }

    private boolean eval(NodeExpression expression, Map<String, Object> args) {
        SpelFlowLongExpression evaluator = new SpelFlowLongExpression();
        List<List<NodeExpression>> conditions = Collections.singletonList(Collections.singletonList(expression));
        return evaluator.eval(conditions, args);
    }

    private Map<String, Object> mapOf(String key, Object value) {
        Map<String, Object> args = new HashMap<>();
        args.put(key, value);
        return args;
    }
}
