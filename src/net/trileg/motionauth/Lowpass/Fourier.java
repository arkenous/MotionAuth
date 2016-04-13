package net.trileg.motionauth.Lowpass;

import android.util.Log;
import edu.emory.mathcs.jtransforms.fft.DoubleFFT_1D;
import net.trileg.motionauth.Authentication.InputName;
import net.trileg.motionauth.Utility.Enum;
import net.trileg.motionauth.Utility.LogUtil;
import net.trileg.motionauth.Utility.ManageData;

/**
 * Low pass filter using Fourier transform.
 * Using JTransforms for Fourier transform.
 *
 * @author Kensuke Kosaka
 * @see <a href="https://sites.google.com/site/piotrwendykier/software/jtransforms">https://sites.google.com/site/piotrwendykier/software/jtransforms</a>
 */
public class Fourier {
  private ManageData mManageData = new ManageData();

  /**
   * Low pass filtering double type 3-array data.
   *
   * @param data     Double type 3-array data you want to low pass filtering.
   * @param sensorName Sensor name.
   * @return Low pass filtered double type 3-array data.
   */
  public double[][][] LowpassFilter(double[][][] data, String sensorName) {
    LogUtil.log(Log.INFO);

    DoubleFFT_1D realfft = new DoubleFFT_1D(data[0][0].length);

    // Execute forward fourier transform
    for (double[][] i : data) {
      for (double[] j : i) {
        realfft.realForward(j);
      }
    }

    double[][][] real = new double[Enum.NUM_TIME][Enum.NUM_AXIS][data[0][0].length / 2];
    double[][][] imaginary = new double[Enum.NUM_TIME][Enum.NUM_AXIS][data[0][0].length / 2];

    int countReal;
    int countImaginary;

    // Decompose real-part and imaginary-part
    for (int time = 0; time < Enum.NUM_TIME; time++) {
      for (int axis = 0; axis < Enum.NUM_AXIS; axis++) {
        countReal = 0;
        countImaginary = 0;
        for (int item = 0; item < data[time][axis].length; item++) {
          if (item % 2 == 0) {
            real[time][axis][countReal] = data[time][axis][item];
            countReal++;
          } else {
            imaginary[time][axis][countImaginary] = data[time][axis][item];
            countImaginary++;
          }
        }
      }
    }

    mManageData.writeDoubleThreeArrayData(net.trileg.motionauth.Registration.InputName.name, "ResultFFT", "rFFT" + sensorName, real);
    mManageData.writeDoubleThreeArrayData(net.trileg.motionauth.Registration.InputName.name, "ResultFFT", "iFFT" + sensorName, imaginary);

    double[][][] power = new double[Enum.NUM_TIME][Enum.NUM_AXIS][data[0][0].length / 2];

    for (int time = 0; time < Enum.NUM_TIME; time++) {
      for (int axis = 0; axis < Enum.NUM_AXIS; axis++) {
        for (int item = 0; item < data[time][axis].length / 2; item++) {
          power[time][axis][item] = Math.sqrt(Math.pow(real[time][axis][item], 2) + Math.pow(imaginary[time][axis][item], 2));
        }
      }
    }

    mManageData.writeDoubleThreeArrayData(net.trileg.motionauth.Registration.InputName.name, "ResultFFT", "powerFFT" + sensorName, power);

    // Low pass filtering
    for (int time = 0; time < Enum.NUM_TIME; time++) {
      for (int axis = 0; axis < Enum.NUM_AXIS; axis++) {
        for (int item = 0; item < data[time][axis].length; item++) {
          if (item > 30) data[time][axis][item] = 0;
        }
      }
    }

    for (double[][] i : data) {
      for (double[] j : i) {
        realfft.realInverse(j, true);
      }
    }

    mManageData.writeDoubleThreeArrayData(net.trileg.motionauth.Registration.InputName.name, "AfterFFT", sensorName, data);

    return data;
  }


  /**
   * Low pass filtering double type 2-array data.
   *
   * @param data     Double type 2-array data you want to low pass filtering.
   * @param sensorName sensor name.
   * @return Low pass filtered double type 2-array data.
   */
  public double[][] LowpassFilter(double[][] data, String sensorName) {
    LogUtil.log(Log.INFO);

    DoubleFFT_1D realfft = new DoubleFFT_1D(data[0].length);

    // Execute forward fourier transform
    for (double[] i : data) realfft.realForward(i);

    double[][] real = new double[Enum.NUM_AXIS][data[0].length / 2];
    double[][] imaginary = new double[Enum.NUM_AXIS][data[0].length / 2];

    int countReal;
    int countImaginary;

    // Decompose real-part and imaginary-part
    for (int axis = 0; axis < Enum.NUM_AXIS; axis++) {
      countReal = 0;
      countImaginary = 0;
      for (int item = 0; item < data[axis].length; item++) {
        if (item % 2 == 0) {
          real[axis][countReal] = data[axis][item];
          countReal++;
        } else {
          imaginary[axis][countImaginary] = data[axis][item];
          countImaginary++;
        }

      }
    }

    mManageData.writeDoubleTwoArrayData(InputName.userName, "ResultFFT", "rFFT" + sensorName, real);
    mManageData.writeDoubleTwoArrayData(InputName.userName, "ResultFFT", "iFFT" + sensorName, imaginary);

    double[][] power = new double[Enum.NUM_AXIS][data[0].length / 2];
    for (int axis = 0; axis < Enum.NUM_AXIS; axis++) {
      for (int item = 0; item < data[axis].length / 2; item++) {
        power[axis][item] = Math.sqrt(Math.pow(real[axis][item], 2) + Math.pow(imaginary[axis][item], 2));
      }
    }

    mManageData.writeDoubleTwoArrayData(InputName.userName, "ResultFFT", "powerFFT" + sensorName, power);

    // Low pass filtering
    for (int axis = 0; axis < Enum.NUM_AXIS; axis++) {
      for (int item = 0; item < data[axis].length; item++) {
        if (item > 30) data[axis][item] = 0;
      }
    }

    // Execute inverse fourier transform
    for (double[] i : data) realfft.realInverse(i, true);

    mManageData.writeDoubleTwoArrayData(InputName.userName, "AfterFFT", sensorName, data);

    return data;
  }
}
