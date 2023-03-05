package com.flowlong.bpm.engine.core.enums;


/**
 * 流程状态
 *
 */
public enum InstanceState {
    /**
     * 活动
     */
    active(1),
    /**
     * 结束
     */
    finish(0),
    /**
     * 终止
     */
    termination(2);

    private final int value;

    InstanceState(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

}
