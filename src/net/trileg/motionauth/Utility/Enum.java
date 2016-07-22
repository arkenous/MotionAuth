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
    INCORRECT, MAYBE, CORRECT, PERFECT
  }

  public enum MODE {
    MAX, MIN, MEDIAN
  }

  public enum TARGET {
    DISTANCE, LINEAR_DISTANCE, ANGLE
  }

  public static final double LOOSE = 0.4;
  public static final double NORMAL = 0.6;
  public static final double STRICT = 0.8;

  public static final int NUM_AXIS = 3;
  public static final int NUM_TIME = 3;

  public static final double SENSOR_DELAY_TIME = 0.02;
}
