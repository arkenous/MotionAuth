package net.trileg.motionauth.Utility;

/**
 * Use for judgment of correlation.
 *
 * @author Kensuke Kosaka
 */
public class Enum {
	public enum STATUS {
		DOWN, UP
	}

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
