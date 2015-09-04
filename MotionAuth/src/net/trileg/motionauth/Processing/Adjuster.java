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

  public ArrayList<float[][][]> adjust(ArrayList<ArrayList<ArrayList<Float>>> acceleration,
                                       ArrayList<ArrayList<ArrayList<Float>>> gyroscope) {
    // Get max length of each time data.
    int firstLength = acceleration.get(0).get(0).size();
    int secondLength = acceleration.get(1).get(0).size();
    int thirdLength = acceleration.get(2).get(0).size();

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

    float[][][] accelerationArray = new float[acceleration.size()][acceleration.get(0).size()][maxLength];
    float[][][] gyroscopeArray = new float[gyroscope.size()][gyroscope.get(0).size()][maxLength];

    for (int time = 0; time < Enum.NUM_TIME; time++) {
      if (time == maxTime) {
        for (int axis = 0; axis < Enum.NUM_AXIS; axis++) {
          for (int length = 0; length < maxLength; length++) {
            accelerationArray[time][axis][length] = acceleration.get(time).get(axis).get(length);
            gyroscopeArray[time][axis][length] = gyroscope.get(time).get(axis).get(length);
          }
        }
      } else {
        int diff = maxLength - acceleration.get(time).get(0).size();

        for (int axis = 0; axis < Enum.NUM_AXIS; axis++) {
          for (int length = 0; length < maxLength; length++) {
            if (length < maxLength - diff) {
              accelerationArray[time][axis][length] = acceleration.get(time).get(axis).get(length);
              gyroscopeArray[time][axis][length] = gyroscope.get(time).get(axis).get(length);
            } else {
              accelerationArray[time][axis][length] = 0;
              gyroscopeArray[time][axis][length] = 0;
            }
          }
        }
      }
    }

    ArrayList<float[][][]> result = new ArrayList<>();
    result.add(accelerationArray);
    result.add(gyroscopeArray);
    return result;
  }


  public ArrayList<float[][]> adjust(ArrayList<ArrayList<Float>> acceleration, ArrayList<ArrayList<Float>> gyroscope,
                                     int registeredDataLength) {
    float[][] accelerationArray = new float[acceleration.size()][registeredDataLength];
    float[][] gyroscopeArray = new float[gyroscope.size()][registeredDataLength];

    if (registeredDataLength < acceleration.get(0).size()) {
      // New data is longer than registered data.
      for (int axis = 0; axis < Enum.NUM_AXIS; axis++) {
        for (int length = 0; length < registeredDataLength; length++) {
          accelerationArray[axis][length] = acceleration.get(axis).get(length);
          gyroscopeArray[axis][length] = gyroscope.get(axis).get(length);
        }
      }
    } else if (registeredDataLength > acceleration.get(0).size()) {
      // New data is shorter than registered data.
      int diff = registeredDataLength - acceleration.get(0).size();

      for (int axis = 0; axis < Enum.NUM_AXIS; axis++) {
        for (int length = 0; length < registeredDataLength; length++) {
          if (length < registeredDataLength - diff) {
            accelerationArray[axis][length] = acceleration.get(axis).get(length);
            gyroscopeArray[axis][length] = gyroscope.get(axis).get(length);
          } else {
            accelerationArray[axis][length] = 0;
            gyroscopeArray[axis][length] = 0;
          }
        }
      }
    } else {
      for (int axis = 0; axis < Enum.NUM_AXIS; axis++) {
        for (int length = 0; length < registeredDataLength; length++) {
          accelerationArray[axis][length] = acceleration.get(axis).get(length);
          gyroscopeArray[axis][length] = gyroscope.get(axis).get(length);
        }
      }
    }

    ArrayList<float[][]> result = new ArrayList<>();
    result.add(accelerationArray);
    result.add(gyroscopeArray);
    return result;
  }
}
