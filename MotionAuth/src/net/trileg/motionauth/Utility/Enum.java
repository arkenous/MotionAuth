package net.trileg.motionauth.Utility;

/**
 * 列挙型．相関周りの判定の際に使用
 *
 * @author Kensuke Kousaka
 */
public class Enum {
	public enum MEASURE {
		BAD, INCORRECT, CORRECT, PERFECT
	}

	public enum MODE {
		MAX, MIN, MEDIAN
	}

	public enum TARGET {
		DISTANCE, ANGLE
	}

	public final double LOOSE = 0.4;
	public final double NORMAL = 0.6;
	public final double STRICT = 0.8;
}
