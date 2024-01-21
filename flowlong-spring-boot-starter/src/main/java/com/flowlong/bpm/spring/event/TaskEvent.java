/*
 * Copyright 2023-2025 Licensed under the AGPL License
 */
package com.flowlong.bpm.spring.event;

import com.flowlong.bpm.engine.core.enums.EventType;
import com.flowlong.bpm.engine.entity.FlwTask;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * 流程任务事件对象
 *
 * <p>
 * 尊重知识产权，不允许非法使用，后果自负
 * </p>
 *
 * @author hubin
 * @since 1.0
 */
@Getter
@Setter
public class TaskEvent implements Serializable {
    private EventType eventType;
    private FlwTask flwTask;

}
