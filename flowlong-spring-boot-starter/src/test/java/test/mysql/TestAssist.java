package test.mysql;

import com.aizuda.bpm.engine.assist.DateUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TestAssist {

    @Test
    public void parseTimerTaskTime() {
        Assertions.assertNotNull(DateUtils.parseTimerTaskTime("1:d"));
        Assertions.assertNotNull(DateUtils.parseTimerTaskTime("1:h"));
        Assertions.assertNotNull(DateUtils.parseTimerTaskTime("1:m"));
        Assertions.assertNotNull(DateUtils.parseTimerTaskTime("01:30:10"));
        Assertions.assertNotNull(DateUtils.parseTimerTaskTime("12:30:10"));
        Assertions.assertNotNull(DateUtils.parseTimerTaskTime("22:30:10"));
    }
}
