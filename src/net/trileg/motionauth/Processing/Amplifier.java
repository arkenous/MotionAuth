package net.trileg.motionauth.Processing;

import android.util.Log;
import net.trileg.motionauth.Utility.Enum;
import net.trileg.motionauth.Utility.LogUtil;

/**
 * Amplify data.
 *
 * @author Kensuke Kosaka
 */
public class Amplifier {
  private boolean isRangeCheck = false;


  /**
   * Check weather data is less than threshold.
   *
   * @param data Double type 3-array data to check.
   * @return true if data is less than threshold even once during the entire number of trials, otherwise false.
   */
  public boolean CheckValueRange(double[][][] data, double checkRangeValue) {
    LogUtil.log(Log.INFO);

    LogUtil.log(Log.DEBUG, "checkRangeValue" + checkRangeValue);

    double[][] max = new double[Enum.NUM_TIME][Enum.NUM_AXIS];
    double[][] min = new double[Enum.NUM_TIME][Enum.NUM_AXIS];

    for (int time = 0; time < Enum.NUM_TIME; time++) {
      for (int axis = 0; axis < Enum.NUM_AXIS; axis++) {
        max[time][axis] = 0;
        min[time][axis] = 0;
      }
    }

    double range;
    for (int time = 0; time < Enum.NUM_TIME; time++) {
      for (int axis = 0; axis < Enum.NUM_AXIS; axis++) {
        for (int item = 0; item < data[time][axis].length; item++) {
          if (data[time][axis][item] > max[time][axis]) {
            max[time][axis] = data[time][axis][item];
          } else if (data[time][axis][item] < min[time][axis]) {
            min[time][axis] = data[time][axis][item];
          }
        }
      }
    }

    for (int time = 0; time < Enum.NUM_TIME; time++) {
      for (int axis = 0; axis < Enum.NUM_AXIS; axis++) {
        range = max[time][axis] - min[time][axis];
        LogUtil.log(Log.DEBUG, "range = " + range);
        if (range < checkRangeValue) isRangeCheck = true;
      }
    }

    return isRangeCheck;
  }


  /**
   * Amplify data.
   *
   * @param data     Double type 3-array data to amplify.
   * @param ampValue How much amplify data.
   * @return Amplified double type 3-array data
   */
  public double[][][] Amplify(double[][][] data, double ampValue) {
    LogUtil.log(Log.INFO);

    if (ampValue != 0.0) {
      for (int time = 0; time < Enum.NUM_TIME; time++) {
        for (int axis = 0; axis < Enum.NUM_AXIS; axis++) {
          for (int item = 0; item < data[time][axis].length; item++) {
            data[time][axis][item] *= ampValue;
          }
        }
      }
    }
    return data;
  }


  /**
   * Amplify data.
   *
   * @param data     Double type 2-array data to amplify.
   * @param ampValue How much amplify data.
   * @return Amplified double type 2-array data
   */
  public double[][] Amplify(double[][] data, double ampValue) {
    LogUtil.log(Log.INFO);

    if (ampValue != 0.0) {
      for (int axis = 0; axis < Enum.NUM_AXIS; axis++) {
        for (int item = 0; item < data[axis].length; item++) {
          data[axis][item] *= ampValue;
        }
      }
    }

    return data;
  }
}