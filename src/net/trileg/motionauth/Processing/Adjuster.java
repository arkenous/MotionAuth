package net.trileg.motionauth.Processing;

import java.util.ArrayList;

import static net.trileg.motionauth.Utility.Enum.NUM_AXIS;

/**
 * Adjustment data length.
 * Set adjustment base to longest data.
 *
 * @author Kensuke Kosaka
 */
public class Adjuster {

  public ArrayList<float[][][]> adjust(float[][][] linearAcceleration,
                                       float[][][] gyroscope) {
    // Get max length of each time data.
    ArrayList<Integer> lengthList = new ArrayList<>();
    for (float[][] aLinearAcceleration : linearAcceleration)
      lengthList.add(aLinearAcceleration[0].length);

    int maxTime = 0;
    int maxLength = 0;

    int tmp = 0;
    for (int time = 0; time < linearAcceleration.length; time++) {
      if (tmp < lengthList.get(time)) {
        maxTime = time;
        maxLength = lengthList.get(time);
      }
    }

    // Adjust data length to even number for low pass filtering
    if (maxLength % 2 != 0) maxLength -= 1;

    float[][][] linearAccelerationArray = new float[linearAcceleration.length][linearAcceleration[0].length][maxLength];
    float[][][] gyroscopeArray = new float[gyroscope.length][gyroscope[0].length][maxLength];

    for (int time = 0; time < linearAcceleration.length; time++) {
      if (time == maxTime) {
        for (int axis = 0; axis < NUM_AXIS; axis++) {
          for (int length = 0; length < maxLength; length++) {
            linearAccelerationArray[time][axis][length] = linearAcceleration[time][axis][length];
            gyroscopeArray[time][axis][length] = gyroscope[time][axis][length];
          }
        }
      } else {
        int diff = maxLength - linearAcceleration[time][0].length;

        for (int axis = 0; axis < NUM_AXIS; axis++) {
          for (int length = 0; length < maxLength; length++) {
            if (length < maxLength - diff) {
              linearAccelerationArray[time][axis][length] = linearAcceleration[time][axis][length];
              gyroscopeArray[time][axis][length] = gyroscope[time][axis][length];
            } else {
              linearAccelerationArray[time][axis][length] = 0;
              gyroscopeArray[time][axis][length] = 0;
            }
          }
        }
      }
    }

    ArrayList<float[][][]> result = new ArrayList<>();
    result.add(linearAccelerationArray);
    result.add(gyroscopeArray);
    return result;
  }


  public ArrayList<float[][]> adjust(float[][] linearAcceleration,
                                     float[][] gyroscope,
                                     int registeredDataLength) {
    float[][] linearAccelerationArray = new float[linearAcceleration.length][registeredDataLength];
    float[][] gyroscopeArray = new float[gyroscope.length][registeredDataLength];

    if (registeredDataLength < linearAcceleration[0].length) {
      // New data is longer than registered data.
      for (int axis = 0; axis < NUM_AXIS; axis++) {
        for (int length = 0; length < registeredDataLength; length++) {
          linearAccelerationArray[axis][length] = linearAcceleration[axis][length];
          gyroscopeArray[axis][length] = gyroscope[axis][length];
        }
      }
    } else if (registeredDataLength > linearAcceleration[0].length) {
      // New data is shorter than registered data.
      int diff = registeredDataLength - linearAcceleration[0].length;

      for (int axis = 0; axis < NUM_AXIS; axis++) {
        for (int length = 0; length < registeredDataLength; length++) {
          if (length < registeredDataLength - diff) {
            linearAccelerationArray[axis][length] = linearAcceleration[axis][length];
            gyroscopeArray[axis][length] = gyroscope[axis][length];
          } else {
            linearAccelerationArray[axis][length] = 0;
            gyroscopeArray[axis][length] = 0;
          }
        }
      }
    } else {
      for (int axis = 0; axis < NUM_AXIS; axis++) {
        for (int length = 0; length < registeredDataLength; length++) {
          linearAccelerationArray[axis][length] = linearAcceleration[axis][length];
          gyroscopeArray[axis][length] = gyroscope[axis][length];
        }
      }
    }

    ArrayList<float[][]> result = new ArrayList<>();
    result.add(linearAccelerationArray);
    result.add(gyroscopeArray);
    return result;
  }
}
