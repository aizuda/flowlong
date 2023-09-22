package test;

import com.flowlong.bpm.solon.adaptive.SolonExpression;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

public class TestSpelExpression {

    @Test
    public void test() {
        SolonExpression expression = new SolonExpression();
        Map<String, Object> args = new HashMap<>();
        args.put("day", 8);
        Assertions.assertTrue(expression.eval(Boolean.class, "#day>7", args));
    }
}
