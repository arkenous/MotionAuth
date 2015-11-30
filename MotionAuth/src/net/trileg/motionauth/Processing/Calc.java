package net.trileg.motionauth.Processing;

import android.util.Log;
import net.trileg.motionauth.Utility.Enum;
import net.trileg.motionauth.Utility.LogUtil;

/**
 * Calculate distance or angle from acceleration or gyroscope.
 *
 * @author Kensuke Kosaka
 */
public class Calc {

  /**
   * Convert acceleration data to distance data.
   *
   * @param inputVal 3-array acceleration data for target of conversion.
   * @param t        time
   * @return After converted 3-array distance data.
   */
  public double[][][] accelToDistance(double[][][] inputVal, double t) {
    LogUtil.log(Log.INFO);

    double[][][] returnVal = new double[Enum.NUM_TIME][Enum.NUM_AXIS][inputVal[0][0].length];

    for (int time = 0; time < Enum.NUM_TIME; time++) {
      for (int axis = 0; axis < Enum.NUM_AXIS; axis++) {
        for (int item = 0; item < inputVal[time][axis].length; item++) {
          returnVal[time][axis][item] = (inputVal[time][axis][item] * t * t) / 2;
        }
      }
    }

    return returnVal;
  }


  /**
   * Convert gyroscope data to angle data.
   *
   * @param inputVal 3-array gyroscope data for target of conversion.
   * @param t        time
   * @return After converted 3-array angle data.
   */
  public double[][][] gyroToAngle(double[][][] inputVal, double t) {
    LogUtil.log(Log.INFO);

    double[][][] returnVal = new double[Enum.NUM_TIME][Enum.NUM_AXIS][inputVal[0][0].length];

    for (int time = 0; time < Enum.NUM_TIME; time++) {
      for (int axis = 0; axis < Enum.NUM_AXIS; axis++) {
        for (int item = 0; item < inputVal[time][axis].length; item++) {
          returnVal[time][axis][item] = (inputVal[time][axis][item] * t);
        }
      }
    }

    return returnVal;
  }


  /**
   * Convert acceleration data to distance data.
   *
   * @param inputVal 2-array acceleration data for target of conversion.
   * @param t        time
   * @return After converted 2-array distance data.
   */
  public double[][] accelToDistance(double[][] inputVal, double t) {
    LogUtil.log(Log.INFO);

    double[][] returnVal = new double[Enum.NUM_AXIS][inputVal[0].length];

    for (int axis = 0; axis < Enum.NUM_AXIS; axis++) {
      for (int item = 0; item < inputVal[axis].length; item++) {
        returnVal[axis][item] = (inputVal[axis][item] * t * t) / 2;
      }
    }

    return returnVal;
  }


  /**
   * Convert gyroscope data to angle data.
   *
   * @param inputVal 2-array gyroscope data for target of conversion.
   * @param t        time
   * @return After converted 2-array angle data.
   */
  public double[][] gyroToAngle(double[][] inputVal, double t) {
    LogUtil.log(Log.INFO);

    double[][] returnVal = new double[Enum.NUM_AXIS][inputVal[0].length];

    for (int axis = 0; axis < Enum.NUM_AXIS; axis++) {
      for (int item = 0; item < inputVal[axis].length; item++) {
        returnVal[axis][item] = (inputVal[axis][item] * t);
      }
    }

    return returnVal;
  }
}
