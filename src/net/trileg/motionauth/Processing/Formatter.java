package net.trileg.motionauth.Processing;

import static android.util.Log.INFO;
import static net.trileg.motionauth.Utility.Enum.*;
import static net.trileg.motionauth.Utility.LogUtil.log;


/**
 * Round data length.
 *
 * @author Kensuke Kosaka
 */
public class Formatter {

  public double[][] convertFloatToDouble(float[][] inputVal) {
    log(INFO);

    double[][] returnVal = new double[NUM_AXIS][inputVal[0].length];

    for (int axis = 0; axis < inputVal.length; axis++) {
      for (int item = 0; item < inputVal[axis].length; item++) {
        returnVal[axis][item] = inputVal[axis][item];
      }
    }

    return returnVal;
  }


  public double[][][] convertFloatToDouble(float[][][] inputVal) {
    log(INFO);

    double[][][] returnVal = new double[NUM_TIME][NUM_AXIS][inputVal[0][0].length];

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
