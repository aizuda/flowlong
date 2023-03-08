package com.flowlong.bpm.engine.scheduling;

import com.flowlong.bpm.engine.core.FlowLongContext;
import lombok.Getter;
import lombok.Setter;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


/**
 * Spring Boot 内置定时任务实现流程提醒处理类
 *
 * <p>
 * 尊重知识产权，CV 请保留版权，爱组搭 http://aizuda.com 出品
 * </p>
 *
 * @author hubin
 * @since 1.0
 */
@Getter
@Setter
public class SpringBootScheduler {
    private FlowLongContext context;
    private JobReminder jobReminder;

    /**
     * 提醒防重入锁
     */
    private static Lock REMIND_LOCK = new ReentrantLock();

    @Scheduled(cron = "${flowlong.remind.cron}")
    public void remind() {
        if (null != jobReminder) {
            try {
                REMIND_LOCK.lock();

                // TODO 数据库中定时读取待提醒流程实例和任务
                jobReminder.remind(context, null, null);
            } finally {
                REMIND_LOCK.unlock();
            }
        }
    }
}
