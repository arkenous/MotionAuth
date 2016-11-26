package net.trileg.motionauth.Lowpass;

import net.trileg.motionauth.Utility.ManageData;

import edu.emory.mathcs.jtransforms.fft.DoubleFFT_1D;

import static android.util.Log.INFO;
import static net.trileg.motionauth.Utility.Enum.NUM_AXIS;
import static net.trileg.motionauth.Utility.LogUtil.log;

/**
 * Low pass filter using Fourier transform.
 * Using JTransforms for Fourier transform.
 *
 * @author Kensuke Kosaka
 * @see <a href="https://sites.google.com/site/piotrwendykier/software/jtransforms">https://sites.google.com/site/piotrwendykier/software/jtransforms</a>
 */
public class Fourier {
  private ManageData manageData = new ManageData();

  /**
   * Low pass filtering double type 3-array data.
   *
   * @param data       Double type 3-array data you want to low pass filtering.
   * @param sensorName Sensor name.
   * @param userName   user name.
   * @return Low pass filtered double type 3-array data.
   */
  public double[][][] LowpassFilter(double[][][] data, String sensorName, String userName) {
    log(INFO);

    DoubleFFT_1D realfft = new DoubleFFT_1D(data[0][0].length);

    // Execute forward fourier transform
    for (double[][] i : data) for (double[] j : i) realfft.realForward(j);

    double[][][] real = new double[data.length][NUM_AXIS][data[0][0].length / 2];
    double[][][] imaginary = new double[data.length][NUM_AXIS][data[0][0].length / 2];

    int countReal;
    int countImaginary;

    // Decompose real-part and imaginary-part
    for (int time = 0; time < data.length; time++) {
      for (int axis = 0; axis < NUM_AXIS; axis++) {
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

    manageData.writeDoubleThreeArrayData(userName, "ResultFFT", "rFFT" + sensorName, real);
    manageData.writeDoubleThreeArrayData(userName, "ResultFFT", "iFFT" + sensorName, imaginary);

    double[][][] power = new double[data.length][NUM_AXIS][data[0][0].length / 2];

    for (int time = 0; time < data.length; time++)
      for (int axis = 0; axis < NUM_AXIS; axis++)
        for (int item = 0; item < data[time][axis].length / 2; item++)
          power[time][axis][item] = Math.sqrt(Math.pow(real[time][axis][item], 2)
                                              + Math.pow(imaginary[time][axis][item], 2));

    manageData.writeDoubleThreeArrayData(userName, "ResultFFT", "powerFFT" + sensorName, power);

    // Low pass filtering
    for (int time = 0; time < data.length; time++)
      for (int axis = 0; axis < NUM_AXIS; axis++)
        for (int item = 0; item < data[time][axis].length; item++)
          if (item > 30) data[time][axis][item] = 0;

    for (double[][] i : data) for (double[] j : i) realfft.realInverse(j, true);

    manageData.writeDoubleThreeArrayData(userName, "AfterFFT", sensorName, data);

    return data;
  }


  /**
   * Low pass filtering double type 2-array data.
   *
   * @param data       Double type 2-array data you want to low pass filtering.
   * @param sensorName sensor name.
   * @param userName   user name.
   * @return Low pass filtered double type 2-array data.
   */
  public double[][] LowpassFilter(double[][] data, String sensorName, String userName) {
    log(INFO);

    DoubleFFT_1D realfft = new DoubleFFT_1D(data[0].length);

    // Execute forward fourier transform
    for (double[] i : data) realfft.realForward(i);

    double[][] real = new double[NUM_AXIS][data[0].length / 2];
    double[][] imaginary = new double[NUM_AXIS][data[0].length / 2];

    int countReal;
    int countImaginary;

    // Decompose real-part and imaginary-part
    for (int axis = 0; axis < NUM_AXIS; axis++) {
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

    manageData.writeDoubleTwoArrayData(userName, "ResultFFT", "rFFT" + sensorName, real);
    manageData.writeDoubleTwoArrayData(userName, "ResultFFT", "iFFT" + sensorName, imaginary);

    double[][] power = new double[NUM_AXIS][data[0].length / 2];
    for (int axis = 0; axis < NUM_AXIS; axis++)
      for (int item = 0; item < data[axis].length / 2; item++)
        power[axis][item] = Math.sqrt(Math.pow(real[axis][item], 2)
                                      + Math.pow(imaginary[axis][item], 2));

    manageData.writeDoubleTwoArrayData(userName, "ResultFFT", "powerFFT" + sensorName, power);

    // Low pass filtering
    for (int axis = 0; axis < NUM_AXIS; axis++)
      for (int item = 0; item < data[axis].length; item++)
        if (item > 30) data[axis][item] = 0;

    // Execute inverse fourier transform
    for (double[] i : data) realfft.realInverse(i, true);

    manageData.writeDoubleTwoArrayData(userName, "AfterFFT", sensorName, data);

    return data;
  }
}
