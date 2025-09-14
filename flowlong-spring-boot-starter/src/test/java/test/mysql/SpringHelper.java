package test.mysql;

import com.aizuda.bpm.engine.FlowLongEngine;
import org.springframework.context.ApplicationContext;

public class SpringHelper {
    private static ApplicationContext APPLICATION_CONTEXT;

    public static void setApplicationContext(ApplicationContext applicationContext) {
        if (null == APPLICATION_CONTEXT) {
            APPLICATION_CONTEXT = applicationContext;
        }
    }

    public static <T> T getBean(Class<T> clazz) {
        return APPLICATION_CONTEXT.getBean(clazz);
    }

    public static FlowLongEngine getFlowLongEngine() {
        return getBean(FlowLongEngine.class);
    }
}
