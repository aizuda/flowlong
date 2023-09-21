package com.flowlong.bpm.solon.autoconfigure;

import com.flowlong.bpm.engine.scheduling.RemindParam;
import org.noear.solon.annotation.Configuration;
import org.noear.solon.annotation.Inject;

/**
 * 配置属性
 *
 * <p>
 * 尊重知识产权，CV 请保留版权，爱组搭 http://aizuda.com 出品，不允许非法使用，后果自负
 * </p>
 *
 * @author hubin
 * @since 1.0
 */
@Inject("${flowlong}")
@Configuration
public class FlowLongProperties {
    /**
     * 提醒时间
     */
    private RemindParam remind;

    public RemindParam getRemind() {
        return remind;
    }

    public void setRemind(RemindParam remind) {
        this.remind = remind;
    }
}