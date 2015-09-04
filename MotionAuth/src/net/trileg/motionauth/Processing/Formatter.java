package net.trileg.motionauth.Processing;

import android.util.Log;
import net.trileg.motionauth.Utility.Enum;
import net.trileg.motionauth.Utility.LogUtil;

import java.util.Locale;


/**
 * Round data length.
 *
 * @author Kensuke Kosaka
 */
public class Formatter {

  /**
   * Round float type 2-array data to two digits after the decimal point, and convert it to double type.
   *
   * @param inputVal Float type 2-array data.
   * @return Rounded and Converted double type 2-array data.
   */
  public double[][] floatToDoubleFormatter(float[][] inputVal) {
    LogUtil.log(Log.INFO);

    double[][] returnVal = new double[Enum.NUM_AXIS][inputVal[0].length];

    for (int axis = 0; axis < Enum.NUM_AXIS; axis++) {
      for (int item = 0; item < inputVal[axis].length; item++) {
        String format = String.format(Locale.getDefault(), "%.2f", inputVal[axis][item]);
        returnVal[axis][item] = Double.valueOf(format);
      }
    }

    return returnVal;
  }


  /**
   * Round float type 3-array data to two digits after the decimal point, and convert it to double type.
   *
   * @param inputVal Float type 3-array data.
   * @return Rounded and Converted double type 3-array data.
   */
  public double[][][] floatToDoubleFormatter(float[][][] inputVal) {
    LogUtil.log(Log.INFO);

    double[][][] returnVal = new double[Enum.NUM_TIME][Enum.NUM_AXIS][inputVal[0][0].length];

    for (int time = 0; time < inputVal.length; time++) {
      for (int axis = 0; axis < inputVal[time].length; axis++) {
        for (int item = 0; item < inputVal[time][axis].length; item++) {
          String format = String.format(Locale.getDefault(), "%.2f", inputVal[time][axis][item]);
          returnVal[time][axis][item] = Double.valueOf(format);
        }
      }
    }

    return returnVal;
  }


  /**
   * Round double type 2-array data to two digits after the decimal point.
   *
   * @param inputVal Double type 2-array data.
   * @return Rounded double type 2-array data.
   */
  public double[][] doubleToDoubleFormatter(double[][] inputVal) {
    LogUtil.log(Log.INFO);

    double[][] returnVal = new double[Enum.NUM_AXIS][inputVal[0].length];

    for (int axis = 0; axis < inputVal.length; axis++) {
      for (int item = 0; item < inputVal[axis].length; item++) {
        String format = String.format(Locale.getDefault(), "%.2f", inputVal[axis][item]);
        returnVal[axis][item] = Double.valueOf(format);
      }
    }

    return returnVal;
  }


  /**
   * Round double type 3-array data to two digits after the decimal point.
   *
   * @param inputVal Double type 3-array data.
   * @return Rounded double type 3-array data.
   */
  public double[][][] doubleToDoubleFormatter(double[][][] inputVal) {
    LogUtil.log(Log.INFO);

    double[][][] returnVal = new double[Enum.NUM_TIME][Enum.NUM_AXIS][inputVal[0][0].length];

    for (int time = 0; time < inputVal.length; time++) {
      for (int axis = 0; axis < inputVal[time].length; axis++) {
        for (int item = 0; item < inputVal[time][axis].length; item++) {
          String format = String.format(Locale.getDefault(), "%.2f", inputVal[time][axis][item]);
          returnVal[time][axis][item] = Double.valueOf(format);
        }
      }
    }

    return returnVal;
  }
}
