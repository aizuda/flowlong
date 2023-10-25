package test;

import com.googlecode.aviator.AviatorEvaluator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

public class TestSpelExpression {

    @Test
    public void test() {
        Map<String, Object> args = new HashMap<>();
        args.put("day", 8);
        Assertions.assertTrue((Boolean) AviatorEvaluator.execute("day>7", args));
    }
}
