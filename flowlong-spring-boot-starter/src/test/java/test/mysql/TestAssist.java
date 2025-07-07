package test.mysql;

import com.aizuda.bpm.engine.assist.DateUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TestAssist {

    @Test
    public void parseTimerTaskTime() {
        Assertions.assertNotNull(DateUtils.parseDelayTime("1:d"));
        Assertions.assertNotNull(DateUtils.parseDelayTime("1:h"));
        Assertions.assertNotNull(DateUtils.parseDelayTime("1:m"));
        Assertions.assertNotNull(DateUtils.parseDelayTime("01:30:10"));
        Assertions.assertNotNull(DateUtils.parseDelayTime("12:30:10"));
        Assertions.assertNotNull(DateUtils.parseDelayTime("22:30:10"));
    }
}
