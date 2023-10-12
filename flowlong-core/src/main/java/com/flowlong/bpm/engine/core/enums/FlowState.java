package com.flowlong.bpm.engine.core.enums;

/**
 * 流程状态
 */
public enum FlowState {
    /**
     * 启用
     */
    active(1),
    /**
     * 未启用
     */
    inactive(0);

    private final int value;

    FlowState(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

}
