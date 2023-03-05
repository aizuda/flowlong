package test.mysql.task.interceptor;

import com.flowlong.bpm.engine.FlowLongInterceptor;
import com.flowlong.bpm.engine.core.Execution;
import com.flowlong.bpm.engine.core.FlowLongContext;
import com.flowlong.bpm.engine.entity.Task;
import lombok.extern.slf4j.Slf4j;

/**
 * Interceptor for test
 *
 * @author august
 */
@Slf4j
public class LocalTaskInterceptor implements FlowLongInterceptor {

    @Override
    public void intercept(FlowLongContext flowLongContext, Execution execution) {
        if (log.isInfoEnabled()) {
            log.info("LocalTaskInterceptor start...");
            for (Task task : execution.getTasks()) {
                StringBuilder buffer = new StringBuilder(100);
                buffer.append("创建任务[标识=").append(task.getId());
                buffer.append(",名称=").append(task.getDisplayName());
                buffer.append(",创建时间=").append(task.getCreateTime());
                buffer.append(",参与者={");
                if (task.actorIds() != null) {
                    for (String actor : task.actorIds()) {
                        buffer.append(actor).append(";");
                    }
                }
                buffer.append("}]");
                log.info(buffer.toString());
            }
            log.info("LocalTaskInterceptor finish...");
        }
    }
}
