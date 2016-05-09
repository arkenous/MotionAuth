package net.trileg.motionauth.Processing;

import net.trileg.motionauth.Utility.Enum;

import java.util.ArrayList;

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
    int firstLength = linearAcceleration[0][0].length;
    int secondLength = linearAcceleration[1][0].length;
    int thirdLength = linearAcceleration[2][0].length;

    int maxTime;
    int maxLength;

    if (firstLength >= secondLength && firstLength >= thirdLength) {
      maxTime = 0;
      maxLength = firstLength;
    } else if (secondLength >= firstLength && secondLength >= thirdLength) {
      maxTime = 1;
      maxLength = secondLength;
    } else {
      maxTime = 2;
      maxLength = thirdLength;
    }

    // Adjust data length to even number for low pass filtering
    if (maxLength % 2 != 0) maxLength -= 1;

    float[][][] linearAccelerationArray = new float[linearAcceleration.length][linearAcceleration[0].length][maxLength];
    float[][][] gyroscopeArray = new float[gyroscope.length][gyroscope[0].length][maxLength];

    for (int time = 0; time < Enum.NUM_TIME; time++) {
      if (time == maxTime) {
        for (int axis = 0; axis < Enum.NUM_AXIS; axis++) {
          for (int length = 0; length < maxLength; length++) {
            linearAccelerationArray[time][axis][length] = linearAcceleration[time][axis][length];
            gyroscopeArray[time][axis][length] = gyroscope[time][axis][length];
          }
        }
      } else {
        int diff = maxLength - linearAcceleration[time][0].length;

        for (int axis = 0; axis < Enum.NUM_AXIS; axis++) {
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
      for (int axis = 0; axis < Enum.NUM_AXIS; axis++) {
        for (int length = 0; length < registeredDataLength; length++) {
          linearAccelerationArray[axis][length] = linearAcceleration[axis][length];
          gyroscopeArray[axis][length] = gyroscope[axis][length];
        }
      }
    } else if (registeredDataLength > linearAcceleration[0].length) {
      // New data is shorter than registered data.
      int diff = registeredDataLength - linearAcceleration[0].length;

      for (int axis = 0; axis < Enum.NUM_AXIS; axis++) {
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
      for (int axis = 0; axis < Enum.NUM_AXIS; axis++) {
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
