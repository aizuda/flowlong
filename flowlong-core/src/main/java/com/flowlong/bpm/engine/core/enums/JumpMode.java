package com.flowlong.bpm.engine.core.enums;

/**
 * 跳转模式
 *
 * <p>
 * 尊重知识产权，CV 请保留版权，爱组搭 http://aizuda.com 出品
 * </p>
 *
 * @author Binfeng.Yan
 * @since 1.0
 */
public enum JumpMode {

	/**
	 * 任意跳转
	 */
	all(0),
	/**
	 * 前进
	 */
	advance(1),
	/**
	 * 后退
	 */
	retreat(2);

	private final int value;

	JumpMode(int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}

}
