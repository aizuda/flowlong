package com.flowlong.bpm.solon.adaptive;

import com.flowlong.bpm.engine.handler.FlowJsonHandler;
import org.noear.snack.ONode;
import org.noear.solon.annotation.Component;


/**
 * Solon Snack3 JSON 解析处理器接口适配
 *
 * <p>
 * 尊重知识产权，CV 请保留版权，爱组搭 http://aizuda.com 出品，不允许非法使用，后果自负
 * </p>
 *
 * @author hubin
 * @author noear
 * @since 1.0
 */
@Component
public class SolonFlowJsonHandler implements FlowJsonHandler {
    @Override
    public String toJson(Object object) {
        return ONode.stringify(object);
    }

    @Override
    public <T> T fromJson(String jsonString, Class<T> clazz) {
        return ONode.deserialize(jsonString, clazz);
    }
}
