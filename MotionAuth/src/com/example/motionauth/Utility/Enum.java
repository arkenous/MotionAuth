package com.example.motionauth.Utility;

/**
 * 列挙型．相関周りの判定の際に使用
 *
 * @author Kensuke Kousaka
 */
public class Enum {
	public static enum MEASURE {
		BAD, INCORRECT, CORRECT, PERFECT
	}

	public static enum MODE {
		MAX, MIN, MEDIAN
	}

	public final double LOOSE = 0.4;
	public final double NORMAL = 0.6;
	public final double STRICT = 0.8;
}
