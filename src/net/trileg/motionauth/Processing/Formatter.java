package net.trileg.motionauth.Processing;

import android.util.Log;
import net.trileg.motionauth.Utility.Enum;
import net.trileg.motionauth.Utility.LogUtil;


/**
 * Round data length.
 *
 * @author Kensuke Kosaka
 */
public class Formatter {

  public double[][] convertFloatToDouble(float[][] inputVal) {
    LogUtil.log(Log.INFO);

    double[][] returnVal = new double[Enum.NUM_AXIS][inputVal[0].length];

    for (int axis = 0; axis < inputVal.length; axis++) {
      for (int item = 0; item < inputVal[axis].length; item++) {
        returnVal[axis][item] = inputVal[axis][item];
      }
    }

    return returnVal;
  }


  public double[][][] convertFloatToDouble(float[][][] inputVal) {
    LogUtil.log(Log.INFO);

    double[][][] returnVal = new double[Enum.NUM_TIME][Enum.NUM_AXIS][inputVal[0][0].length];

    for (int time = 0; time < inputVal.length; time++) {
      for (int axis = 0; axis < inputVal[time].length; axis++) {
        for (int item = 0; item < inputVal[time][axis].length; item++) {
          returnVal[time][axis][item] = inputVal[time][axis][item];
        }
      }
    }

    return returnVal;
  }
}
