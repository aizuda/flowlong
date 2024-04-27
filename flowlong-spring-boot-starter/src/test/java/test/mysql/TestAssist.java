package test.mysql;

import com.aizuda.bpm.engine.assist.DateUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TestAssist {

    @Test
    public void parseTimerTaskTime() {
        Assertions.assertNotNull(DateUtils.parseTimerTaskTime("1:D"));
        Assertions.assertNotNull(DateUtils.parseTimerTaskTime("1:H"));
        Assertions.assertNotNull(DateUtils.parseTimerTaskTime("1:M"));
        Assertions.assertNotNull(DateUtils.parseTimerTaskTime("01:30:10"));
        Assertions.assertNotNull(DateUtils.parseTimerTaskTime("12:30:10"));
        Assertions.assertNotNull(DateUtils.parseTimerTaskTime("22:30:10"));
    }
}
